package com.chouten.app.presentation.ui.screens.more.screens.module_screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseInExpo
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chouten.app.R
import com.chouten.app.common.LocalAppPadding
import com.chouten.app.common.MoreNavGraph
import com.chouten.app.common.UiText
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@MoreNavGraph
@Destination
@Composable
fun ModuleView(
    navigator: DestinationsNavigator,
    viewModel: ModuleViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTabIndex by rememberSaveable {
        mutableIntStateOf(0)
    }
    val tabs = setOf("Modules", "Repositories")
    val types by viewModel.moduleTypes.collectAsState()
    var selectedType by rememberSaveable { mutableStateOf<String?>(null) }

    fun <T> chipAnimationSpec(): FiniteAnimationSpec<T> =
        tween(150, easing = EaseInExpo)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(LocalAppPadding.current)
            .consumeWindowInsets(WindowInsets.systemBars),
        topBar = {
            TopAppBar(title = { Text(UiText.Literal("Modules").string()) },
                navigationIcon = {
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(tab) }
                    )
                }
            }
            when (selectedTabIndex) {
                0 -> {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(types.toList()) {
                                FilterChip(
                                    modifier = Modifier
                                        .animateContentSize(
                                            animationSpec = chipAnimationSpec()
                                        ),
                                    leadingIcon = {
                                        AnimatedVisibility(
                                            visible = selectedType == it,
                                            enter = fadeIn(chipAnimationSpec()),
                                            exit = fadeOut(chipAnimationSpec())
                                        ) {
                                            Icon(
                                                Icons.Filled.Check,
                                                null
                                            )

                                        }
                                    },
                                    onClick = {
                                        selectedType = if (selectedType == it) null else it
                                    },
                                    selected = selectedType == it,
                                    label = {
                                        Text(
                                            it.first().uppercase() + it.slice(1..<it.length)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                1 -> {
                    Text(
                        "Sorry. This is still a work-in-progress!",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}