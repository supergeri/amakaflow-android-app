package com.amakaflow.companion.ui.screens.workouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amakaflow.companion.data.model.Workout
import com.amakaflow.companion.data.repository.Result
import com.amakaflow.companion.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkoutsUiState(
    val isLoading: Boolean = true,
    val workouts: List<Workout> = emptyList(),
    val filteredWorkouts: List<Workout>? = null,
    val error: String? = null,
    val searchQuery: String = ""
) {
    val displayWorkouts: List<Workout>
        get() = filteredWorkouts ?: workouts
}

@HiltViewModel
class WorkoutsViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutsUiState())
    val uiState: StateFlow<WorkoutsUiState> = _uiState.asStateFlow()

    private var allWorkouts: List<Workout> = emptyList()

    init {
        loadWorkouts()
    }

    private fun loadWorkouts() {
        viewModelScope.launch {
            workoutRepository.getIncomingWorkouts().collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Result.Success -> {
                        allWorkouts = result.data
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                workouts = result.data,
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

    fun search(query: String) {
        _uiState.update { state ->
            if (query.isBlank()) {
                state.copy(
                    searchQuery = query,
                    filteredWorkouts = null
                )
            } else {
                val filtered = allWorkouts.filter { workout ->
                    workout.name.contains(query, ignoreCase = true) ||
                    workout.sport.name.contains(query, ignoreCase = true)
                }
                state.copy(
                    searchQuery = query,
                    filteredWorkouts = filtered
                )
            }
        }
    }

    fun refresh() {
        loadWorkouts()
    }
}
