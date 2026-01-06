package com.amakaflow.companion.data.repository

import android.os.Build
import android.provider.Settings
import com.amakaflow.companion.BuildConfig
import com.amakaflow.companion.data.api.AmakaflowApi
import com.amakaflow.companion.data.local.SecureStorage
import com.amakaflow.companion.data.model.*
import com.amakaflow.companion.debug.DebugLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "Pairing"

sealed class PairingError : Exception() {
    data class InvalidCode(override val message: String) : PairingError()
    data object CodeExpired : PairingError()
    data object InvalidResponse : PairingError()
    data class ServerError(val code: Int) : PairingError()
    data object TokenStorageFailed : PairingError()
}

@Singleton
class PairingRepository @Inject constructor(
    private val api: AmakaflowApi,
    private val secureStorage: SecureStorage,
    private val json: Json
) {
    private val _isPaired = MutableStateFlow(false)
    val isPaired: StateFlow<Boolean> = _isPaired.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _needsReauth = MutableStateFlow(false)
    val needsReauth: StateFlow<Boolean> = _needsReauth.asStateFlow()

    init {
        // Check if already paired on init
        _isPaired.value = secureStorage.getToken() != null
        loadProfile()
    }

    private fun loadProfile() {
        secureStorage.getUserProfile()?.let { profileJson ->
            try {
                _userProfile.value = json.decodeFromString<UserProfile>(profileJson)
            } catch (e: Exception) {
                // Invalid profile, clear it
                secureStorage.clearUserProfile()
            }
        }
    }

    fun getToken(): String? = secureStorage.getToken()

    fun getOrCreateDeviceId(): String {
        return secureStorage.getDeviceId() ?: run {
            val deviceId = UUID.randomUUID().toString()
            secureStorage.saveDeviceId(deviceId)
            deviceId
        }
    }

    suspend fun pair(code: String): Result<PairingResponse> {
        return try {
            val isShortCode = code.length == 6
            DebugLog.info("Attempting to pair with ${if (isShortCode) "short code" else "token"}: ${code.take(4)}...", TAG)

            val request = PairingRequest(
                token = if (isShortCode) null else code,
                shortCode = if (isShortCode) code.uppercase() else null,
                deviceInfo = DeviceInfo(
                    device = getDeviceName(),
                    os = "Android ${Build.VERSION.RELEASE}",
                    appVersion = BuildConfig.VERSION_NAME,
                    deviceId = getOrCreateDeviceId()
                )
            )

            val response = api.pair(request)
            DebugLog.debug("Pairing API response: ${response.code()}", TAG)

            when {
                response.isSuccessful && response.body() != null -> {
                    val pairingResponse = response.body()!!
                    secureStorage.saveToken(pairingResponse.jwt)
                    pairingResponse.profile?.let { profile ->
                        secureStorage.saveUserProfile(json.encodeToString(profile))
                        _userProfile.value = profile
                        DebugLog.success("Paired successfully as ${profile.email}", TAG)
                    } ?: DebugLog.success("Paired successfully (no profile)", TAG)
                    _isPaired.value = true
                    _needsReauth.value = false
                    Result.Success(pairingResponse)
                }
                response.code() == 400 -> {
                    DebugLog.error("Pairing failed: Invalid code", TAG)
                    Result.Error("Invalid code", 400)
                }
                response.code() == 410 -> {
                    DebugLog.error("Pairing failed: Code expired", TAG)
                    Result.Error("Code has expired. Please generate a new one.", 410)
                }
                else -> {
                    DebugLog.error("Pairing failed: Server error ${response.code()}", TAG)
                    Result.Error("Server error: ${response.code()}", response.code())
                }
            }
        } catch (e: Exception) {
            DebugLog.error("Pairing exception: ${e.message}", TAG)
            DebugLog.error(e, TAG)
            Result.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun refreshToken(): Boolean {
        DebugLog.info("Refreshing token...", TAG)
        return try {
            val response = api.refreshToken(
                TokenRefreshRequest(deviceId = getOrCreateDeviceId())
            )

            if (response.isSuccessful && response.body() != null) {
                secureStorage.saveToken(response.body()!!.jwt)
                _needsReauth.value = false
                DebugLog.success("Token refreshed successfully", TAG)
                true
            } else {
                if (response.code() == 401) {
                    _needsReauth.value = true
                    DebugLog.warning("Token refresh failed: needs re-auth", TAG)
                } else {
                    DebugLog.error("Token refresh failed: ${response.code()}", TAG)
                }
                false
            }
        } catch (e: Exception) {
            DebugLog.error("Token refresh exception: ${e.message}", TAG)
            false
        }
    }

    fun markAuthInvalid() {
        DebugLog.warning("Auth marked as invalid", TAG)
        _needsReauth.value = true
    }

    fun unpair() {
        DebugLog.info("Unpiring device...", TAG)
        secureStorage.clearToken()
        secureStorage.clearUserProfile()
        _isPaired.value = false
        _userProfile.value = null
        DebugLog.success("Device unpaired", TAG)
    }

    private fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        val model = Build.MODEL
        return if (model.startsWith(manufacturer, ignoreCase = true)) {
            model
        } else {
            "$manufacturer $model"
        }
    }
}
