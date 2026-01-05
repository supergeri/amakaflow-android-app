package com.amakaflow.companion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.amakaflow.companion.data.TestConfig
import com.amakaflow.companion.ui.navigation.MainScreen
import com.amakaflow.companion.ui.theme.AmakaFlowTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var testConfig: TestConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AmakaFlowTheme {
                MainScreen(testConfig = testConfig)
            }
        }
    }
}
