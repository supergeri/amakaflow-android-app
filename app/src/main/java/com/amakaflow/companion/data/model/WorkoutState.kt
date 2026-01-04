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
 */
data class FlattenedInterval(
    val index: Int,
    val interval: WorkoutInterval,
    val roundInfo: String? = null
) {
    val stepType: StepType
        get() = when (interval) {
            is WorkoutInterval.Warmup, is WorkoutInterval.Cooldown, is WorkoutInterval.Time -> StepType.TIMED
            is WorkoutInterval.Reps -> StepType.REPS
            is WorkoutInterval.Distance -> StepType.DISTANCE
            is WorkoutInterval.Repeat -> StepType.TIMED // Should be flattened before reaching here
        }

    val stepName: String
        get() = when (val i = interval) {
            is WorkoutInterval.Warmup -> "Warm Up"
            is WorkoutInterval.Cooldown -> "Cool Down"
            is WorkoutInterval.Time -> i.target ?: "Timed Interval"
            is WorkoutInterval.Reps -> i.name
            is WorkoutInterval.Distance -> "Distance"
            is WorkoutInterval.Repeat -> "Repeat" // Should be flattened
        }

    val durationSeconds: Int?
        get() = when (val i = interval) {
            is WorkoutInterval.Warmup -> i.seconds
            is WorkoutInterval.Cooldown -> i.seconds
            is WorkoutInterval.Time -> i.seconds
            is WorkoutInterval.Reps -> null
            is WorkoutInterval.Distance -> null
            is WorkoutInterval.Repeat -> null
        }

    val targetReps: Int?
        get() = when (val i = interval) {
            is WorkoutInterval.Reps -> i.reps
            else -> null
        }

    val restSeconds: Int?
        get() = when (val i = interval) {
            is WorkoutInterval.Reps -> i.restSec
            else -> null
        }
}

/**
 * Utility to flatten workout intervals
 */
object IntervalFlattener {
    fun flatten(intervals: List<WorkoutInterval>): List<FlattenedInterval> {
        val result = mutableListOf<FlattenedInterval>()
        flattenRecursive(intervals, result, null)
        return result.mapIndexed { index, item -> item.copy(index = index) }
    }

    private fun flattenRecursive(
        intervals: List<WorkoutInterval>,
        result: MutableList<FlattenedInterval>,
        roundPrefix: String?
    ) {
        for (interval in intervals) {
            when (interval) {
                is WorkoutInterval.Repeat -> {
                    for (rep in 1..interval.reps) {
                        val newPrefix = if (roundPrefix != null) {
                            "$roundPrefix.$rep/${interval.reps}"
                        } else {
                            "Round $rep/${interval.reps}"
                        }
                        flattenRecursive(interval.intervals, result, newPrefix)
                    }
                }
                else -> {
                    result.add(FlattenedInterval(
                        index = 0, // Will be updated after
                        interval = interval,
                        roundInfo = roundPrefix
                    ))
                }
            }
        }
    }
}
