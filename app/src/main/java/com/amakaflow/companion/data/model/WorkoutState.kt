package com.amakaflow.companion.data.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Workout execution phase
 */
@Serializable
enum class WorkoutPhase {
    @SerialName("idle") IDLE,
    @SerialName("running") RUNNING,
    @SerialName("paused") PAUSED,
    @SerialName("resting") RESTING,
    @SerialName("ended") ENDED
}

/**
 * Type of workout step
 */
@Serializable
enum class StepType {
    @SerialName("timed") TIMED,
    @SerialName("reps") REPS,
    @SerialName("distance") DISTANCE
}

/**
 * Reason workout ended
 */
@Serializable
enum class EndReason {
    @SerialName("completed") COMPLETED,
    @SerialName("userEnded") USER_ENDED,
    @SerialName("discarded") DISCARDED,
    @SerialName("savedForLater") SAVED_FOR_LATER,
    @SerialName("error") ERROR
}

/**
 * Saved workout progress for resume functionality
 */
@Serializable
data class SavedWorkoutProgress(
    val workoutId: String,
    val workoutName: String,
    val currentStepIndex: Int,
    val elapsedSeconds: Int,
    val savedAt: Instant = Clock.System.now()
)

/**
 * Current workout state for broadcasting
 */
@Serializable
data class WorkoutState(
    val stateVersion: Int,
    val workoutId: String,
    val workoutName: String,
    val phase: WorkoutPhase,
    val stepIndex: Int,
    val stepCount: Int,
    val stepName: String,
    val stepType: StepType,
    val remainingMs: Int? = null,
    val roundInfo: String? = null,
    val targetReps: Int? = null,
    val lastCommandAck: CommandAck? = null
)

/**
 * Command acknowledgment
 */
@Serializable
data class CommandAck(
    val commandId: String,
    val status: CommandStatus,
    val errorCode: String? = null
)

/**
 * Command status
 */
@Serializable
enum class CommandStatus {
    @SerialName("success") SUCCESS,
    @SerialName("error") ERROR
}

/**
 * Remote commands for workout control
 */
@Serializable
enum class RemoteCommand {
    @SerialName("PAUSE") PAUSE,
    @SerialName("RESUME") RESUME,
    @SerialName("NEXT_STEP") NEXT_STEP,
    @SerialName("PREV_STEP") PREVIOUS_STEP,
    @SerialName("SKIP_REST") SKIP_REST,
    @SerialName("END") END
}

/**
 * Flattened interval for workout execution
 * Matches iOS FlattenedInterval structure for consistent behavior
 */
data class FlattenedInterval(
    val index: Int,
    val interval: WorkoutInterval,
    val roundInfo: String? = null,
    val hasRestAfter: Boolean = false,
    val restAfterSeconds: Int? = null // null = manual rest ("tap when ready"), 0 = no rest, >0 = timed countdown
) {
    val stepType: StepType
        get() = when (interval) {
            is WorkoutInterval.Warmup, is WorkoutInterval.Cooldown, is WorkoutInterval.Time -> StepType.TIMED
            is WorkoutInterval.Reps -> StepType.REPS
            is WorkoutInterval.Distance -> StepType.DISTANCE
            is WorkoutInterval.Repeat -> StepType.TIMED // Should be flattened before reaching here
            is WorkoutInterval.Rest -> StepType.TIMED // Rest intervals have optional duration
        }

    val stepName: String
        get() = when (val i = interval) {
            is WorkoutInterval.Warmup -> "Warm Up"
            is WorkoutInterval.Cooldown -> "Cool Down"
            is WorkoutInterval.Time -> i.target ?: "Timed Interval"
            is WorkoutInterval.Reps -> i.name
            is WorkoutInterval.Distance -> "Distance"
            is WorkoutInterval.Repeat -> "Repeat" // Should be flattened
            is WorkoutInterval.Rest -> if (i.seconds != null) "Rest" else "Rest"
        }

    val durationSeconds: Int?
        get() = when (val i = interval) {
            is WorkoutInterval.Warmup -> i.seconds
            is WorkoutInterval.Cooldown -> i.seconds
            is WorkoutInterval.Time -> i.seconds
            is WorkoutInterval.Reps -> null
            is WorkoutInterval.Distance -> null
            is WorkoutInterval.Repeat -> null
            is WorkoutInterval.Rest -> i.seconds  // null for manual rest
        }

    val targetReps: Int?
        get() = when (interval) {
            is WorkoutInterval.Reps -> interval.reps
            else -> null
        }

    val restSeconds: Int?
        get() = when (interval) {
            is WorkoutInterval.Reps -> interval.restSec
            else -> null
        }

    /** Whether this is a rest interval (not a work interval) */
    val isRestInterval: Boolean
        get() = interval is WorkoutInterval.Rest

    /** Whether this is a manual rest (tap when ready) vs timed rest */
    val isManualRestInterval: Boolean
        get() = interval is WorkoutInterval.Rest && (interval as WorkoutInterval.Rest).seconds == null
}

