package com.amakaflow.companion.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amakaflow.companion.data.model.WeeklySummary
import com.amakaflow.companion.data.model.Workout
import com.amakaflow.companion.data.repository.PairingRepository
import com.amakaflow.companion.data.repository.Result
import com.amakaflow.companion.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val userName: String? = null,
    val todayWorkouts: List<Workout> = emptyList(),
    val upcomingWorkouts: List<Workout> = emptyList(),
    val weeklyStats: WeeklySummary = WeeklySummary(0, 0, 0),
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val pairingRepository: PairingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
        loadWeeklyStats()
        observeUserProfile()
    }

    private fun observeUserProfile() {
        viewModelScope.launch {
            pairingRepository.userProfile.collect { profile ->
                _uiState.update { it.copy(userName = profile?.name) }
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            workoutRepository.getIncomingWorkouts().collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                upcomingWorkouts = result.data,
                                todayWorkouts = result.data.take(2), // Mock today's workouts
                                error = null
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    private fun loadWeeklyStats() {
        viewModelScope.launch {
            workoutRepository.getCompletions(limit = 50, offset = 0).collect { result ->
                when (result) {
                    is Result.Success -> {
                        val weeklyStats = WeeklySummary.fromCompletions(result.data.completions)
                        _uiState.update { it.copy(weeklyStats = weeklyStats) }
                    }
                    else -> { /* Keep default stats on error */ }
                }
            }
        }
    }

    fun refresh() {
        loadData()
        loadWeeklyStats()
    }
}
