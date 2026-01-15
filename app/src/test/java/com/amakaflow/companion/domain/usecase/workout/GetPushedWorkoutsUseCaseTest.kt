package com.amakaflow.companion.domain.usecase.workout

import app.cash.turbine.test
import com.amakaflow.companion.domain.Result
import com.amakaflow.companion.domain.repository.WorkoutRepository
import com.amakaflow.companion.test.TestFixtures
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetPushedWorkoutsUseCaseTest {

    private lateinit var useCase: GetPushedWorkoutsUseCase
    private lateinit var mockRepository: WorkoutRepository

    @Before
    fun setup() {
        mockRepository = mockk()
        useCase = GetPushedWorkoutsUseCase(mockRepository)
    }

    @Test
    fun `invoke returns workouts on success`() = runTest {
        // Given
        val workouts = TestFixtures.sampleWorkouts
        every { mockRepository.getPushedWorkouts() } returns flowOf(Result.Success(workouts))

        // When/Then
        useCase().test {
            val result = awaitItem()
            assertThat(result).isInstanceOf(Result.Success::class.java)
            assertThat((result as Result.Success).data).hasSize(3)
            awaitComplete()
        }
    }

    @Test
    fun `invoke returns error on failure`() = runTest {
        // Given
        every { mockRepository.getPushedWorkouts() } returns flowOf(Result.Error("Network error"))

        // When/Then
        useCase().test {
            val result = awaitItem()
            assertThat(result).isInstanceOf(Result.Error::class.java)
            assertThat((result as Result.Error).message).isEqualTo("Network error")
            awaitComplete()
        }
    }

    @Test
    fun `invoke emits Loading first then Success`() = runTest {
        // Given
        val workouts = TestFixtures.sampleWorkouts
        every { mockRepository.getPushedWorkouts() } returns flow {
            emit(Result.Loading)
            emit(Result.Success(workouts))
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
    fun `invoke returns empty list when no workouts`() = runTest {
        // Given
        every { mockRepository.getPushedWorkouts() } returns flowOf(Result.Success(emptyList()))

        // When/Then
        useCase().test {
            val result = awaitItem()
            assertThat(result).isInstanceOf(Result.Success::class.java)
            assertThat((result as Result.Success).data).isEmpty()
            awaitComplete()
        }
    }

    @Test
    fun `getLocal returns workouts from local storage`() = runTest {
        // Given
        val workouts = TestFixtures.sampleWorkouts
        every { mockRepository.getLocalPushedWorkouts() } returns flowOf(workouts)

        // When/Then
        useCase.getLocal().test {
            val result = awaitItem()
            assertThat(result).hasSize(3)
            awaitComplete()
        }
    }

    @Test
    fun `getLocalSync returns workouts synchronously`() = runTest {
        // Given
        val workouts = TestFixtures.sampleWorkouts
        coEvery { mockRepository.getLocalPushedWorkoutsSync() } returns workouts

        // When
        val result = useCase.getLocalSync()

        // Then
        assertThat(result).hasSize(3)
    }
}
