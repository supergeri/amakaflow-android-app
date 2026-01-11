package com.amakaflow.companion.ui.screens.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amakaflow.companion.BuildConfig
import com.amakaflow.companion.data.AppEnvironment
import com.amakaflow.companion.data.TestConfig
import com.amakaflow.companion.data.model.Workout
import com.amakaflow.companion.data.repository.PairingRepository
import com.amakaflow.companion.data.repository.WorkoutRepository
import com.amakaflow.companion.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "SettingsViewModel"

data class PendingWorkoutsState(
    val isLoading: Boolean = false,
    val workouts: List<Workout> = emptyList(),
    val error: String? = null,
    val isSynced: Boolean = false
)

data class SettingsUiState(
    val isPaired: Boolean = false,
    val userEmail: String? = null,
    val environment: AppEnvironment = AppEnvironment.PRODUCTION,
    val appVersion: String = BuildConfig.VERSION_NAME,
    val isTestModeEnabled: Boolean = false,
    val testUserEmail: String? = null,
    val pendingWorkouts: PendingWorkoutsState = PendingWorkoutsState()
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val pairingRepository: PairingRepository,
    private val workoutRepository: WorkoutRepository,
    private val testConfig: TestConfig
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        // Set initial state immediately, including test mode check
        val isTestMode = testConfig.isTestModeEnabled
        val actuallyPaired = pairingRepository.getToken() != null
        _uiState.update {
            it.copy(
                environment = testConfig.appEnvironment,
                isTestModeEnabled = isTestMode,
                testUserEmail = if (isTestMode) testConfig.testUserEmail else null,
                isPaired = actuallyPaired || isTestMode,
                userEmail = if (isTestMode) testConfig.testUserEmail else null
            )
        }
        observePairingState()
        // Auto-check pending workouts on init
        checkPendingWorkouts()
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

    fun checkPendingWorkouts() {
        Log.d(TAG, "checkPendingWorkouts: Starting check, environment=${AppEnvironment.current.displayName}, url=${AppEnvironment.current.mapperApiUrl}")
        viewModelScope.launch {
            _uiState.update {
                it.copy(pendingWorkouts = it.pendingWorkouts.copy(isLoading = true, error = null))
            }

            workoutRepository.getPushedWorkouts().collect { result ->
                Log.d(TAG, "checkPendingWorkouts: Got result: $result")
                when (result) {
                    is Result.Loading -> {
                        Log.d(TAG, "checkPendingWorkouts: Loading...")
                        _uiState.update {
                            it.copy(pendingWorkouts = it.pendingWorkouts.copy(isLoading = true))
                        }
                    }
                    is Result.Success -> {
                        Log.d(TAG, "checkPendingWorkouts: Success! Found ${result.data.size} workouts")
                        result.data.forEach { workout ->
                            Log.d(TAG, "  - ${workout.name} (${workout.id})")
                        }
                        _uiState.update {
                            it.copy(
                                pendingWorkouts = PendingWorkoutsState(
                                    isLoading = false,
                                    workouts = result.data,
                                    isSynced = true,
                                    error = null
                                )
                            )
                        }

                        // AMA-307: Confirm sync for each successfully fetched workout
                        result.data.forEach { workout ->
                            try {
                                workoutRepository.confirmSync(workout.id)
                                Log.d(TAG, "Confirmed sync for workout: ${workout.name}")
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to confirm sync for ${workout.name}: ${e.message}")
                            }
                        }
                    }
                    is Result.Error -> {
                        Log.e(TAG, "checkPendingWorkouts: Error - ${result.message}")
                        _uiState.update {
                            it.copy(
                                pendingWorkouts = PendingWorkoutsState(
                                    isLoading = false,
                                    workouts = emptyList(),
                                    isSynced = false,
                                    error = result.message
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
