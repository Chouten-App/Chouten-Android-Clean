package com.chouten.app.presentation.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.chouten.app.presentation.ui.components.navigation.ChoutenNavigation
import com.chouten.app.presentation.ui.components.navigation.NavigationViewModel
import com.chouten.app.presentation.ui.screens.NavGraphs
import com.chouten.app.presentation.ui.theme.ChoutenTheme
import com.ramcosta.composedestinations.DestinationsNavHost

@Composable
fun ChoutenApp(
) {
    val navigationViewModel = hiltViewModel<NavigationViewModel>()
    val navigator = rememberNavController()

    ChoutenTheme {
        Scaffold(
            bottomBar = { ChoutenNavigation(navigator, navigationViewModel) }
        ) { paddingValues ->
            DestinationsNavHost(
                navController = navigator,
                navGraph = NavGraphs.root,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}