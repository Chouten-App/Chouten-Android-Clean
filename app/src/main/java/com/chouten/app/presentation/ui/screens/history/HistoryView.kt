package com.chouten.app.presentation.ui.screens.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.chouten.app.common.LocalAppPadding
import com.chouten.app.common.Navigation
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.launch

@Composable
@Destination(
    route = Navigation.HistoryRoute
)
fun HistoryView(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val history by viewModel.history.collectAsState(emptyList())
    val coroutineScope = rememberCoroutineScope()
    LazyColumn(
        Modifier.padding(LocalAppPadding.current)
    ) {
        items(items = history) { historyEntry ->
            Text(text = historyEntry.entryTitle, modifier = Modifier.clickable {
                coroutineScope.launch {
                    viewModel.updateHistoryEntry(historyEntry.copy(entryTitle = "Updated ${historyEntry.entryTitle}"))
                }
            })
        }
    }
}