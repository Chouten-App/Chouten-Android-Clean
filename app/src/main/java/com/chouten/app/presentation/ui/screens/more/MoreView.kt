package com.chouten.app.presentation.ui.screens.more

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.chouten.app.R
import com.chouten.app.common.MoreNavGraph
import com.chouten.app.presentation.ui.screens.destinations.AppearanceViewDestination
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Composable
@MoreNavGraph(
    start = true
)
@Destination(
    start = true
)
fun MoreView(navigator: DestinationsNavigator) {
    ListItem(
        headlineContent = { Text(stringResource(R.string.appearance)) },
        supportingContent = { Text(stringResource(R.string.appearance_page_description)) },
        trailingContent = {
            Icon(Icons.Filled.ChevronRight, contentDescription = stringResource(R.string.appearance_page_description))
        },
        modifier = Modifier.clickable {
            navigator.navigate(AppearanceViewDestination)
        }
    )
}