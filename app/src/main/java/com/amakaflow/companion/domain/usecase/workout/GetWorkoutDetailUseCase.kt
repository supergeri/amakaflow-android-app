package com.amakaflow.companion.domain.usecase.workout

import com.amakaflow.companion.data.model.Workout
import com.amakaflow.companion.domain.Result
import com.amakaflow.companion.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting a specific workout by ID.
 */
class GetWorkoutDetailUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    /**
     * Get a workout by ID as a Flow.
     * Checks cache and local storage first, then fetches from API if needed.
     */
    operator fun invoke(workoutId: String): Flow<Result<Workout>> {
        return workoutRepository.getWorkout(workoutId)
    }
}
