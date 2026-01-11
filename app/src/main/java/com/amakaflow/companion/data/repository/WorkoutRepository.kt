package com.amakaflow.companion.data.repository

import com.amakaflow.companion.data.api.AmakaflowApi
import com.amakaflow.companion.data.model.ConfirmSyncRequest
import com.amakaflow.companion.data.model.ReportSyncFailedRequest
import com.amakaflow.companion.data.model.Workout
import com.amakaflow.companion.data.model.WorkoutCompletion
import com.amakaflow.companion.data.model.WorkoutCompletionDetail
import com.amakaflow.companion.data.model.WorkoutCompletionSubmission
import com.amakaflow.companion.debug.DebugLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "API"

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val code: Int? = null) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}

/**
 * Paginated completions result with total count for pagination
 */
data class CompletionsResult(
    val completions: List<WorkoutCompletion>,
    val total: Int
)

@Singleton
class WorkoutRepository @Inject constructor(
    private val api: AmakaflowApi
) {
    // In-memory cache of workouts for quick lookup
    private val workoutCache = mutableMapOf<String, Workout>()

    /**
     * Cache workouts for later lookup by ID
     */
    private fun cacheWorkouts(workouts: List<Workout>) {
        workouts.forEach { workout ->
            workoutCache[workout.id] = workout
        }
    }

    /**
     * Get a cached workout by ID, or null if not found
     */
    fun getCachedWorkout(id: String): Workout? = workoutCache[id]

    fun getIncomingWorkouts(): Flow<Result<List<Workout>>> = flow {
        emit(Result.Loading)
        DebugLog.info("Fetching incoming workouts...", TAG)
        try {
            val response = api.getIncomingWorkouts()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    // Cache workouts for later lookup by ID
                    cacheWorkouts(body.workouts)
                    DebugLog.success("Fetched ${body.workouts.size} incoming workout(s)", TAG)
                    emit(Result.Success(body.workouts))
                } else {
                    DebugLog.error("Failed to load incoming workouts: ${body.message}", TAG)
                    emit(Result.Error(body.message ?: "Failed to load incoming workouts", response.code()))
                }
            } else {
                DebugLog.error("Failed to load incoming workouts: ${response.code()}", TAG)
                emit(Result.Error("Failed to load incoming workouts", response.code()))
            }
        } catch (e: Exception) {
            DebugLog.error("Exception fetching incoming workouts: ${e.message}", TAG)
            emit(Result.Error(e.message ?: "Unknown error"))
        }
    }

    fun getPushedWorkouts(): Flow<Result<List<Workout>>> = flow {
        emit(Result.Loading)
        DebugLog.info("Fetching pushed workouts...", TAG)
        try {
            val response = api.getPushedWorkouts()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    // Cache workouts for later lookup by ID
                    cacheWorkouts(body.workouts)
                    DebugLog.success("Fetched ${body.workouts.size} pushed workout(s)", TAG)
                    emit(Result.Success(body.workouts))
                } else {
                    DebugLog.error("Failed to load pushed workouts: ${body.message}", TAG)
                    emit(Result.Error(body.message ?: "Failed to load pushed workouts", response.code()))
                }
            } else {
                DebugLog.error("Failed to load pushed workouts: ${response.code()}", TAG)
                emit(Result.Error("Failed to load pushed workouts", response.code()))
            }
        } catch (e: Exception) {
            DebugLog.error("Exception fetching pushed workouts: ${e.message}", TAG)
            emit(Result.Error(e.message ?: "Unknown error"))
        }
    }

    fun getWorkout(id: String): Flow<Result<Workout>> = flow {
        // First check the cache for the workout
        val cachedWorkout = workoutCache[id]
        if (cachedWorkout != null) {
            emit(Result.Success(cachedWorkout))
            return@flow
        }

        // Workout not in cache, try fetching from API
        emit(Result.Loading)
        try {
            val response = api.getWorkout(id)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.workout != null) {
                    // Cache the fetched workout
                    workoutCache[id] = body.workout
                    emit(Result.Success(body.workout))
                } else {
                    emit(Result.Error(body.message ?: "Workout not found", response.code()))
                }
            } else {
                emit(Result.Error("Failed to load workout", response.code()))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown error"))
        }
    }

    fun getCompletions(limit: Int = 50, offset: Int = 0): Flow<Result<CompletionsResult>> = flow {
        emit(Result.Loading)
        try {
            val response = api.getCompletions(limit, offset)
            if (response.isSuccessful && response.body()?.success == true) {
                val body = response.body()!!
                emit(Result.Success(CompletionsResult(
                    completions = body.completions,
                    total = body.total
                )))
            } else {
                emit(Result.Error("Failed to load completions", response.code()))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown error"))
        }
    }

    fun getCompletionDetail(id: String): Flow<Result<WorkoutCompletionDetail>> = flow {
        emit(Result.Loading)
        try {
            val response = api.getCompletionDetail(id)
            if (response.isSuccessful && response.body()?.success == true) {
                emit(Result.Success(response.body()!!.completion))
            } else {
                emit(Result.Error("Failed to load completion detail", response.code()))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown error"))
        }
    }

    /**
     * Submit a completed workout to the API
     */
    suspend fun completeWorkout(submission: WorkoutCompletionSubmission): Result<WorkoutCompletion> {
        DebugLog.info("Submitting workout completion for: ${submission.workoutId}", TAG)
        return try {
            val response = api.completeWorkout(submission)
            if (response.isSuccessful && response.body() != null) {
                DebugLog.success("Workout completion submitted successfully", TAG)
                Result.Success(response.body()!!)
            } else {
                DebugLog.error("Failed to submit completion: ${response.code()}", TAG)
                Result.Error("Failed to submit completion", response.code())
            }
        } catch (e: Exception) {
            DebugLog.error("Exception submitting completion: ${e.message}", TAG)
            Result.Error(e.message ?: "Unknown error")
        }
    }

    /**
     * Confirm successful workout sync to backend (AMA-307)
     * Called after successfully saving a pushed workout to the device
     */
    suspend fun confirmSync(workoutId: String): Result<Unit> {
        DebugLog.info("Confirming sync for workout: $workoutId", TAG)
        return try {
            val request = ConfirmSyncRequest(workoutId = workoutId)
            val response = api.confirmSync(request)
            if (response.isSuccessful) {
                DebugLog.success("Sync confirmed for workout: $workoutId", TAG)
                Result.Success(Unit)
            } else {
                DebugLog.error("Failed to confirm sync: ${response.code()}", TAG)
                Result.Error("Failed to confirm sync", response.code())
            }
        } catch (e: Exception) {
            DebugLog.error("Exception confirming sync: ${e.message}", TAG)
            Result.Error(e.message ?: "Unknown error")
        }
    }

    /**
     * Report failed workout sync to backend (AMA-307)
     * Called when saving a pushed workout fails
     */
    suspend fun reportSyncFailed(workoutId: String, error: String): Result<Unit> {
        DebugLog.info("Reporting sync failure for workout: $workoutId - $error", TAG)
        return try {
            val request = ReportSyncFailedRequest(workoutId = workoutId, error = error)
            val response = api.reportSyncFailed(request)
            if (response.isSuccessful) {
                DebugLog.success("Sync failure reported for workout: $workoutId", TAG)
                Result.Success(Unit)
            } else {
                DebugLog.error("Failed to report sync failure: ${response.code()}", TAG)
                Result.Error("Failed to report sync failure", response.code())
            }
        } catch (e: Exception) {
            DebugLog.error("Exception reporting sync failure: ${e.message}", TAG)
            Result.Error(e.message ?: "Unknown error")
        }
    }
}
