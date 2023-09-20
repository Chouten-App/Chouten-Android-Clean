package com.chouten.app.presentation.ui.screens.more.screens.log_screen

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chouten.app.R
import com.chouten.app.common.MoreNavGraph
import com.chouten.app.common.Navigation
import com.chouten.app.common.UiText
import com.chouten.app.common.epochMillisToTime
import com.chouten.app.domain.model.LogEntry
import com.chouten.app.domain.model.SnackbarModel
import com.chouten.app.presentation.ui.screens.destinations.MoreViewDestination
import com.chouten.app.presentation.ui.theme.Typography
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@MoreNavGraph
@Destination(
    route = Navigation.LogRoute
)
@Composable
fun LogView(
    navigator: DestinationsNavigator,
    snackbarLambda: (SnackbarModel) -> Unit,
    viewModel: LogViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val logEntries by viewModel.logs.collectAsState(emptyList())
    val expandedEntries by viewModel.expanded.collectAsState(emptyList())

    val exportIntent: (Uri) -> Intent = remember {
        {
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_STREAM, it)
                    putExtra(Intent.EXTRA_TITLE, UiText.StringRes(R.string.logs).string(context))
                }, UiText.StringRes(R.string.share_app_logs).string(context)
            )
        }
    }

    val intentLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {}

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(UiText.StringRes(R.string.app_logs).string()) },
                navigationIcon = {
                IconButton(onClick = {
                    navigator.navigate(MoreViewDestination)
                }) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = UiText.StringRes(R.string.back).string()
                    )
                }
            }, actions = {
                AnimatedVisibility(
                    visible = logEntries.isNotEmpty()
                ) {
                    Row(
                        verticalAlignment = Alignment.Top
                    ) {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                viewModel.exportLogs(context)?.let {
                                    intentLauncher.launch(exportIntent(it))
                                }
                            }
                        }) {
                            Icon(
                                Icons.Filled.Share,
                                contentDescription = UiText.StringRes(R.string.share_logs).string(),
                            )
                        }

                        IconButton(onClick = {
                            coroutineScope.launch {
                                viewModel.deleteAllLogs()
                                snackbarLambda(
                                    SnackbarModel(
                                        message = UiText.StringRes(R.string.app_logs_removed)
                                            .string(context), duration = SnackbarDuration.Short
                                    )
                                )
                            }
                        }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = UiText.StringRes(R.string.remove_app_logs)
                                    .string()
                            )
                        }
                    }
                }
            })
        }, modifier = Modifier.consumeWindowInsets(WindowInsets.systemBars)
    ) { it ->
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(
                top = it.calculateTopPadding()
            )
        ) {
            items(
                items = logEntries
            ) { logEntry ->
                LogCard(logEntry, onExpandToggle = {
                    coroutineScope.launch {
                        viewModel.toggleExpanded(it.id)
                    }
                }) {
                    expandedEntries.contains(it.id)
                }
            }
        }
    }
}


@Composable
fun LogCard(
    logEntry: LogEntry, onExpandToggle: (LogEntry) -> Unit, isExpanded: (LogEntry) -> Boolean
) {

    val isExpanded by remember(logEntry.id) { derivedStateOf { isExpanded(logEntry) } }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        ),
    ) {
        Box(modifier = Modifier
            .clickable {
                onExpandToggle(logEntry)
            }
            .padding(16.dp)) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .wrapContentHeight()
                    ) {
                        Text(
                            logEntry.entryHeader,
                            style = Typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(.25f)
                            .wrapContentHeight(),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            epochMillisToTime(logEntry.entryTimestamp),
                            style = Typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Row {
                    Text(
                        logEntry.entryContent,
                        style = Typography.bodyMedium,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 4,
                        modifier = Modifier.animateContentSize(),
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun LogCardPreview() {
    var expanded by remember {
        mutableStateOf(false)
    }
    LogCard(LogEntry(
        id = 10,
        entryContent = "Entry content",
        entryHeader = "Entry header",
        entryTimestamp = System.currentTimeMillis(),
    ), onExpandToggle = {
        expanded = !expanded
    }) {
        expanded
    }
}