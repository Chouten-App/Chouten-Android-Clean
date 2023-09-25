package com.chouten.app.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.chouten.app.R
import com.chouten.app.presentation.ui.screens.destinations.HistoryViewDestination
import com.chouten.app.presentation.ui.screens.destinations.HomeViewDestination
import com.chouten.app.presentation.ui.screens.destinations.MoreViewDestination
import com.chouten.app.presentation.ui.screens.destinations.SearchViewDestination
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec

object Navigation {
    // Since Compose Navigation is handling the navigation itself, we just
    // need to provide the destinations here so the navigation bar can
    // display them.

    const val HomeRoute = "home"
    const val SearchRoute = "search"
    const val InfoRoute = "info"
    const val HistoryRoute = "history"
    const val MoreRoute = "more"

    // Nested routes for the More screen
    const val AppearanceRoute = "appearance"
    const val LogRoute = "log"

    enum class Destination(
        val direction: DirectionDestinationSpec,
        val route: String,
        val label: UiText,
        val icon: ImageVector,
        val inactiveIcon: ImageVector = icon
    ) {
        Home(
            HomeViewDestination,
            HomeRoute,
            UiText.StringRes(R.string.home),
            icon = Icons.Filled.Home,
            inactiveIcon = Icons.Outlined.Home
        ),
        Search(
            SearchViewDestination,
            SearchRoute,
            UiText.StringRes(R.string.search),
            icon = Icons.Filled.Search,
            inactiveIcon = Icons.Outlined.Search
        ),
        History(
            HistoryViewDestination,
            HistoryRoute,
            UiText.StringRes(R.string.history),
            icon = Icons.Filled.History,
            inactiveIcon = Icons.Outlined.History
        ),
        More(
            MoreViewDestination,
            MoreRoute,
            UiText.StringRes(R.string.more),
            icon = Icons.Filled.MoreHoriz,
            inactiveIcon = Icons.Outlined.MoreHoriz
        )
    }
}

@RootNavGraph
@NavGraph
annotation class MoreNavGraph(
    val start: Boolean = false,
    val route: String = Navigation.MoreRoute
)