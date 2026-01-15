package com.amakaflow.companion.ui.screens.workouts

import app.cash.turbine.test
import com.amakaflow.companion.domain.Result
import com.amakaflow.companion.domain.usecase.workout.GetIncomingWorkoutsUseCase
import com.amakaflow.companion.test.MainDispatcherRule
import com.amakaflow.companion.test.TestFixtures
import com.google.common.truth.Truth.assertThat
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class WorkoutsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockGetIncomingWorkouts: GetIncomingWorkoutsUseCase

    @Before
    fun setup() {
        mockGetIncomingWorkouts = mockk()
    }

    private fun createViewModel(): WorkoutsViewModel {
        return WorkoutsViewModel(mockGetIncomingWorkouts)
    }

    @Test
    fun `initial state shows loading`() = runTest {
        // Given
        every { mockGetIncomingWorkouts() } returns flow {
            emit(Result.Loading)
            // Don't emit success yet to keep in loading state
        }

        // When
        val viewModel = createViewModel()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.isLoading).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loads workouts successfully`() = runTest {
        // Given
        val workouts = TestFixtures.sampleWorkouts
        every { mockGetIncomingWorkouts() } returns flowOf(Result.Success(workouts))

        // When
        val viewModel = createViewModel()

        // Then
        viewModel.uiState.test {
            val finalState = expectMostRecentItem()
            assertThat(finalState.isLoading).isFalse()
            assertThat(finalState.workouts).hasSize(3)
            assertThat(finalState.error).isNull()
        }
    }

    @Test
    fun `shows error on failure`() = runTest {
        // Given
        every { mockGetIncomingWorkouts() } returns flowOf(Result.Error("Network error"))

        // When
        val viewModel = createViewModel()

        // Then
        viewModel.uiState.test {
            val finalState = expectMostRecentItem()
            assertThat(finalState.isLoading).isFalse()
            assertThat(finalState.error).isEqualTo("Network error")
        }
    }

    @Test
    fun `search filters workouts by name`() = runTest {
        // Given
        val workouts = TestFixtures.sampleWorkouts
        every { mockGetIncomingWorkouts() } returns flowOf(Result.Success(workouts))
        val viewModel = createViewModel()

        // When
        viewModel.search("HIIT")

        // Then
        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertThat(state.displayWorkouts).hasSize(1)
            assertThat(state.displayWorkouts[0].name).contains("HIIT")
            assertThat(state.searchQuery).isEqualTo("HIIT")
        }
    }

    @Test
    fun `search filters workouts by sport`() = runTest {
        // Given
        val workouts = TestFixtures.sampleWorkouts
        every { mockGetIncomingWorkouts() } returns flowOf(Result.Success(workouts))
        val viewModel = createViewModel()

        // When
        viewModel.search("RUNNING")

        // Then
        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertThat(state.displayWorkouts).hasSize(1)
            assertThat(state.displayWorkouts[0].name).isEqualTo("5K Tempo Run")
        }
    }

    @Test
    fun `empty search shows all workouts`() = runTest {
        // Given
        val workouts = TestFixtures.sampleWorkouts
        every { mockGetIncomingWorkouts() } returns flowOf(Result.Success(workouts))
        val viewModel = createViewModel()

        // First search for something
        viewModel.search("HIIT")

        // When - clear search
        viewModel.search("")

        // Then
        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertThat(state.displayWorkouts).hasSize(3)
            assertThat(state.filteredWorkouts).isNull()
        }
    }

    @Test
    fun `refresh reloads workouts`() = runTest {
        // Given
        every { mockGetIncomingWorkouts() } returns flowOf(Result.Success(emptyList()))
        val viewModel = createViewModel()

        // When
        viewModel.refresh()

        // Then
        coVerify(exactly = 2) { mockGetIncomingWorkouts() } // init + refresh
    }

    @Test
    fun `displayWorkouts returns filtered when search is active`() = runTest {
        // Given
        val workouts = TestFixtures.sampleWorkouts
        every { mockGetIncomingWorkouts() } returns flowOf(Result.Success(workouts))
        val viewModel = createViewModel()

        // When
        viewModel.search("Strength")

        // Then
        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertThat(state.filteredWorkouts).isNotNull()
            assertThat(state.displayWorkouts).hasSize(1)
        }
    }
}
