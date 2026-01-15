package com.amakaflow.companion.ui.screens.pairing

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amakaflow.companion.domain.Result
import com.amakaflow.companion.domain.usecase.pairing.LoadPairingStateUseCase
import com.amakaflow.companion.domain.usecase.pairing.PairDeviceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject

private const val TAG = "PairingViewModel"

/**
 * JSON payload format for QR code pairing.
 */
@Serializable
private data class QRCodePayload(
    val type: String? = null,
    val version: Int? = null,
    val token: String? = null,
    val api_url: String? = null
)

private val jsonParser = Json { ignoreUnknownKeys = true }

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
    private val pairDevice: PairDeviceUseCase,
    private val loadPairingState: LoadPairingStateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PairingUiState())
    val uiState: StateFlow<PairingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            loadPairingState.isPaired.collect { isPaired ->
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
        Log.d(TAG, "QR code scanned raw: '$code'")
        Log.d(TAG, "QR code extracted token: '$token'")
        pair(token)
    }

    /**
     * Extract pairing token from QR code content.
     *
     * Supports formats:
     * - JSON payload: {"type":"amakaflow_pairing","version":1,"token":"xxx","api_url":"..."}
     * - amakaflow://pair?token=xxx or amakaflow://pair?code=xxx
     * - https://app.amakaflow.com/pair?token=xxx or ?code=xxx
     * - https://app.amakaflow.com/pair/xxx (code in path)
     * - Raw token string
     * - 6-character short code
     */
    private fun extractTokenFromQRCode(qrValue: String): String {
        return try {
            when {
                // Handle JSON payload format
                qrValue.trimStart().startsWith("{") -> {
                    extractTokenFromJson(qrValue) ?: qrValue
                }
                // Handle amakaflow:// deep links
                qrValue.startsWith("amakaflow://") -> {
                    val uri = Uri.parse(qrValue)
                    extractTokenFromUri(uri) ?: qrValue
                }
                // Handle https:// URLs
                qrValue.startsWith("https://") || qrValue.startsWith("http://") -> {
                    val uri = Uri.parse(qrValue)
                    extractTokenFromUri(uri) ?: qrValue
                }
                // Raw token or short code
                else -> qrValue
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse QR code, using raw value", e)
            qrValue
        }
    }

    /**
     * Extract token from JSON payload.
     */
    private fun extractTokenFromJson(jsonString: String): String? {
        return try {
            val payload = jsonParser.decodeFromString<QRCodePayload>(jsonString)
            if (payload.type == "amakaflow_pairing" && !payload.token.isNullOrEmpty()) {
                Log.d(TAG, "Extracted token from JSON payload: ${payload.token.take(8)}...")
                payload.token
            } else {
                Log.w(TAG, "JSON payload missing required fields: type=${payload.type}, hasToken=${payload.token != null}")
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse JSON payload", e)
            null
        }
    }

    /**
     * Extract token from a parsed URI.
     * Checks query parameters (token, code) and path segments.
     */
    private fun extractTokenFromUri(uri: Uri): String? {
        // Try common query parameter names
        uri.getQueryParameter("token")?.let { return it }
        uri.getQueryParameter("code")?.let { return it }

        // Try to extract from path (e.g., /pair/ABC123)
        val pathSegments = uri.pathSegments
        if (pathSegments.size >= 2 && pathSegments[0] == "pair") {
            val potentialCode = pathSegments[1]
            // Validate it looks like a pairing code (alphanumeric, 6 chars)
            if (potentialCode.length == 6 && potentialCode.all { it.isLetterOrDigit() }) {
                return potentialCode
            }
        }

        // Check last path segment as a fallback
        val lastSegment = pathSegments.lastOrNull()
        if (lastSegment != null && lastSegment.length == 6 && lastSegment.all { it.isLetterOrDigit() }) {
            return lastSegment
        }

        return null
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

            when (val result = pairDevice(code)) {
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
