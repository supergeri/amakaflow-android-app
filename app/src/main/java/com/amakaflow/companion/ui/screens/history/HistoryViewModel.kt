package com.amakaflow.companion.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amakaflow.companion.data.model.DateCategory
import com.amakaflow.companion.data.model.WorkoutCompletion
import com.amakaflow.companion.domain.Result
import com.amakaflow.companion.domain.usecase.completion.GetCompletionHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val completions: List<WorkoutCompletion> = emptyList(),
    val total: Int = 0,
    val error: String? = null
) {
    val groupedCompletions: List<Pair<DateCategory, List<WorkoutCompletion>>>
        get() = completions
            .groupBy { it.dateCategory }
            .toList()
            .sortedBy { (category, _) -> category.sortOrder }

    val hasMore: Boolean
        get() = completions.size < total

    val canLoadMore: Boolean
        get() = hasMore && !isLoading && !isLoadingMore
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getCompletionHistory: GetCompletionHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    companion object {
        private const val PAGE_SIZE = 50
    }

    init {
        loadCompletions()
    }

    private fun loadCompletions() {
        viewModelScope.launch {
            getCompletionHistory(limit = PAGE_SIZE, offset = 0).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                completions = result.data.completions,
                                total = result.data.total,
                                error = null
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                error = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun loadMore() {
        val currentState = _uiState.value
        if (!currentState.canLoadMore) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }

            val offset = currentState.completions.size
            getCompletionHistory(limit = PAGE_SIZE, offset = offset).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        // Already set isLoadingMore above
                    }
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoadingMore = false,
                                completions = it.completions + result.data.completions,
                                total = result.data.total,
                                error = null
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoadingMore = false,
                                error = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadCompletions()
    }
}
