package com.amakaflow.companion.ui.screens.home

import app.cash.turbine.test
import com.amakaflow.companion.data.model.UserProfile
import com.amakaflow.companion.domain.Result
import com.amakaflow.companion.domain.repository.CompletionsResult
import com.amakaflow.companion.domain.usecase.completion.GetCompletionHistoryUseCase
import com.amakaflow.companion.domain.usecase.pairing.LoadPairingStateUseCase
import com.amakaflow.companion.domain.usecase.workout.GetPushedWorkoutsUseCase
import com.amakaflow.companion.simulation.SimulationSettings
import com.amakaflow.companion.test.MainDispatcherRule
import com.amakaflow.companion.test.TestFixtures
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockGetPushedWorkouts: GetPushedWorkoutsUseCase
    private lateinit var mockGetCompletionHistory: GetCompletionHistoryUseCase
    private lateinit var mockLoadPairingState: LoadPairingStateUseCase
    private lateinit var mockSimulationSettings: SimulationSettings

    private val userProfileFlow = MutableStateFlow<UserProfile?>(null)

    @Before
    fun setup() {
        mockGetPushedWorkouts = mockk()
        mockGetCompletionHistory = mockk()
        mockLoadPairingState = mockk()
        mockSimulationSettings = mockk(relaxed = true)

        // Default mock setup
        every { mockLoadPairingState.userProfile } returns userProfileFlow
        every { mockGetPushedWorkouts.getLocal() } returns flowOf(emptyList())
        coEvery { mockGetPushedWorkouts.getLocalSync() } returns emptyList()
    }

    private fun createViewModel(): HomeViewModel {
        return HomeViewModel(
            mockGetPushedWorkouts,
            mockGetCompletionHistory,
            mockLoadPairingState,
            mockSimulationSettings
        )
    }

    @Test
    fun `loads workouts successfully`() = runTest {
        // Given
        val workouts = TestFixtures.sampleWorkouts
        every { mockGetPushedWorkouts() } returns flowOf(Result.Success(workouts))
        every { mockGetCompletionHistory(any(), any()) } returns flowOf(
            Result.Success(CompletionsResult(emptyList(), 0))
        )

        // When
        val viewModel = createViewModel()

        // Then
        viewModel.uiState.test {
            val finalState = expectMostRecentItem()
            assertThat(finalState.isLoading).isFalse()
            assertThat(finalState.todayWorkouts).hasSize(3)
            assertThat(finalState.error).isNull()
        }
    }

    @Test
    fun `shows user name from pairing state`() = runTest {
        // Given
        val workouts = TestFixtures.sampleWorkouts
        every { mockGetPushedWorkouts() } returns flowOf(Result.Success(workouts))
        every { mockGetCompletionHistory(any(), any()) } returns flowOf(
            Result.Success(CompletionsResult(emptyList(), 0))
        )

        val viewModel = createViewModel()

        // When
        userProfileFlow.value = UserProfile(
            id = "user-123",
            email = "test@example.com",
            name = "Test User"
        )

        // Then
        viewModel.uiState.test {
            val finalState = expectMostRecentItem()
            assertThat(finalState.userName).isEqualTo("Test User")
        }
    }

    @Test
    fun `calculates weekly stats from completions`() = runTest {
        // Given
        every { mockGetPushedWorkouts() } returns flowOf(Result.Success(emptyList()))
        every { mockGetCompletionHistory(any(), any()) } returns flowOf(
            Result.Success(CompletionsResult(TestFixtures.sampleCompletions, 3))
        )

        // When
        val viewModel = createViewModel()

        // Then
        viewModel.uiState.test {
            val finalState = expectMostRecentItem()
            assertThat(finalState.weeklyStats.workoutCount).isEqualTo(3)
            assertThat(finalState.weeklyStats.totalDurationSeconds).isGreaterThan(0)
        }
    }

    @Test
    fun `falls back to local workouts on API error`() = runTest {
        // Given
        val localWorkouts = TestFixtures.sampleWorkouts
        every { mockGetPushedWorkouts() } returns flowOf(Result.Error("Network error"))
        coEvery { mockGetPushedWorkouts.getLocalSync() } returns localWorkouts
        every { mockGetCompletionHistory(any(), any()) } returns flowOf(
            Result.Success(CompletionsResult(emptyList(), 0))
        )

        // When
        val viewModel = createViewModel()

        // Then
        viewModel.uiState.test {
            val finalState = expectMostRecentItem()
            assertThat(finalState.todayWorkouts).hasSize(3)
            assertThat(finalState.error).isNull() // No error since we have local workouts
        }
    }

    @Test
    fun `shows error when API fails and no local workouts`() = runTest {
        // Given
        every { mockGetPushedWorkouts() } returns flowOf(Result.Error("Network error"))
        coEvery { mockGetPushedWorkouts.getLocalSync() } returns emptyList()
        every { mockGetCompletionHistory(any(), any()) } returns flowOf(
            Result.Success(CompletionsResult(emptyList(), 0))
        )

        // When
        val viewModel = createViewModel()

        // Then
        viewModel.uiState.test {
            val finalState = expectMostRecentItem()
            assertThat(finalState.error).isEqualTo("Network error")
        }
    }

    @Test
    fun `uses local workouts when API returns empty`() = runTest {
        // Given
        val localWorkouts = TestFixtures.sampleWorkouts
        every { mockGetPushedWorkouts() } returns flowOf(Result.Success(emptyList()))
        coEvery { mockGetPushedWorkouts.getLocalSync() } returns localWorkouts
        every { mockGetCompletionHistory(any(), any()) } returns flowOf(
            Result.Success(CompletionsResult(emptyList(), 0))
        )

        // When
        val viewModel = createViewModel()

        // Then
        viewModel.uiState.test {
            val finalState = expectMostRecentItem()
            assertThat(finalState.todayWorkouts).hasSize(3)
        }
    }
}
