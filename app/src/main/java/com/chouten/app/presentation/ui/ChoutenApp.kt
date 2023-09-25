package com.chouten.app.presentation.ui

import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.chouten.app.R
import com.chouten.app.common.LocalAppPadding
import com.chouten.app.common.UiText
import com.chouten.app.domain.model.SnackbarModel
import com.chouten.app.domain.proto.filepathDatastore
import com.chouten.app.presentation.ui.components.common.AppState
import com.chouten.app.presentation.ui.components.navigation.ChoutenNavigation
import com.chouten.app.presentation.ui.components.navigation.NavigationViewModel
import com.chouten.app.presentation.ui.components.snackbar.SnackbarHost
import com.chouten.app.presentation.ui.screens.NavGraphs
import com.chouten.app.presentation.ui.screens.destinations.InfoViewDestination
import com.chouten.app.presentation.ui.theme.ChoutenTheme
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.utils.currentDestinationAsState
import kotlinx.coroutines.launch

/**
 * The main entry point for the app's UI.
 * @param appViewModel The [ChoutenAppViewModel] instance to use for the app.
 * The [ChoutenAppViewModel] is passed to all screens in the app using a navigation dependency.
 */
@Composable
fun ChoutenApp(
    appState: AppState,
) {
    val navigationViewModel = hiltViewModel<NavigationViewModel>()
    val navigator = rememberNavController()

    val context = LocalContext.current

    // Check if the app has a module directory set
    val filePreferences by context.filepathDatastore.data.collectAsState(initial = null)

    // Initialise the SAF picker
    val safPicker = safPicker(appState)

    val snackbarLambda: (SnackbarModel) -> Unit = { snackbarModel ->
        appState.showSnackbar(snackbarModel)
    }

    LaunchedEffect(Unit, filePreferences?.CHOUTEN_ROOT_DIR) {
        if (filePreferences?.IS_CHOUTEN_MODULE_DIR_SET == true) {
            try {
                appState.viewModel.runAsync {
                    appState.viewModel.getModules()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                appState.showSnackbar(
                    SnackbarModel(
                        isError = true, message = e.message ?: "Unknown error"
                    )
                )
            }
        }
    }

    val navigationDestination by navigator.currentDestinationAsState()

    ChoutenTheme {
        Scaffold(snackbarHost = { SnackbarHost(appState.snackbarHostState) }, bottomBar = {
            AnimatedVisibility(visible = when (navigationDestination?.route) {
                InfoViewDestination.route -> {
                    false
                }

                else -> {
                    true
                }
            }, enter = slideInVertically { it }, exit = slideOutVertically { it }) {
                ChoutenNavigation(navigator, navigationViewModel)
            }
        }
        ) { paddingValues ->
            // Provide the padding values as a CompositionLocal
            // This allows us to use them in the screens


            CompositionLocalProvider(LocalAppPadding provides paddingValues) {
                if (filePreferences?.IS_CHOUTEN_MODULE_DIR_SET == false) {
                    AlertDialog(onDismissRequest = {
                        // We don't want to allow the user to dismiss the dialog
                        // So we do nothing here
                    }, title = {
                        Text(
                            text = UiText.StringRes(R.string.chouten_data_directory_not_set)
                                .string()
                        )
                    }, text = {
                        Column {
                            Text(text = UiText.StringRes(R.string.chouten_directory_alert).string())
                        }
                    }, confirmButton = {
                        Button(onClick = {
                            safPicker.launch(
                                // We want to use the Documents directory as the default
                                // If the user has a different directory set, we'll use that
                                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                                    ?.toUri()
                            )
                        }) {
                            Text(text = UiText.StringRes(R.string.select_directory).string())
                        }
                    }, dismissButton = {})
                } else DestinationsNavHost(navController = navigator,
                    navGraph = NavGraphs.root,
                    dependenciesContainerBuilder = {
                        dependency(appState.viewModel)
                        dependency(snackbarLambda)
                    })
            }
        }
    }
}

@Composable
fun safPicker(
    appState: AppState
): ManagedActivityResultLauncher<Uri?, Uri?> {
    val context = LocalContext.current

    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { result ->
        result?.let {
            appState.coroutineScope.launch {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                appState.viewModel.setModuleDirectory(context, it)
            }
        } ?: appState.coroutineScope.launch {
            appState.showSnackbar(
                SnackbarModel(
                    isError = true,
                    message = UiText.Literal("No Directory Selected").string(context),
                )
            )
        }
    }
}