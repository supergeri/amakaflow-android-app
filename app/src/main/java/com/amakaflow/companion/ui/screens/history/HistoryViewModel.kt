package com.amakaflow.companion.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amakaflow.companion.data.model.DateCategory
import com.amakaflow.companion.data.model.WorkoutCompletion
import com.amakaflow.companion.data.repository.Result
import com.amakaflow.companion.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val isLoading: Boolean = true,
    val completions: List<WorkoutCompletion> = emptyList(),
    val error: String? = null
) {
    val groupedCompletions: List<Pair<DateCategory, List<WorkoutCompletion>>>
        get() = completions
            .groupBy { it.dateCategory }
            .toList()
            .sortedBy { (category, _) -> category.sortOrder }
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadCompletions()
    }

    private fun loadCompletions() {
        viewModelScope.launch {
            workoutRepository.getCompletions().collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                completions = result.data,
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

    fun refresh() {
        loadCompletions()
    }
}
