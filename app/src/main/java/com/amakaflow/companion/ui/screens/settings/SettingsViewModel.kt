package com.amakaflow.companion.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amakaflow.companion.BuildConfig
import com.amakaflow.companion.data.AppEnvironment
import com.amakaflow.companion.data.repository.PairingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isPaired: Boolean = false,
    val userEmail: String? = null,
    val environment: AppEnvironment = AppEnvironment.PRODUCTION,
    val appVersion: String = BuildConfig.VERSION_NAME
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val pairingRepository: PairingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observePairingState()
        _uiState.update { it.copy(environment = AppEnvironment.current) }
    }

    private fun observePairingState() {
        viewModelScope.launch {
            pairingRepository.isPaired.collect { isPaired ->
                _uiState.update { it.copy(isPaired = isPaired) }
            }
        }
        viewModelScope.launch {
            pairingRepository.userProfile.collect { profile ->
                _uiState.update { it.copy(userEmail = profile?.email) }
            }
        }
    }

    fun setEnvironment(environment: AppEnvironment) {
        AppEnvironment.current = environment
        _uiState.update { it.copy(environment = environment) }
    }

    fun unpair() {
        pairingRepository.unpair()
    }
}
