package com.amakaflow.companion.domain.usecase.completion

import com.amakaflow.companion.domain.Result
import com.amakaflow.companion.domain.repository.CompletionRepository
import com.amakaflow.companion.domain.repository.CompletionsResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting the user's workout completion history.
 */
class GetCompletionHistoryUseCase @Inject constructor(
    private val completionRepository: CompletionRepository
) {
    /**
     * Get paginated list of workout completions.
     *
     * @param limit Maximum number of completions to return.
     * @param offset Number of completions to skip (for pagination).
     */
    operator fun invoke(limit: Int = 50, offset: Int = 0): Flow<Result<CompletionsResult>> {
        return completionRepository.getCompletions(limit, offset)
    }
}
