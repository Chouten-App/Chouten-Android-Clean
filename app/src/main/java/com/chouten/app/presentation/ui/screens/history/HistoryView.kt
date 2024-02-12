package com.chouten.app.presentation.ui.screens.history

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.chouten.app.common.LocalAppPadding
import com.chouten.app.common.Navigation
import com.chouten.app.presentation.ui.screens.destinations.InfoViewDestination
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Composable
@Destination(
    route = Navigation.HistoryRoute
)
fun HistoryView(
    viewModel: HistoryViewModel = hiltViewModel(), navigator: DestinationsNavigator
) {
    val history by viewModel.history.collectAsState(emptyMap())
    val coroutineScope = rememberCoroutineScope()
    LazyColumn(
        Modifier.padding(LocalAppPadding.current)
    ) {
        items(items = history.keys.toList()) { moduleId ->
            Log.d("HistoryView", "Going through keys. On ID $moduleId")
            history[moduleId]?.let { (moduleId, historyEntries) ->
                Column {
                    Text(text = moduleId, style = MaterialTheme.typography.titleSmall)
                    historyEntries.forEach { historyEntry ->
                        Text(text = historyEntry.entryTitle, modifier = Modifier.clickable {
                            navigator.navigate(
                                InfoViewDestination(
                                    title = historyEntry.entryTitle,
                                    url = historyEntry.parentUrl,
                                    resumeIndex = historyEntry.mediaIndex
                                )
                            )
                        })
                    }
                }

            }
        }
    }
}