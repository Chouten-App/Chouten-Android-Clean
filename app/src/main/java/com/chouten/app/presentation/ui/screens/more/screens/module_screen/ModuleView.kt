package com.chouten.app.presentation.ui.screens.more.screens.module_screen

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutBack
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.chouten.app.R
import com.chouten.app.common.LocalAppPadding
import com.chouten.app.common.MoreNavGraph
import com.chouten.app.common.UiText
import com.chouten.app.domain.proto.moduleDatastore
import com.chouten.app.presentation.ui.ChoutenAppViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@MoreNavGraph
@Destination
@Composable
fun ModuleView(
    appViewModel: ChoutenAppViewModel,
    navigator: DestinationsNavigator,
    viewModel: ModuleViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var selectedTabIndex by rememberSaveable {
        mutableIntStateOf(0)
    }
    val tabs = setOf("Modules", "Repositories")

    val refreshState = rememberPullToRefreshState()

    if (refreshState.isRefreshing) {
        viewModel.refreshModules().also {
            it.invokeOnCompletion {
                refreshState.endRefresh()
            }
        }
    }

    val types by viewModel.moduleTypes.collectAsState()
    var selectedType by rememberSaveable { mutableStateOf<String?>(null) }

    val modules by viewModel.modules.collectAsState()
    val modulePreferences by context.moduleDatastore.data.collectAsState(null)

    val expandedCards = mutableStateListOf<Int>()

    Scaffold(modifier = Modifier
        .fillMaxSize()
        .padding(LocalAppPadding.current)
        .consumeWindowInsets(WindowInsets.systemBars), topBar = {
        TopAppBar(title = { Text(UiText.Literal("Modules").string()) }, navigationIcon = {
            Icon(Icons.Filled.ArrowBack,
                contentDescription = UiText.StringRes(R.string.back).string(),
                modifier = Modifier
                    .clickable {
                        coroutineScope.launch {
                            navigator.popBackStack()
                        }
                    }
                    .clip(MaterialTheme.shapes.small)
                    .padding(
                        horizontal = 12.dp
                    ))
        })
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(tab) })
                }
            }
            when (selectedTabIndex) {
                0 -> {
                    Box(
                        modifier = Modifier.nestedScroll(refreshState.nestedScrollConnection),

                        ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        ) {
                            Spacer(modifier = Modifier.height(12.dp))
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                itemsIndexed(modules) { index, model ->
                                    Card(
                                        onClick = {
                                            Log.d(
                                                "ModuleView",
                                                "${index in expandedCards}? $expandedCards"
                                            )
                                            if (index in expandedCards) {
                                                expandedCards -= index
                                            } else expandedCards += index
                                        }, elevation = CardDefaults.elevatedCardElevation(
                                            defaultElevation = 3.dp
                                        )
                                    ) {
                                        ListItem(leadingContent = {
                                            AsyncImage(
                                                model = model.metadata.icon,
                                                contentDescription = "${model.name} icon",
                                                contentScale = ContentScale.Fit,
                                                modifier = Modifier
                                                    .requiredSize(40.dp)
                                                    .clip(MaterialTheme.shapes.medium),
                                            )
                                        }, headlineContent = {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(model.name, fontWeight = FontWeight.Bold)
                                                Text("v${model.version}")
                                            }
                                        }, supportingContent = {
                                            AssistChip(onClick = {}, label = {
                                                Text(model.subtypes.firstOrNull()?.apply {
                                                    if (this.all { it.isUpperCase() || it.isWhitespace() }) this else this.first()
                                                        .uppercase() + this.slice(1..<this.length)
                                                } ?: "")
                                            })
                                        }, trailingContent = {
                                            Icon(
                                                Icons.Filled.ChevronLeft,
                                                null,
                                                modifier = Modifier.rotate(
                                                    animateFloatAsState(
                                                        if (index in expandedCards) 90f else 270f,
                                                        label = "Card Chevron"
                                                    ).value
                                                ),
                                            )
                                        })
                                        AnimatedVisibility(
                                            index in expandedCards,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    MaterialTheme.colorScheme.surfaceColorAtElevation(
                                                        3.dp
                                                    )
                                                )
                                                .padding(
                                                    horizontal = 8.dp,
                                                    vertical = animateDpAsState(if (index in expandedCards) 8.dp else 0.dp).value
                                                )
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(model.metadata.description ?: "")

                                                Row(
                                                    modifier = Modifier
                                                        .background(
                                                            MaterialTheme.colorScheme.surfaceColorAtElevation(
                                                                8.dp
                                                            ), shape = RoundedCornerShape(4.dp)
                                                        )
                                                        .padding(4.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    Text("Allow Module to Auto-Update?")
                                                    Checkbox(checked = modulePreferences?.autoUpdatingModules?.contains(
                                                        model.id
                                                    ) == true, onCheckedChange = {
                                                        appViewModel.runAsync {
                                                            context.moduleDatastore.updateData { prefs ->
                                                                prefs.copy(
                                                                    autoUpdatingModules = if (!it) prefs.autoUpdatingModules - model.id else (setOf(
                                                                        model.id
                                                                    ) + prefs.autoUpdatingModules).toList()
                                                                )
                                                            }
                                                        }
                                                    })
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        PullToRefreshContainer(
                            state = refreshState,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .alpha(
                                    animateFloatAsState(
                                        if (refreshState.progress > 0.1f) 1f else 0f,
                                        animationSpec = tween(
                                            durationMillis = 100,
                                            delayMillis = 0,
                                            easing = EaseInOutBack
                                        )
                                    ).value
                                )
                        )
                    }
                }

                1 -> {
                    Text(
                        "Sorry. This is still a work-in-progress!",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}