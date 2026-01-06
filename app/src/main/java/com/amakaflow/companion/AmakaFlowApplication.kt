package com.amakaflow.companion

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.amakaflow.companion.data.AppEnvironment
import com.amakaflow.companion.data.TestConfig
import com.amakaflow.companion.data.sync.CompletionSyncWorker
import com.amakaflow.companion.debug.DebugLog
import com.amakaflow.companion.debug.GlobalExceptionHandler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AmakaFlowApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    // Inject TestConfig early to initialize AppEnvironment.current from persisted value
    @Inject
    lateinit var testConfig: TestConfig

    override fun onCreate() {
        super.onCreate()

        // Install global exception handler for crash logging
        GlobalExceptionHandler.install()

        // Log app startup
        DebugLog.info("App started - v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})", "App")
        DebugLog.info("Build type: ${BuildConfig.BUILD_TYPE}", "App")

        // Force TestConfig initialization to set AppEnvironment.current
        // The init block in TestConfig handles this
        testConfig.hashCode()

        DebugLog.info("Environment: ${AppEnvironment.current.displayName}", "App")

        // Schedule periodic completion sync
        CompletionSyncWorker.schedulePeriodicSync(this)
        DebugLog.debug("Completion sync worker scheduled", "App")
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
