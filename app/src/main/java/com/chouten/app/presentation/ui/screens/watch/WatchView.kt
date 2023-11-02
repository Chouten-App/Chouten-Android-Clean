package com.chouten.app.presentation.ui.screens.watch

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.chouten.app.common.Navigation
import com.chouten.app.domain.model.SnackbarModel
import com.chouten.app.presentation.ui.ChoutenAppViewModel
import com.chouten.app.presentation.ui.screens.watch.WatchViewModel.Companion.STATUS
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class WatchBundle(
    val mediaUuid: String,
    val url: String,
    val selectedMediaIndex: Int,
    val mediaTitle: String,
)

@Composable
@Destination(
    route = Navigation.WatchRoute
)
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
fun WatchView(
    navigator: DestinationsNavigator,
    bundle: WatchBundle,
    snackbarLambda: (SnackbarModel) -> Unit,
    appViewModel: ChoutenAppViewModel
) {
    val context = LocalContext.current
    WatchView.FILE_PREFIX = bundle.mediaUuid
    val watchViewModel: WatchViewModel = hiltViewModel<WatchViewModel>()

    var hasExited by rememberSaveable { mutableStateOf(false) }

    val status by watchViewModel.savedStateHandle.getStateFlow(
        STATUS, WatchViewModelState.DEFAULT
    ).collectAsState()

    val selectedServer by rememberSaveable { mutableIntStateOf(0) }
    val selectedSource by rememberSaveable { mutableIntStateOf(0) }

    var servers by remember { mutableStateOf<List<WatchResult.ServerData>?>(null) }
    // Intent to launch player activity
    val playerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        hasExited = true
        navigator.navigateUp()
    }

    LaunchedEffect(status, servers) {
        while (status.error.isNotEmpty()) {
            val error = status.error.first()
            snackbarLambda(
                SnackbarModel(
                    message = error, isError = true
                )
            )
            watchViewModel.savedStateHandle[STATUS] = status.copy(error = status.error.drop(1))
        }

        if (!status.isServerSet) {
            watchViewModel.viewModelScope.launch {
                watchViewModel.getServers(
                    bundle,
                    bundle.selectedMediaIndex
                )
            }
        } else {
            if (servers == null) {
                watchViewModel.viewModelScope.launch {
                    context.cacheDir.resolve("${bundle.mediaUuid}_server.json").useLines { lines ->
                        val text = lines.joinToString("\n")
                        val result = Json.decodeFromString<List<WatchResult.ServerData>>(text)
                        servers = result
                    }
                }
            } else {
                val serverUrl =
                    servers?.getOrNull(selectedServer)?.list?.getOrNull(selectedSource)?.url
                watchViewModel.getSource(serverUrl ?: "")
            }
        }

        if (status.isSourceSet) {
            if (hasExited) return@LaunchedEffect

            playerLauncher.launch(Intent(context, ExoplayerActivity::class.java).apply {
                putExtra(ExoplayerActivity.BUNDLE, Json.encodeToString(bundle))
            })
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (!status.isSourceSet) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

object WatchView {
    var FILE_PREFIX: String = ""
}