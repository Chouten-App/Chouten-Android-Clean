package com.chouten.app.presentation.ui.screens.more

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.chouten.app.common.Navigation
import com.ramcosta.composedestinations.annotation.Destination

@Composable
@Destination(
    route = Navigation.MoreRoute
)
fun MoreView() {
    Text("More")
}