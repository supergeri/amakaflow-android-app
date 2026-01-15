package com.amakaflow.companion.domain.repository

import com.amakaflow.companion.data.model.Workout
import com.amakaflow.companion.domain.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for workout operations.
 * Part of the domain layer - defines the contract for data access.
 */
interface WorkoutRepository {
    /**
     * Get workouts from the user's calendar (incoming workouts).
     */
    fun getIncomingWorkouts(): Flow<Result<List<Workout>>>

    /**
     * Get workouts that have been pushed to this device.
     */
    fun getPushedWorkouts(): Flow<Result<List<Workout>>>

    /**
     * Get a specific workout by ID.
     */
    fun getWorkout(id: String): Flow<Result<Workout>>

    /**
     * Get a cached workout by ID, or null if not found.
     * This is a synchronous operation from in-memory cache.
     */
    fun getCachedWorkout(id: String): Workout?

    /**
     * Confirm successful sync of a workout to the backend.
     */
    suspend fun confirmSync(workoutId: String): Result<Unit>
}
