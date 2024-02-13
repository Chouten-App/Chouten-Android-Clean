package com.chouten.app.presentation.ui.screens.history

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.chouten.app.common.LocalAppPadding
import com.chouten.app.common.Navigation
import com.chouten.app.domain.model.HistoryEntry
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
    var hiddenEntries by rememberSaveable { mutableStateOf<List<String>>(listOf()) }
    val coroutineScope = rememberCoroutineScope()
    LazyColumn(
        Modifier.padding(LocalAppPadding.current)
    ) {
        items(items = history.keys.toList()) { moduleId ->
            history[moduleId]?.let { (moduleId, historyEntries) ->
                if (historyEntries.isEmpty()) return@let
                val isModuleHidden = moduleId in hiddenEntries
                val chevronRotation by animateFloatAsState(targetValue = if (isModuleHidden) 90f else 270f)
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = moduleId, style = MaterialTheme.typography.titleLarge)
                        IconButton(onClick = {
                            if (isModuleHidden) hiddenEntries -= moduleId else hiddenEntries += moduleId
                        }) {
                            Icon(
                                Icons.Default.ChevronLeft,
                                contentDescription = null,
                                modifier = Modifier.rotate(chevronRotation)
                            )
                        }
                    }
                    AnimatedVisibility(
                        !isModuleHidden
                    ) {
                        Column {
                            historyEntries.forEach { historyEntry ->
                                if (viewModel.modules.find { it.id == historyEntry.moduleId }?.id == null) {
                                    return@forEach
                                }
//                                Text(text = historyEntry.entryTitle, modifier = Modifier.clickable {
//                                    navigator.navigate(
//                                        InfoViewDestination(
//                                            title = historyEntry.entryTitle,
//                                            url = historyEntry.parentUrl,
//                                            resumeIndex = historyEntry.mediaIndex
//                                        )
//                                    )
//                                })
                                HistoryEntryView(historyEntry = historyEntry)
                            }
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun HistoryEntryView(historyEntry: HistoryEntry) {
//    ListItem(headlineContent = { historyEntry.entryTitle }, supportingContent = {
//        Row() {
    Log.d("HistoryEntry", "${historyEntry.entryTitle} - ${historyEntry.entryImage}")
            AsyncImage(model = historyEntry.entryImage, contentDescription = null)
//        }
//    })
}