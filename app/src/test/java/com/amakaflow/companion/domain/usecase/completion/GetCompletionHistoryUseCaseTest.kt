package com.amakaflow.companion.domain.usecase.completion

import app.cash.turbine.test
import com.amakaflow.companion.domain.Result
import com.amakaflow.companion.domain.repository.CompletionRepository
import com.amakaflow.companion.domain.repository.CompletionsResult
import com.amakaflow.companion.test.TestFixtures
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetCompletionHistoryUseCaseTest {

    private lateinit var useCase: GetCompletionHistoryUseCase
    private lateinit var mockRepository: CompletionRepository

    @Before
    fun setup() {
        mockRepository = mockk()
        useCase = GetCompletionHistoryUseCase(mockRepository)
    }

    @Test
    fun `invoke returns completions on success`() = runTest {
        // Given
        val completionsResult = CompletionsResult(
            completions = TestFixtures.sampleCompletions,
            total = 3
        )
        every { mockRepository.getCompletions(50, 0) } returns flowOf(Result.Success(completionsResult))

        // When/Then
        useCase().test {
            val result = awaitItem()
            assertThat(result).isInstanceOf(Result.Success::class.java)
            assertThat((result as Result.Success).data.completions).hasSize(3)
            assertThat(result.data.total).isEqualTo(3)
            awaitComplete()
        }
    }

    @Test
    fun `invoke returns error on failure`() = runTest {
        // Given
        every { mockRepository.getCompletions(50, 0) } returns flowOf(Result.Error("Failed to load history"))

        // When/Then
        useCase().test {
            val result = awaitItem()
            assertThat(result).isInstanceOf(Result.Error::class.java)
            assertThat((result as Result.Error).message).isEqualTo("Failed to load history")
            awaitComplete()
        }
    }

    @Test
    fun `invoke emits Loading first then Success`() = runTest {
        // Given
        val completionsResult = CompletionsResult(
            completions = TestFixtures.sampleCompletions,
            total = 3
        )
        every { mockRepository.getCompletions(50, 0) } returns flow {
            emit(Result.Loading)
            emit(Result.Success(completionsResult))
        }

        // When/Then
        useCase().test {
            assertThat(awaitItem()).isEqualTo(Result.Loading)
            val successResult = awaitItem()
            assertThat(successResult).isInstanceOf(Result.Success::class.java)
            awaitComplete()
        }
    }

    @Test
    fun `invoke passes limit and offset to repository`() = runTest {
        // Given
        val completionsResult = CompletionsResult(
            completions = emptyList(),
            total = 0
        )
        every { mockRepository.getCompletions(20, 10) } returns flowOf(Result.Success(completionsResult))

        // When
        useCase(limit = 20, offset = 10).test {
            awaitItem()
            awaitComplete()
        }

        // Then
        verify { mockRepository.getCompletions(20, 10) }
    }

    @Test
    fun `invoke returns empty list when no completions`() = runTest {
        // Given
        val completionsResult = CompletionsResult(
            completions = emptyList(),
            total = 0
        )
        every { mockRepository.getCompletions(50, 0) } returns flowOf(Result.Success(completionsResult))

        // When/Then
        useCase().test {
            val result = awaitItem()
            assertThat(result).isInstanceOf(Result.Success::class.java)
            assertThat((result as Result.Success).data.completions).isEmpty()
            assertThat(result.data.total).isEqualTo(0)
            awaitComplete()
        }
    }
}
