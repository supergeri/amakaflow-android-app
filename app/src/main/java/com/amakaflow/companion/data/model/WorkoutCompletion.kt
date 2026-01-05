package com.amakaflow.companion.data.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

/**
 * Completion source - which device recorded the workout
 */
@Serializable
enum class CompletionSource {
    @SerialName("apple_watch") APPLE_WATCH,
    @SerialName("garmin") GARMIN,
    @SerialName("manual") MANUAL,
    @SerialName("phone") PHONE,
    @SerialName("wear_os") WEAR_OS;

    val displayName: String
        get() = when (this) {
            APPLE_WATCH -> "Apple Watch"
            GARMIN -> "Garmin"
            MANUAL -> "Manual"
            PHONE -> "Phone"
            WEAR_OS -> "Wear OS"
        }

    val iconName: String
        get() = when (this) {
            APPLE_WATCH -> "watch"
            GARMIN -> "watch"
            MANUAL -> "edit"
            PHONE -> "smartphone"
            WEAR_OS -> "watch"
        }
}

/**
 * Workout completion record
 */
@Serializable
data class WorkoutCompletion(
    val id: String,
    @SerialName("workout_name")
    val workoutName: String,
    @SerialName("started_at")
    val startedAt: Instant,
    @SerialName("ended_at")
    val endedAt: Instant? = null,
    @SerialName("duration_seconds")
    val durationSeconds: Int,
    @SerialName("avg_heart_rate")
    val avgHeartRate: Int? = null,
    @SerialName("max_heart_rate")
    val maxHeartRate: Int? = null,
    @SerialName("min_heart_rate")
    val minHeartRate: Int? = null,
    @SerialName("active_calories")
    val activeCalories: Int? = null,
    @SerialName("total_calories")
    val totalCalories: Int? = null,
    @SerialName("distance_meters")
    val distanceMeters: Int? = null,
    val steps: Int? = null,
    val source: CompletionSource,
    @SerialName("synced_to_strava")
    val syncedToStrava: Boolean? = null,
    @SerialName("workout_id")
    val workoutId: String? = null,
    @SerialName("original_workout")
    val originalWorkout: Workout? = null
) {
    val resolvedEndedAt: Instant
        get() = endedAt ?: (startedAt + durationSeconds.seconds)

    val isSyncedToStrava: Boolean
        get() = syncedToStrava ?: false

    val canRerun: Boolean
        get() = originalWorkout != null

    val formattedDuration: String
        get() {
            val hours = durationSeconds / 3600
            val minutes = (durationSeconds % 3600) / 60
            val seconds = durationSeconds % 60

            return if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%d:%02d", minutes, seconds)
            }
        }

    val formattedStartTime: String
        get() {
            val localDateTime = startedAt.toLocalDateTime(TimeZone.currentSystemDefault())
            val hour = localDateTime.hour
            val minute = localDateTime.minute
            val amPm = if (hour >= 12) "PM" else "AM"
            val hour12 = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
            return String.format("%d:%02d %s", hour12, minute, amPm)
        }

    val formattedDate: String
        get() {
            val localDateTime = startedAt.toLocalDateTime(TimeZone.currentSystemDefault())
            val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            return "${months[localDateTime.monthNumber - 1]} ${localDateTime.dayOfMonth}"
        }

    val hasHealthMetrics: Boolean
        get() = avgHeartRate != null || activeCalories != null

    val dateCategory: DateCategory
        get() {
            val now = Clock.System.now()
            val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
            val completionDate = startedAt.toLocalDateTime(TimeZone.currentSystemDefault()).date
            val yesterday = (now - 1.days).toLocalDateTime(TimeZone.currentSystemDefault()).date
            val weekAgo = (now - 7.days).toLocalDateTime(TimeZone.currentSystemDefault()).date

            return when {
                completionDate == today -> DateCategory.Today
                completionDate == yesterday -> DateCategory.Yesterday
                completionDate >= weekAgo -> DateCategory.ThisWeek(startedAt)
                else -> DateCategory.Older(startedAt)
            }
        }
}

/**
 * Date category for grouping completions
 */
sealed class DateCategory {
    data object Today : DateCategory()
    data object Yesterday : DateCategory()
    data class ThisWeek(val date: Instant) : DateCategory()
    data class Older(val date: Instant) : DateCategory()

    val title: String
        get() = when (this) {
            is Today -> "Today"
            is Yesterday -> "Yesterday"
            is ThisWeek -> {
                val localDateTime = date.toLocalDateTime(TimeZone.currentSystemDefault())
                val dayNames = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
                val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                "${dayNames[localDateTime.dayOfWeek.ordinal]} ${months[localDateTime.monthNumber - 1]} ${localDateTime.dayOfMonth}"
            }
            is Older -> {
                val localDateTime = date.toLocalDateTime(TimeZone.currentSystemDefault())
                val dayNames = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
                val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                "${dayNames[localDateTime.dayOfWeek.ordinal]} ${months[localDateTime.monthNumber - 1]} ${localDateTime.dayOfMonth}"
            }
        }

    val sortOrder: Int
        get() = when (this) {
            is Today -> 0
            is Yesterday -> 1
            is ThisWeek -> 2
            is Older -> 3
        }
}

/**
 * Weekly summary statistics
 */
data class WeeklySummary(
    val workoutCount: Int,
    val totalDurationSeconds: Int,
    val totalCalories: Int
) {
    val formattedDuration: String
        get() {
            val hours = totalDurationSeconds / 3600
            val minutes = (totalDurationSeconds % 3600) / 60
            return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
        }

    val formattedCalories: String
        get() = if (totalCalories >= 1000) {
            String.format("%.1fk", totalCalories / 1000.0)
        } else {
            totalCalories.toString()
        }

    companion object {
        fun fromCompletions(completions: List<WorkoutCompletion>): WeeklySummary {
            return WeeklySummary(
                workoutCount = completions.size,
                totalDurationSeconds = completions.sumOf { it.durationSeconds },
                totalCalories = completions.sumOf { it.activeCalories ?: 0 }
            )
        }
    }
}
