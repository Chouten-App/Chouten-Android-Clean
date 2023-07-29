package com.chouten.app.presentation.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.chouten.app.presentation.ui.components.navigation.ChoutenNavigation
import com.chouten.app.presentation.ui.components.navigation.NavigationViewModel
import com.chouten.app.presentation.ui.components.snackbar.SnackbarHost
import com.chouten.app.presentation.ui.screens.NavGraphs
import com.chouten.app.presentation.ui.theme.ChoutenTheme
import com.ramcosta.composedestinations.DestinationsNavHost
import kotlinx.coroutines.launch

@Composable
fun ChoutenApp(
) {
    val navigationViewModel = hiltViewModel<NavigationViewModel>()
    val navigator = rememberNavController()

    val appViewModel = hiltViewModel<ChoutenAppViewModel>()
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarState by appViewModel.snackbarChannel.collectAsState(null)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(snackbarState) {
        snackbarState?.let {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(it)
            }
        }
    }

    ChoutenTheme {
        Scaffold(
            snackbarHost = {
                SnackbarHost(
                    snackbarHostState
                )
            },
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