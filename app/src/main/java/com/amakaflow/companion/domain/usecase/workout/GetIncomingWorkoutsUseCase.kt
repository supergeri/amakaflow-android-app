package com.amakaflow.companion.domain.usecase.workout

import com.amakaflow.companion.data.model.Workout
import com.amakaflow.companion.domain.Result
import com.amakaflow.companion.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting incoming workouts from the user's calendar.
 */
class GetIncomingWorkoutsUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    /**
     * Get incoming workouts as a Flow.
     * Emits Loading, then Success with workouts or Error.
     */
    operator fun invoke(): Flow<Result<List<Workout>>> {
        return workoutRepository.getIncomingWorkouts()
    }
}
