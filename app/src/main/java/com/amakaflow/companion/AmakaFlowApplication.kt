package com.amakaflow.companion

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AmakaFlowApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize any app-wide services here
    }
}
