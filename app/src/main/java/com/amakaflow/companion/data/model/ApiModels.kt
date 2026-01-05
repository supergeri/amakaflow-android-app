package com.amakaflow.companion.data.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Pairing request to exchange code for JWT
 */
@Serializable
data class PairingRequest(
    val token: String? = null,
    @SerialName("short_code")
    val shortCode: String? = null,
    @SerialName("device_info")
    val deviceInfo: DeviceInfo
)

/**
 * Device information for pairing
 */
@Serializable
data class DeviceInfo(
    val device: String,
    val os: String,
    @SerialName("app_version")
    val appVersion: String,
    @SerialName("device_id")
    val deviceId: String
)

/**
 * Pairing response with JWT
 */
@Serializable
data class PairingResponse(
    val jwt: String,
    val profile: UserProfile? = null,
    @SerialName("expires_at")
    val expiresAt: String
)

/**
 * User profile information
 */
@Serializable
data class UserProfile(
    val id: String,
    val email: String? = null,
    val name: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null
)

/**
 * Token refresh request
 */
@Serializable
data class TokenRefreshRequest(
    @SerialName("device_id")
    val deviceId: String
)

/**
 * Token refresh response
 */
@Serializable
data class TokenRefreshResponse(
    val jwt: String,
    @SerialName("expires_at")
    val expiresAt: Instant,
    @SerialName("refreshed_at")
    val refreshedAt: Instant
)

/**
 * API error response
 */
@Serializable
data class ApiErrorResponse(
    val detail: String? = null,
    val error: String? = null,
    val message: String? = null
)

/**
 * Incoming workouts response
 */
@Serializable
data class IncomingWorkoutsResponse(
    val workouts: List<Workout>
)

/**
 * Health metrics for workout completion
 */
@Serializable
data class HealthMetrics(
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
    val steps: Int? = null
)

/**
 * Workout completion submission
 */
@Serializable
data class WorkoutCompletionSubmission(
    @SerialName("workout_id")
    val workoutId: String? = null,
    @SerialName("workout_name")
    val workoutName: String,
    @SerialName("started_at")
    val startedAt: Instant,
    @SerialName("ended_at")
    val endedAt: Instant? = null,
    @SerialName("duration_seconds")
    val durationSeconds: Int,
    val source: CompletionSource,
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
    @SerialName("device_info")
    val deviceInfo: CompletionDeviceInfo? = null,
    @SerialName("workout_structure")
    val workoutStructure: List<WorkoutIntervalSubmission>? = null
)

/**
 * Simplified interval for workout submission
 */
@Serializable
data class WorkoutIntervalSubmission(
    val type: String,
    val seconds: Int? = null,
    val target: String? = null,
    val reps: Int? = null,
    val sets: Int? = null,
    val name: String? = null
)

/**
 * Heart rate data point for completion detail
 */
@Serializable
data class HeartRateDataPoint(
    val timestamp: Instant,
    val bpm: Int
)

/**
 * Device info for completion detail
 */
@Serializable
data class CompletionDeviceInfo(
    val model: String? = null,
    val platform: String? = null,
    @SerialName("os_version")
    val osVersion: String? = null
) {
    val displayName: String
        get() {
            model?.let {
                return when {
                    it.startsWith("Pixel") -> it
                    it.startsWith("SM-") -> "Samsung Galaxy"
                    else -> it
                }
            }
            platform?.let {
                return when (it.lowercase()) {
                    "android" -> "Android"
                    "wear_os" -> "Wear OS"
                    else -> it.replaceFirstChar { c -> c.uppercase() }
                }
            }
            return "Unknown Device"
        }
}

/**
 * Extended workout completion detail
 */
@Serializable
data class WorkoutCompletionDetail(
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
    val steps: Int? = null,
    @SerialName("distance_meters")
    val distanceMeters: Int? = null,
    val source: CompletionSource,
    @SerialName("device_info")
    val deviceInfo: CompletionDeviceInfo? = null,
    @SerialName("heart_rate_samples")
    val heartRateSamples: List<HeartRateDataPoint>? = null,
    @SerialName("synced_to_strava")
    val syncedToStrava: Boolean? = null,
    @SerialName("strava_activity_id")
    val stravaActivityId: String? = null,
    @SerialName("workout_id")
    val workoutId: String? = null,
    @SerialName("workout_structure")
    val workoutStructure: List<WorkoutInterval>? = null
) {
    val resolvedEndedAt: Instant
        get() = endedAt ?: (startedAt + kotlin.time.Duration.parse("${durationSeconds}s"))

    val isSyncedToStrava: Boolean
        get() = syncedToStrava ?: false

    val hasHeartRateData: Boolean
        get() = avgHeartRate != null || !heartRateSamples.isNullOrEmpty()

    val hasHeartRateSamples: Boolean
        get() = !heartRateSamples.isNullOrEmpty()

    val hasSummaryMetrics: Boolean
        get() = activeCalories != null || steps != null || distanceMeters != null

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
}

/**
 * Voice workout parsing request
 */
@Serializable
data class VoiceWorkoutRequest(
    val transcript: String,
    val sport: String? = null
)

/**
 * Voice workout parsing response
 */
@Serializable
data class VoiceWorkoutResponse(
    val workout: Workout? = null,
    val error: String? = null,
    @SerialName("raw_response")
    val rawResponse: String? = null
)

/**
 * Completions list response wrapper
 */
@Serializable
data class CompletionsResponse(
    val success: Boolean,
    val completions: List<WorkoutCompletion>,
    val total: Int
)

/**
 * Single completion detail response wrapper
 */
@Serializable
data class CompletionDetailResponse(
    val success: Boolean,
    val completion: WorkoutCompletionDetail
)
