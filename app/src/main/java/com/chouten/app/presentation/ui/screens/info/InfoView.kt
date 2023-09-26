package com.chouten.app.presentation.ui.screens.info

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import com.chouten.app.common.Navigation
import com.chouten.app.common.Resource
import com.chouten.app.domain.model.SnackbarModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dagger.hilt.android.internal.managers.FragmentComponentManager.findActivity

@Destination(
    route = Navigation.InfoRoute
)
@Composable
fun InfoView(
    navigator: DestinationsNavigator,
    title: String,
    url: String,
    snackbarLambda: (SnackbarModel) -> Unit
) {
    val context = LocalContext.current
    val infoViewModel: InfoViewModel = hiltViewModel(
        findActivity(context) as ViewModelStoreOwner,
    )

    val infoResults by infoViewModel.infoResults.collectAsState()
    val episodeList by infoViewModel.episodeList.collectAsState()

    LaunchedEffect(Unit, infoResults) {
        when (infoResults) {
            is Resource.Success -> {
                if (infoResults.data?.epListURLs?.isNotEmpty() == true) {
                    val urls = infoResults.data?.epListURLs ?: listOf()
                    infoViewModel.getEpisodes(urls, 0)
                }
            }

            is Resource.Uninitialized -> {
                infoViewModel.getInfo(title, url)
            }

            else -> {}
        }
    }

    LaunchedEffect(episodeList) {
        if (infoViewModel.paginatedAll) {
            // We have no further episodes to load
            // so don't need the webview anymore.
            infoViewModel.epListHandler.destroy()
        }
    }
}