/**
 * Utility to flatten workout intervals
 * Matches iOS IntervalFlattener behavior for consistent rest handling
 */
object IntervalFlattener {
    fun flatten(intervals: List<WorkoutInterval>): List<FlattenedInterval> {
        val result = mutableListOf<FlattenedInterval>()
        flattenRecursive(intervals, result, null, isInsideRepeat = false)
        return result.mapIndexed { index, item ->
            // Set hasRestAfter for all steps except cooldown
            val isCooldown = item.interval is WorkoutInterval.Cooldown
            item.copy(
                index = index,
                hasRestAfter = !isCooldown
            )
        }
    }

    private fun flattenRecursive(
        intervals: List<WorkoutInterval>,
        result: MutableList<FlattenedInterval>,
        roundPrefix: String?,
        isInsideRepeat: Boolean
    ) {
        for ((i, interval) in intervals.withIndex()) {
            when (interval) {
                is WorkoutInterval.Repeat -> {
                    for (rep in 1..interval.reps) {
                        val newPrefix = if (roundPrefix != null) {
                            "$roundPrefix.$rep/${interval.reps}"
                        } else {
                            "Round $rep/${interval.reps}"
                        }
                        flattenRecursive(interval.intervals, result, newPrefix, isInsideRepeat = true)
                    }
                }
                is WorkoutInterval.Time -> {
                    // Skip Time intervals inside Repeat blocks - they represent rest periods
                    // for the previous exercise, not separate exercise steps
                    if (isInsideRepeat) {
                        // Apply this time as rest to the previous step if it exists
                        if (result.isNotEmpty()) {
                            val lastStep = result.last()
                            result[result.size - 1] = lastStep.copy(
                                restAfterSeconds = interval.seconds
                            )
                        }
                    } else {
                        // Top-level Time intervals are standalone timed exercises
                        result.add(FlattenedInterval(
                            index = 0,
                            interval = interval,
                            roundInfo = roundPrefix,
                            restAfterSeconds = null // Manual rest after standalone timed intervals
                        ))
                    }
                }
                is WorkoutInterval.Rest -> {
                    // Rest intervals represent explicit rest periods
                    // Apply as rest to the previous step if inside a repeat, otherwise add as step
                    if (isInsideRepeat && result.isNotEmpty()) {
                        // Apply this rest to the previous step
                        val lastStep = result.last()
                        result[result.size - 1] = lastStep.copy(
                            restAfterSeconds = interval.seconds  // null = manual rest
                        )
                    } else {
                        // Standalone Rest interval - add as a separate step
                        result.add(FlattenedInterval(
                            index = 0,
                            interval = interval,
                            roundInfo = roundPrefix,
                            restAfterSeconds = null,
                            hasRestAfter = false  // Rest steps don't have additional rest after
                        ))
                    }
                }
                is WorkoutInterval.Reps -> {
                    // Check if next interval is a Time (rest period)
                    val nextInterval = intervals.getOrNull(i + 1)
                    val restSeconds = when {
                        // If reps has explicit restSec, use that
                        interval.restSec != null && interval.restSec > 0 -> interval.restSec
                        // If next interval is Time inside a repeat, it will be handled when we process it
                        // For now, default to manual rest (null)
                        else -> null
                    }
                    result.add(FlattenedInterval(
                        index = 0,
                        interval = interval,
                        roundInfo = roundPrefix,
                        restAfterSeconds = restSeconds
                    ))
                }
                else -> {
                    result.add(FlattenedInterval(
                        index = 0,
                        interval = interval,
                        roundInfo = roundPrefix,
                        restAfterSeconds = null // Manual rest by default
                    ))
                }
            }
        }
    }
}
