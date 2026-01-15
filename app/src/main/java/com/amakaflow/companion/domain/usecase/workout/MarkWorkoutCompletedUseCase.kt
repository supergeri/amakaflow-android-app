package com.amakaflow.companion.domain.usecase.workout

import com.amakaflow.companion.domain.repository.WorkoutRepository
import javax.inject.Inject

/**
 * Use case for marking a workout as completed in local storage.
 */
class MarkWorkoutCompletedUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    /**
     * Mark a workout as completed in local storage.
     *
     * @param workoutId The ID of the workout to mark as completed.
     */
    suspend operator fun invoke(workoutId: String) {
        workoutRepository.markWorkoutCompleted(workoutId)
    }
}
