package com.chouten.app.presentation.ui.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.chouten.app.common.Navigation
import com.chouten.app.domain.model.SnackbarModel
import com.chouten.app.presentation.ui.ChoutenAppViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph

@Composable
@RootNavGraph(
    start = true
)
@Destination(
    route = Navigation.HomeRoute
)
fun HomeView(
    snackbarLambda: (SnackbarModel) -> Unit,
) {
    Text("Home!")
    var counter by rememberSaveable { mutableIntStateOf(1) }
    Button(onClick = {
        snackbarLambda(
                SnackbarModel(
                    message = "Counter: ${counter++}",
                    duration = SnackbarDuration.Indefinite
                )
            )
    }) {
        Text("Show snackbar")
    }
}