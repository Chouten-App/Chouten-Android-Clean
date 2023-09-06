package com.chouten.app.presentation.ui

import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.chouten.app.R
import com.chouten.app.common.UiText
import com.chouten.app.domain.model.SnackbarModel
import com.chouten.app.domain.proto.FilePreferences
import com.chouten.app.domain.proto.filepathDatastore
import com.chouten.app.presentation.ui.components.common.AppState
import com.chouten.app.presentation.ui.components.navigation.ChoutenNavigation
import com.chouten.app.presentation.ui.components.navigation.NavigationViewModel
import com.chouten.app.presentation.ui.components.snackbar.SnackbarHost
import com.chouten.app.presentation.ui.screens.NavGraphs
import com.chouten.app.presentation.ui.theme.ChoutenTheme
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.navigation.dependency
import kotlinx.coroutines.launch

@Composable
fun ChoutenApp(
    appState: AppState,
) {
    val navigationViewModel = hiltViewModel<NavigationViewModel>()
    val navigator = rememberNavController()

    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    // Check if the app has a module directory set
    val filePreferences by context.filepathDatastore.data.collectAsState(initial = FilePreferences.DEFAULT)

    // Launch SAF Picker
    val pickerIntent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            // Get the path of the selected directory
            val uri = result.data?.data ?: return@rememberLauncherForActivityResult

            coroutineScope.launch {
                appState.viewModel.setModuleDirectory(context, uri)
            }
        }
    }

    val snackbarLambda: (SnackbarModel) -> Unit = { snackbarModel ->
        appState.showSnackbar(snackbarModel)
    }

    ChoutenTheme {
        Scaffold(
            snackbarHost = { SnackbarHost(appState.snackbarHostState) },
            bottomBar = { ChoutenNavigation(navigator, navigationViewModel) }
        ) { paddingValues ->
            if (!filePreferences.IS_CHOUTEN_MODULE_DIR_SET) {
                AlertDialog(onDismissRequest = {
                    // We don't want to allow the user to dismiss the dialog
                    // So we do nothing here
                }, title = {
                    Text(
                        text = UiText.StringRes(R.string.chouten_data_directory_not_set).string()
                    )
                }, text = {
                    Column {
                        Text(text = UiText.StringRes(R.string.chouten_directory_alert).string())
                        if (Build.VERSION.SDK_INT >= 29) {
                            Text(
                                text = UiText.StringRes(R.string.chouten_directory_alert_warning)
                                    .string(), fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }, confirmButton = {
                    Button(onClick = {
                        launcher.launch(pickerIntent)
                    }) {
                        Text(text = UiText.StringRes(R.string.select_directory).string())
                    }
                }, dismissButton = {
                    TextButton(onClick = {
                        coroutineScope.launch {
                            appState.viewModel.setModuleDirectory(context)
                        }
                    }) {
                        Text(text = UiText.StringRes(R.string.use_default).string())
                    }
                })
            } else DestinationsNavHost(
                navController = navigator,
                navGraph = NavGraphs.root,
                modifier = Modifier.padding(paddingValues),
                dependenciesContainerBuilder = {
                    dependency(snackbarLambda)
                }
            )
        }
    }
}