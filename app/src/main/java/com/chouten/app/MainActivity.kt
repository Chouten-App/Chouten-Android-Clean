package com.chouten.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.chouten.app.presentation.ui.ChoutenApp
import com.chouten.app.presentation.ui.components.common.rememberAppState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Draw behind the navigation bar
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val appState = rememberAppState()
            ChoutenApp(appState)
        }
    }
}