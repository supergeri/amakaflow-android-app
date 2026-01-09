package com.amakaflow.companion.ui.screens.home

import android.util.Log
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

private const val TAG = "HomeViewModel"

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
            Log.d(TAG, "loadData: Starting to fetch pushed workouts")
            // Load pushed workouts from android-companion endpoint
            workoutRepository.getPushedWorkouts().collect { result ->
                Log.d(TAG, "loadData: Got result: $result")
                when (result) {
                    is Result.Loading -> {
                        Log.d(TAG, "loadData: Loading...")
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Result.Success -> {
                        Log.d(TAG, "loadData: Success! Found ${result.data.size} workouts: ${result.data.map { it.name }}")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                todayWorkouts = result.data,
                                upcomingWorkouts = result.data,
                                error = null
                            )
                        }
                        Log.d(TAG, "loadData: Updated state, todayWorkouts=${_uiState.value.todayWorkouts.size}")
                    }
                    is Result.Error -> {
                        Log.e(TAG, "loadData: Error - ${result.message}")
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
