package com.chouten.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import com.chouten.app.common.findActivity
import com.chouten.app.presentation.ui.ChoutenApp
import com.chouten.app.presentation.ui.ChoutenAppViewModel
import com.chouten.app.presentation.ui.components.common.AppState
import com.chouten.app.presentation.ui.components.common.rememberAppState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var appState: AppState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Draw behind the navigation bar
        enableEdgeToEdge()

        setContent {
            val appState = rememberAppState(
                viewModel = ViewModelProvider(this)[ChoutenAppViewModel::class.java]
            )

            if (!::appState.isInitialized) {
                this.appState = appState
            }

            // On new intent must be manually called for the app to handle intents
            // on launch; otherwise, the app will not handle intents until the app
            // has an instance of the main activity
            onNewIntent(intent)
            ChoutenApp(appState)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(this@MainActivity, intent)
    }

    /**
     * This composable is used to observe the intent that started the app
     * If the intent is a module, we want to install it
     * @param context The context
     * @param intent The intent
     */
    private fun handleIntent(context: Context, intent: Intent?) {

        context.findActivity()?.let {
            // We want to get the intent data. If it's a module, we want to install it
            val supportedActions = arrayOf(
                Intent.ACTION_VIEW, Intent.ACTION_SEND
            )

            val data = intent?.data ?: intent?.clipData?.getItemAt(0)?.uri

            if (intent?.action in supportedActions) {
                data?.let { uri ->
                    appState.viewModel.runAsync {
                        appState.viewModel.installModule(
                            uri, appState::showSnackbar
                        )
                    }
                }
            }
        }
    }
}