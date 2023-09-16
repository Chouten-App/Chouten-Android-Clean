package com.chouten.app.presentation.ui.screens.home

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import com.chouten.app.common.Navigation
import com.chouten.app.domain.model.SnackbarModel
import com.chouten.app.presentation.ui.ChoutenAppViewModel
import com.chouten.app.presentation.ui.components.common.ModuleSelectorWrapper
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@RootNavGraph(
    start = true
)
@Destination(
    route = Navigation.HomeRoute
)
fun HomeView(
    appViewModel: ChoutenAppViewModel,
    homeViewModel: HomeViewModel = hiltViewModel(),
    snackbarLambda: (SnackbarModel) -> Unit,
) {
    LaunchedEffect(Unit) {
        appViewModel.getModules()
    }
    ModuleSelectorWrapper(viewModel = appViewModel) {
   }
}