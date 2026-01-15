package com.amakaflow.companion.domain.repository

import com.amakaflow.companion.data.model.PairingResponse
import com.amakaflow.companion.data.model.UserProfile
import com.amakaflow.companion.domain.Result
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository interface for device pairing operations.
 * Part of the domain layer - defines the contract for pairing state management.
 */
interface PairingRepository {
    /**
     * StateFlow indicating whether the device is currently paired.
     */
    val isPaired: StateFlow<Boolean>

    /**
     * StateFlow containing the user's profile if paired, null otherwise.
     */
    val userProfile: StateFlow<UserProfile?>

    /**
     * StateFlow indicating whether re-authentication is required.
     */
    val needsReauth: StateFlow<Boolean>

    /**
     * Pair the device using a 6-character short code or full token.
     */
    suspend fun pair(code: String): Result<PairingResponse>

    /**
     * Refresh the authentication token.
     * Returns true if successful.
     */
    suspend fun refreshToken(): Boolean

    /**
     * Get the current auth token, or null if not paired.
     */
    fun getToken(): String?

    /**
     * Get the device ID, creating one if it doesn't exist.
     */
    fun getOrCreateDeviceId(): String

    /**
     * Mark the current authentication as invalid.
     * This will set needsReauth to true.
     */
    fun markAuthInvalid()

    /**
     * Unpair the device and clear all stored credentials.
     */
    fun unpair()
}
