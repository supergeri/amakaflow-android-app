package com.amakaflow.companion

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.amakaflow.companion.data.TestConfig
import com.amakaflow.companion.data.sync.CompletionSyncWorker
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

        // Force TestConfig initialization to set AppEnvironment.current
        // The init block in TestConfig handles this
        testConfig.hashCode()

        // Schedule periodic completion sync
        CompletionSyncWorker.schedulePeriodicSync(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
