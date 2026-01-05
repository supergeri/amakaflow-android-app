package com.amakaflow.companion.ui.screens.pairing

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amakaflow.companion.data.repository.PairingRepository
import com.amakaflow.companion.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "PairingViewModel"

enum class PairingMode {
    QR_CODE,
    MANUAL_CODE
}

data class PairingUiState(
    val mode: PairingMode = PairingMode.QR_CODE,
    val manualCode: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isPaired: Boolean = false
)

@HiltViewModel
class PairingViewModel @Inject constructor(
    private val pairingRepository: PairingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PairingUiState())
    val uiState: StateFlow<PairingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            pairingRepository.isPaired.collect { isPaired ->
                _uiState.update { it.copy(isPaired = isPaired) }
            }
        }
    }

    fun setMode(mode: PairingMode) {
        _uiState.update { it.copy(mode = mode, error = null) }
    }

    fun updateManualCode(code: String) {
        // Only allow alphanumeric characters, max 6 chars
        val sanitized = code.uppercase().filter { it.isLetterOrDigit() }.take(6)
        _uiState.update { it.copy(manualCode = sanitized, error = null) }
    }

    fun onQRCodeScanned(code: String) {
        if (_uiState.value.isLoading) return

        // Extract token from URL if QR contains amakaflow:// or https:// URL
        val token = extractTokenFromQRCode(code)
        Log.d(TAG, "QR code scanned: $code -> extracted token: $token")
        pair(token)
    }

    /**
     * Extract pairing token from QR code content.
     *
     * Supports formats:
     * - amakaflow://pair?token=xxx
     * - https://app.amakaflow.com/pair?token=xxx
     * - Raw token string
     * - 6-character short code
     */
    private fun extractTokenFromQRCode(qrValue: String): String {
        return try {
            when {
                // Handle amakaflow:// deep links
                qrValue.startsWith("amakaflow://") -> {
                    val uri = Uri.parse(qrValue)
                    uri.getQueryParameter("token") ?: qrValue
                }
                // Handle https:// URLs
                qrValue.startsWith("https://") || qrValue.startsWith("http://") -> {
                    val uri = Uri.parse(qrValue)
                    uri.getQueryParameter("token") ?: qrValue
                }
                // Raw token or short code
                else -> qrValue
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse QR code as URL, using raw value", e)
            qrValue
        }
    }

    fun submitManualCode() {
        val code = _uiState.value.manualCode
        if (code.length != 6) {
            _uiState.update { it.copy(error = "Please enter a 6-character code") }
            return
        }
        pair(code)
    }

    private fun pair(code: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = pairingRepository.pair(code)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, isPaired = true) }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message,
                            manualCode = "" // Clear code on error
                        )
                    }
                }
                is Result.Loading -> {
                    // Already showing loading state
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
