package com.chouten.app.presentation.ui.screens.home

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.chouten.app.common.Navigation
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph

@Composable
@RootNavGraph(
    start = true
)
@Destination(
    route = Navigation.HomeRoute
)
fun HomeView() {
    Text("Home!")
}