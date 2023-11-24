package com.chouten.app.presentation.ui.components.common

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import com.chouten.app.domain.model.AlertDialogModel
import com.chouten.app.domain.model.SnackbarModel
import com.chouten.app.presentation.ui.ChoutenAppViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch


/**
 * Remember the app state
 * Useful for persisting state across composables (e.g snackbar host state)
 * @param snackbarHostState The snackbar host state
 * @param coroutineScope The coroutine scope
 * @param viewModel The view model
 */
@Composable
fun rememberAppState(
    alertDialogState: MutableStateFlow<AlertDialogModel?> = remember { MutableStateFlow(null) },
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    viewModel: ChoutenAppViewModel = hiltViewModel()
) = remember(viewModel, coroutineScope, snackbarHostState) {
    AppState(
        alertState = alertDialogState,
        snackbarHostState = snackbarHostState,
        coroutineScope = coroutineScope,
        viewModel = viewModel
    )
}

/**
 * The app state
 * @param snackbarHostState The snackbar host state
 * @param coroutineScope The coroutine scope
 * @param viewModel The [ChoutenAppViewModel] view model
 */
class AppState(
    val alertState: MutableStateFlow<AlertDialogModel?>,
    val snackbarHostState: SnackbarHostState,
    val coroutineScope: CoroutineScope,
    val viewModel: ChoutenAppViewModel
) {
    fun showSnackbar(snackbarModel: SnackbarModel) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(snackbarModel)
        }
    }

    fun showAlertDialog(alertDialogModel: AlertDialogModel) {
        coroutineScope.launch {
            alertState.emit(alertDialogModel)
        }
    }
}