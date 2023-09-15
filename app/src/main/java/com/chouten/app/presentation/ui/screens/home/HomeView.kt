package com.chouten.app.presentation.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.chouten.app.common.Navigation
import com.chouten.app.domain.model.SnackbarModel
import com.chouten.app.presentation.ui.ChoutenAppViewModel
import com.chouten.app.presentation.ui.components.common.AppState
import com.chouten.app.presentation.ui.components.common.ModuleSelectorScaffold
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
    snackbarLambda: (SnackbarModel) -> Unit,
) {
   ModuleSelectorScaffold(Modifier, appViewModel) {
       Text("This is my home page")
   }
}