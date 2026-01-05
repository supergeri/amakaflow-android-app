package com.amakaflow.companion.data

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Configuration for E2E test mode.
 * When enabled, uses test headers instead of JWT authentication.
 */
@Singleton
class TestConfig @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("test_config", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TEST_MODE_ENABLED = "test_mode_enabled"
        private const val KEY_TEST_AUTH_SECRET = "test_auth_secret"
        private const val KEY_TEST_USER_ID = "test_user_id"
        private const val KEY_TEST_USER_EMAIL = "test_user_email"

        // Default test credentials for e2e testing
        const val DEFAULT_TEST_USER_EMAIL = "soopergeri+e2etest@gmail.com"
    }

    var isTestModeEnabled: Boolean
        get() = prefs.getBoolean(KEY_TEST_MODE_ENABLED, false)
        set(value) = prefs.edit { putBoolean(KEY_TEST_MODE_ENABLED, value) }

    var testAuthSecret: String?
        get() = prefs.getString(KEY_TEST_AUTH_SECRET, null)
        set(value) = prefs.edit { putString(KEY_TEST_AUTH_SECRET, value) }

    var testUserId: String?
        get() = prefs.getString(KEY_TEST_USER_ID, null)
        set(value) = prefs.edit { putString(KEY_TEST_USER_ID, value) }

    var testUserEmail: String?
        get() = prefs.getString(KEY_TEST_USER_EMAIL, DEFAULT_TEST_USER_EMAIL)
        set(value) = prefs.edit { putString(KEY_TEST_USER_EMAIL, value) }

    fun enableTestMode(authSecret: String, userId: String, userEmail: String = DEFAULT_TEST_USER_EMAIL) {
        testAuthSecret = authSecret
        testUserId = userId
        testUserEmail = userEmail
        isTestModeEnabled = true
    }

    fun disableTestMode() {
        isTestModeEnabled = false
        testAuthSecret = null
        testUserId = null
    }
}
