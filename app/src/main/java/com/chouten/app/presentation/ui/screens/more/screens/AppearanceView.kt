package com.chouten.app.presentation.ui.screens.more.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.chouten.app.common.MoreNavGraph
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@MoreNavGraph()
@Destination()
@Composable
fun AppearanceView(navigator: DestinationsNavigator) {
    Text("Appearance")
}