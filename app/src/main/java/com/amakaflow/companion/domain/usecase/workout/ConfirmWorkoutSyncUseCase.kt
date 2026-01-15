package com.amakaflow.companion.domain.usecase.workout

import com.amakaflow.companion.domain.Result
import com.amakaflow.companion.domain.repository.WorkoutRepository
import javax.inject.Inject

/**
 * Use case for confirming successful sync of a workout to the backend.
 */
class ConfirmWorkoutSyncUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    /**
     * Confirm that a workout has been successfully synced.
     *
     * @param workoutId The ID of the workout to confirm sync for.
     */
    suspend operator fun invoke(workoutId: String): Result<Unit> {
        return workoutRepository.confirmSync(workoutId)
    }
}
