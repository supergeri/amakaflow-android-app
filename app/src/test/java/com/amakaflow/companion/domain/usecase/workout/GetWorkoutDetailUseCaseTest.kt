package com.amakaflow.companion.domain.usecase.workout

import app.cash.turbine.test
import com.amakaflow.companion.domain.Result
import com.amakaflow.companion.domain.repository.WorkoutRepository
import com.amakaflow.companion.test.TestFixtures
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetWorkoutDetailUseCaseTest {

    private lateinit var useCase: GetWorkoutDetailUseCase
    private lateinit var mockRepository: WorkoutRepository

    @Before
    fun setup() {
        mockRepository = mockk()
        useCase = GetWorkoutDetailUseCase(mockRepository)
    }

    @Test
    fun `invoke returns workout on success`() = runTest {
        // Given
        val workout = TestFixtures.hiitWorkout
        every { mockRepository.getWorkout("workout-001") } returns flowOf(Result.Success(workout))

        // When/Then
        useCase("workout-001").test {
            val result = awaitItem()
            assertThat(result).isInstanceOf(Result.Success::class.java)
            assertThat((result as Result.Success).data.id).isEqualTo("workout-001")
            assertThat(result.data.name).isEqualTo("HIIT Blast")
            awaitComplete()
        }
    }

    @Test
    fun `invoke returns error when workout not found`() = runTest {
        // Given
        every { mockRepository.getWorkout("invalid-id") } returns flowOf(Result.Error("Workout not found"))

        // When/Then
        useCase("invalid-id").test {
            val result = awaitItem()
            assertThat(result).isInstanceOf(Result.Error::class.java)
            assertThat((result as Result.Error).message).isEqualTo("Workout not found")
            awaitComplete()
        }
    }

    @Test
    fun `invoke emits Loading first then Success`() = runTest {
        // Given
        val workout = TestFixtures.strengthWorkout
        every { mockRepository.getWorkout("workout-002") } returns flow {
            emit(Result.Loading)
            emit(Result.Success(workout))
        }

        // When/Then
        useCase("workout-002").test {
            assertThat(awaitItem()).isEqualTo(Result.Loading)
            val successResult = awaitItem()
            assertThat(successResult).isInstanceOf(Result.Success::class.java)
            assertThat((successResult as Result.Success).data.name).isEqualTo("Upper Body Strength")
            awaitComplete()
        }
    }

    @Test
    fun `invoke passes correct workoutId to repository`() = runTest {
        // Given
        val workout = TestFixtures.runningWorkout
        every { mockRepository.getWorkout("workout-003") } returns flowOf(Result.Success(workout))

        // When/Then
        useCase("workout-003").test {
            val result = awaitItem() as Result.Success
            assertThat(result.data.id).isEqualTo("workout-003")
            awaitComplete()
        }
    }
}
