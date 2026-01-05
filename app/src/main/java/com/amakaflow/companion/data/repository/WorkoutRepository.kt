package com.amakaflow.companion.data.repository

import com.amakaflow.companion.data.api.AmakaflowApi
import com.amakaflow.companion.data.model.Workout
import com.amakaflow.companion.data.model.WorkoutCompletion
import com.amakaflow.companion.data.model.WorkoutCompletionDetail
import com.amakaflow.companion.data.model.WorkoutCompletionSubmission
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

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
    fun getIncomingWorkouts(): Flow<Result<List<Workout>>> = flow {
        emit(Result.Loading)
        try {
            val response = api.getIncomingWorkouts()
            if (response.isSuccessful) {
                emit(Result.Success(response.body() ?: emptyList()))
            } else {
                emit(Result.Error("Failed to load workouts", response.code()))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown error"))
        }
    }

    fun getPushedWorkouts(): Flow<Result<List<Workout>>> = flow {
        emit(Result.Loading)
        try {
            val response = api.getPushedWorkouts()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    emit(Result.Success(body.workouts))
                } else {
                    emit(Result.Error(body.message ?: "Failed to load pushed workouts", response.code()))
                }
            } else {
                emit(Result.Error("Failed to load pushed workouts", response.code()))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown error"))
        }
    }

    fun getWorkout(id: String): Flow<Result<Workout>> = flow {
        emit(Result.Loading)
        try {
            val response = api.getWorkout(id)
            if (response.isSuccessful && response.body() != null) {
                emit(Result.Success(response.body()!!))
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
        return try {
            val response = api.completeWorkout(submission)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                Result.Error("Failed to submit completion", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }
}
