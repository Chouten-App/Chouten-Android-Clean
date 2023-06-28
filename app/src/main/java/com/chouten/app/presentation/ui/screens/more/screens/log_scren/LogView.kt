package com.chouten.app.presentation.ui.screens.more.screens.log_scren

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.chouten.app.common.MoreNavGraph
import com.chouten.app.common.Navigation
import com.chouten.app.domain.model.LogEntry
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.launch

@MoreNavGraph
@Destination(
    route = Navigation.LogRoute
)
@Composable
fun LogView(
    viewModel: LogViewModel = hiltViewModel()
) {
    val logEntries by viewModel.logs.collectAsState(emptyList())
    val coroutineScope = rememberCoroutineScope()
    LazyColumn {
        item {
            Card(
                modifier = Modifier.clickable {
                    coroutineScope.launch {
                        viewModel.insertLog(
                            LogEntry(
                                entryContent = "Test ${logEntries.size}",
                                entryHeader = "Test ${logEntries.size}",
                                entryTimestamp = System.currentTimeMillis()
                            )
                        )
                    }
                }
            ) {
                Text("Add Entry")
            }
        }
        items(items = logEntries) { logEntry ->
            Text(logEntry.entryContent, Modifier.clickable {
                coroutineScope.launch {
                    viewModel.deleteLogById(logEntry.id)
                }
            })
        }
    }
}