package com.amakaflow.companion.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Workout sport types
 */
@Serializable
enum class WorkoutSport {
    @SerialName("running") RUNNING,
    @SerialName("cycling") CYCLING,
    @SerialName("strength") STRENGTH,
    @SerialName("mobility") MOBILITY,
    @SerialName("swimming") SWIMMING,
    @SerialName("cardio") CARDIO,
    @SerialName("other") OTHER;

    companion object {
        fun fromString(value: String): WorkoutSport {
            return when (value.lowercase()) {
                "running", "run" -> RUNNING
                "cycling", "bike", "biking" -> CYCLING
                "strength", "strengthtraining", "strength_training", "weights" -> STRENGTH
                "mobility", "yoga", "stretching", "flexibility" -> MOBILITY
                "swimming", "swim" -> SWIMMING
                "cardio", "hiit" -> CARDIO
                else -> OTHER
            }
        }
    }
}

/**
 * Workout source types
 */
@Serializable
enum class WorkoutSource {
    @SerialName("instagram") INSTAGRAM,
    @SerialName("youtube") YOUTUBE,
    @SerialName("image") IMAGE,
    @SerialName("ai") AI,
    @SerialName("coach") COACH,
    @SerialName("amaka") AMAKA,
    @SerialName("other") OTHER
}

/**
 * Main workout model
 */
@Serializable
data class Workout(
    val id: String,
    val name: String,
    val sport: WorkoutSport,
    val duration: Int, // seconds
    val intervals: List<WorkoutInterval> = emptyList(),
    val description: String? = null,
    val source: WorkoutSource,
    val sourceUrl: String? = null
) {
    val formattedDuration: String
        get() = WorkoutHelpers.formatDuration(duration)

    val intervalCount: Int
        get() = WorkoutHelpers.countIntervals(intervals)
}

/**
 * Scheduled workout with recurrence support
 */
@Serializable
data class ScheduledWorkout(
    val workout: Workout,
    val scheduledDate: String? = null, // ISO date string
    val scheduledTime: String? = null,
    val isRecurring: Boolean = false,
    val recurrenceDays: List<Int>? = null, // 0 = Sunday, 6 = Saturday
    val recurrenceWeeks: Int? = null,
    val syncedToGoogle: Boolean = false
) {
    val id: String get() = workout.id
}

/**
 * Helper functions for workout formatting
 */
object WorkoutHelpers {
    fun formatDuration(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60

        return if (hours > 0) {
            "${hours}h ${minutes}m"
        } else {
            "${minutes}m"
        }
    }

    fun countIntervals(intervals: List<WorkoutInterval>): Int {
        var count = 0
        for (interval in intervals) {
            when (interval) {
                is WorkoutInterval.Repeat -> {
                    count += interval.reps * countIntervals(interval.intervals)
                }
                else -> count += 1
            }
        }
        return count
    }

    fun formatDistance(meters: Int): String {
        return if (meters >= 1000) {
            val km = meters / 1000.0
            String.format("%.1f km", km)
        } else {
            "${meters}m"
        }
    }

    fun formatTime(seconds: Int): String {
        return if (seconds >= 60) {
            val minutes = seconds / 60
            val secs = seconds % 60
            if (secs > 0) {
                "${minutes}m ${secs}s"
            } else {
                "${minutes} min"
            }
        } else {
            "${seconds}s"
        }
    }
}
