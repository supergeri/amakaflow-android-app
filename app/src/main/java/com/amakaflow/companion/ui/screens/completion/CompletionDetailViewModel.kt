package com.amakaflow.companion.ui.screens.completion

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amakaflow.companion.data.model.WorkoutCompletionDetail
import com.amakaflow.companion.domain.Result
import com.amakaflow.companion.domain.usecase.completion.GetCompletionDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CompletionDetailUiState(
    val isLoading: Boolean = true,
    val completion: WorkoutCompletionDetail? = null,
    val error: String? = null
)

@HiltViewModel
class CompletionDetailViewModel @Inject constructor(
    private val getCompletionDetail: GetCompletionDetailUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val completionId: String = savedStateHandle.get<String>("completionId") ?: ""

    private val _uiState = MutableStateFlow(CompletionDetailUiState())
    val uiState: StateFlow<CompletionDetailUiState> = _uiState.asStateFlow()

    init {
        loadCompletionDetail()
    }

    private fun loadCompletionDetail() {
        if (completionId.isBlank()) {
            _uiState.update { it.copy(isLoading = false, error = "No completion ID provided") }
            return
        }

        viewModelScope.launch {
            getCompletionDetail(completionId).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                completion = result.data,
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
        loadCompletionDetail()
    }
}
