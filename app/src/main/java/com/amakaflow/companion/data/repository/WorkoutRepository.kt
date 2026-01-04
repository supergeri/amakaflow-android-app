package com.amakaflow.companion.data.repository

import com.amakaflow.companion.data.api.AmakaflowApi
import com.amakaflow.companion.data.model.Workout
import com.amakaflow.companion.data.model.WorkoutCompletion
import com.amakaflow.companion.data.model.WorkoutCompletionDetail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val code: Int? = null) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}

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

    fun getCompletions(limit: Int = 50, offset: Int = 0): Flow<Result<List<WorkoutCompletion>>> = flow {
        emit(Result.Loading)
        try {
            val response = api.getCompletions(limit, offset)
            if (response.isSuccessful) {
                emit(Result.Success(response.body() ?: emptyList()))
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
            if (response.isSuccessful && response.body() != null) {
                emit(Result.Success(response.body()!!))
            } else {
                emit(Result.Error("Failed to load completion detail", response.code()))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown error"))
        }
    }
}
