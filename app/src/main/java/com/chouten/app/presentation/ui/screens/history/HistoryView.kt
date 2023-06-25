package com.chouten.app.presentation.ui.screens.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.chouten.app.common.Navigation
import com.chouten.app.domain.model.HistoryEntry
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.launch
import java.sql.Date

@Composable
@Destination(
    route = Navigation.HistoryRoute
)
fun HistoryView(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val history by viewModel.history.collectAsState(emptyList())
    val coroutineScope = rememberCoroutineScope()
    LazyColumn {
        item {
            ElevatedButton(onClick = {
                val entry = HistoryEntry(
                    entryTitle = "Test ${history.size}",
                    entryUrl = "https://www.google.com/${history.size}",
                    entryImage = "https://www.google.com",
                    entryDiscriminator = "Test",
                    entryProgress = Date(System.currentTimeMillis()),
                    entryDuration = Date(System.currentTimeMillis()),
                    entryLastUpdated = Date(System.currentTimeMillis())
                )
                coroutineScope.launch {
                    viewModel.insertHistoryEntry(entry)
                }
            }) {
                Text("Add Entry")
            }
        }

        items(items = history) { historyEntry ->
            Text(text = historyEntry.entryTitle, modifier = Modifier.clickable {
                coroutineScope.launch {
                    viewModel.updateHistoryEntry(historyEntry.copy(entryTitle = "Updated ${historyEntry.entryTitle}"))
                }
            })
        }
    }
}