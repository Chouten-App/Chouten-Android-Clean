package com.chouten.app.presentation.ui.screens.search

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.chouten.app.common.Navigation
import com.ramcosta.composedestinations.annotation.Destination

@Composable
@Destination(
    route = Navigation.SearchRoute
)
fun SearchView() {
    Text(text = "Search!")
}