package com.chouten.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import com.chouten.app.common.compareSemVer
import com.chouten.app.common.findActivity
import com.chouten.app.domain.model.AlertDialogModel
import com.chouten.app.domain.model.LogEntry
import com.chouten.app.domain.model.SnackbarModel
import com.chouten.app.domain.proto.moduleDatastore
import com.chouten.app.domain.use_case.log_use_cases.LogUseCases
import com.chouten.app.domain.use_case.module_use_cases.ModuleInstallEvent
import com.chouten.app.domain.use_case.module_use_cases.ModuleUseCases
import com.chouten.app.presentation.ui.ChoutenApp
import com.chouten.app.presentation.ui.ChoutenAppViewModel
import com.chouten.app.presentation.ui.components.common.AppState
import com.chouten.app.presentation.ui.components.common.rememberAppState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var appState: AppState

    @Inject
    lateinit var moduleUseCases: ModuleUseCases

    @Inject
    lateinit var logUseCases: LogUseCases

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Draw behind the navigation bar
        enableEdgeToEdge()

        ViewModelProvider(this)[ChoutenAppViewModel::class.java].runAsync {
            val modules = moduleUseCases.getModuleUris()
            moduleDatastore.data.firstOrNull()?.let {
                it.autoUpdatingModules.forEach { updatingModule ->
                    Log.d("MainActivity", "Attempting to Update $updatingModule")
                    withContext(Dispatchers.IO) {
                        val module = modules.find { model ->
                            model.id == updatingModule
                        } ?: return@withContext
                        val updateUrl = module.updateUrl

                        try {
                            logUseCases.insertLog(
                                LogEntry(
                                    entryHeader = getString(R.string.module_auto_update),
                                    entryContent = getString(
                                        R.string.module_autoupdate_start, module.name
                                    )
                                )
                            )
                            moduleUseCases.addModule(updateUrl.toUri()) { event ->
                                when (event) {
                                    is ModuleInstallEvent.PARSED -> {
                                        (event.module.version.compareSemVer(module.version) != 1).also { res ->
                                            if (!res) {
                                                appState.viewModel.runAsync {
                                                    logUseCases.insertLog(
                                                        LogEntry(
                                                            entryHeader = getString(R.string.module_auto_update),
                                                            entryContent = getString(
                                                                R.string.module_autoupdate_message,
                                                                module.name,
                                                                module.version,
                                                                event.module.version
                                                            )
                                                        )
                                                    )
                                                }
                                                appState.showSnackbar(
                                                    SnackbarModel(
                                                        isError = false,
                                                        message = getString(
                                                            R.string.module_autoupdate_message,
                                                            module.name,
                                                            module.version,
                                                            event.module.version
                                                        )
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    else -> {
                                        false
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            if (e is InterruptedException) return@withContext
                            e.printStackTrace()
                            appState.showSnackbar(
                                SnackbarModel(
                                    isError = true, message = getString(
                                        R.string.module_autoupdate_error, module.name
                                    )
                                )
                            )
                            logUseCases.insertLog(
                                LogEntry(
                                    entryHeader = getString(R.string.module_auto_update),
                                    entryContent = e.message ?: "Unknown Error"
                                )
                            )
                        }
                    }
                }
            }
        }

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
                        try {
                            appState.viewModel.installModule(
                                uri = uri
                            ) {
                                if (it is ModuleInstallEvent.PARSED) {
                                    // Can the app install the module?
                                    var hasPermission = false
                                    // Has the user dismissed/accepted the permission?
                                    var hasUserPermission = false

                                    appState.showAlertDialog(
                                        AlertDialogModel(
                                            icon = Icons.Default.Warning,
                                            title = getString(
                                                R.string.install_module_dialog_title,
                                                it.module.name
                                            ),
                                            message = getString(
                                                R.string.module_install_dialog_description,
                                                it.module.name
                                            ).trimIndent(),
                                            positiveButton = Pair(getString(R.string.install)) {
                                                hasUserPermission = true
                                                hasPermission = true
                                            },
                                            negativeButton = Pair(getString(R.string.cancel)) {
                                                hasUserPermission = true
                                                hasPermission = false
                                            }
                                        )
                                    )
                                    // We want to suspend until the user has accepted or declined the alert
                                    while (!hasUserPermission) {
                                        runBlocking {
                                            delay(100)
                                        }
                                    }
                                    !hasPermission
                                } else false
                            }
                        } catch (e: InterruptedException) {
                            Log.d("MainActivity", "User cancelled module installation")
                        } catch (e: Exception) {
                            e.printStackTrace()
                            appState.showSnackbar(
                                SnackbarModel(
                                    message = e.message ?: "Unknown error",
                                    actionLabel = "Dismiss",
                                    isError = true
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}