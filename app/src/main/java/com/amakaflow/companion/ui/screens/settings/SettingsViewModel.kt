package com.amakaflow.companion.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amakaflow.companion.BuildConfig
import com.amakaflow.companion.data.AppEnvironment
import com.amakaflow.companion.data.TestConfig
import com.amakaflow.companion.data.repository.PairingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isPaired: Boolean = false,
    val userEmail: String? = null,
    val environment: AppEnvironment = AppEnvironment.PRODUCTION,
    val appVersion: String = BuildConfig.VERSION_NAME,
    val isTestModeEnabled: Boolean = false,
    val testUserEmail: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val pairingRepository: PairingRepository,
    private val testConfig: TestConfig
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observePairingState()
        _uiState.update {
            it.copy(
                environment = testConfig.appEnvironment,  // Read persisted value
                isTestModeEnabled = testConfig.isTestModeEnabled,
                testUserEmail = testConfig.testUserEmail
            )
        }
    }

    private fun observePairingState() {
        viewModelScope.launch {
            pairingRepository.isPaired.collect { isPaired ->
                // In test mode, always show as paired
                val effectivelyPaired = isPaired || testConfig.isTestModeEnabled
                _uiState.update { it.copy(isPaired = effectivelyPaired) }
            }
        }
        viewModelScope.launch {
            pairingRepository.userProfile.collect { profile ->
                // In test mode, show test email
                val email = if (testConfig.isTestModeEnabled) {
                    testConfig.testUserEmail
                } else {
                    profile?.email
                }
                _uiState.update { it.copy(userEmail = email) }
            }
        }
    }

    fun setEnvironment(environment: AppEnvironment) {
        testConfig.appEnvironment = environment  // Persists and sets AppEnvironment.current
        _uiState.update { it.copy(environment = environment) }
    }

    fun enableTestMode(authSecret: String, userId: String) {
        testConfig.enableTestMode(authSecret, userId)
        _uiState.update {
            it.copy(
                isTestModeEnabled = true,
                testUserEmail = testConfig.testUserEmail,
                isPaired = true,
                userEmail = testConfig.testUserEmail
            )
        }
    }

    fun disableTestMode() {
        testConfig.disableTestMode()
        val actuallyPaired = pairingRepository.getToken() != null
        _uiState.update {
            it.copy(
                isTestModeEnabled = false,
                testUserEmail = null,
                isPaired = actuallyPaired
            )
        }
    }

    fun unpair() {
        pairingRepository.unpair()
        if (testConfig.isTestModeEnabled) {
            disableTestMode()
        }
    }
}
