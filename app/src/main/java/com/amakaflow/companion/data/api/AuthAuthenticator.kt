package com.amakaflow.companion.data.api

import com.amakaflow.companion.data.local.SecureStorage
import com.amakaflow.companion.data.model.TokenRefreshRequest
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * OkHttp Authenticator that handles 401 responses by attempting to refresh the JWT token.
 * If refresh fails, triggers re-pairing flow.
 */
@Singleton
class AuthAuthenticator @Inject constructor(
    private val secureStorage: SecureStorage,
    private val json: Json,
    private val baseUrlProvider: Provider<String>,
    private val authStateManager: AuthStateManager
) : Authenticator {

    @Volatile
    private var isRefreshing = false
    private val lock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        // Don't retry if we've already tried
        if (response.request.header("X-Retry-Auth") != null) {
            authStateManager.markNeedsReauth()
            return null
        }

        // Don't retry refresh endpoint
        if (response.request.url.encodedPath.contains("pairing/refresh")) {
            authStateManager.markNeedsReauth()
            return null
        }

        synchronized(lock) {
            // Check if another thread already refreshed
            val currentToken = secureStorage.getToken()
            val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")

            if (currentToken != null && currentToken != requestToken) {
                // Token was refreshed by another thread, retry with new token
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .header("X-Retry-Auth", "true")
                    .build()
            }

            // Attempt to refresh token
            val newToken = runBlocking { refreshToken() }

            return if (newToken != null) {
                response.request.newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .header("X-Retry-Auth", "true")
                    .build()
            } else {
                authStateManager.markNeedsReauth()
                null
            }
        }
    }

    private suspend fun refreshToken(): String? {
        if (isRefreshing) return null
        isRefreshing = true

        return try {
            val deviceId = secureStorage.getDeviceId() ?: return null

            // Create a simple OkHttp client without auth interceptor for refresh
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            val contentType = "application/json".toMediaType()
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrlProvider.get())
                .client(client)
                .addConverterFactory(json.asConverterFactory(contentType))
                .build()

            val api = retrofit.create(AmakaflowApi::class.java)
            val response = api.refreshToken(TokenRefreshRequest(deviceId = deviceId))

            if (response.isSuccessful && response.body() != null) {
                val newToken = response.body()!!.jwt
                secureStorage.saveToken(newToken)
                newToken
            } else {
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            isRefreshing = false
        }
    }
}

/**
 * Interface for managing authentication state across the app.
 * Allows the authenticator to signal when re-pairing is needed.
 */
interface AuthStateManager {
    fun markNeedsReauth()
}
