package com.amakaflow.companion.domain.usecase.completion

import com.amakaflow.companion.data.model.WorkoutCompletionDetail
import com.amakaflow.companion.domain.Result
import com.amakaflow.companion.domain.repository.CompletionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting detailed information about a workout completion.
 */
class GetCompletionDetailUseCase @Inject constructor(
    private val completionRepository: CompletionRepository
) {
    /**
     * Get detailed information about a specific completion.
     *
     * @param completionId The ID of the completion to get details for.
     */
    operator fun invoke(completionId: String): Flow<Result<WorkoutCompletionDetail>> {
        return completionRepository.getCompletionDetail(completionId)
    }
}
