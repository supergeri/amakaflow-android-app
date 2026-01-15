package com.amakaflow.companion.domain.usecase.workout

import com.amakaflow.companion.data.model.Workout
import com.amakaflow.companion.domain.Result
import com.amakaflow.companion.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting workouts that have been pushed to this device.
 */
class GetPushedWorkoutsUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    /**
     * Get pushed workouts from the API as a Flow.
     * Emits Loading, then Success with workouts or Error.
     */
    operator fun invoke(): Flow<Result<List<Workout>>> {
        return workoutRepository.getPushedWorkouts()
    }

    /**
     * Get locally stored pushed workouts.
     * Use this when offline or for faster initial load.
     */
    fun getLocal(): Flow<List<Workout>> {
        return workoutRepository.getLocalPushedWorkouts()
    }

    /**
     * Get locally stored pushed workouts synchronously.
     */
    suspend fun getLocalSync(): List<Workout> {
        return workoutRepository.getLocalPushedWorkoutsSync()
    }
}
