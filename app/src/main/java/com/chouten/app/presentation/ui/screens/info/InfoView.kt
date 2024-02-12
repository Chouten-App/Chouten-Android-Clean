package com.chouten.app.presentation.ui.screens.info

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.chouten.app.common.Navigation
import com.chouten.app.common.Resource
import com.chouten.app.domain.model.SnackbarModel
import com.chouten.app.presentation.ui.ChoutenAppViewModel
import com.chouten.app.presentation.ui.screens.destinations.HistoryViewDestination
import com.chouten.app.presentation.ui.screens.destinations.WatchViewDestination
import com.chouten.app.presentation.ui.screens.watch.WatchBundle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.net.URLDecoder

@OptIn(ExperimentalMaterial3Api::class)
@Destination(
    route = Navigation.InfoRoute
)
@Composable
fun InfoView(
    navigator: DestinationsNavigator,
    title: String,
    url: String,
    snackbarLambda: (SnackbarModel) -> Unit,
    appViewModel: ChoutenAppViewModel = hiltViewModel(),
    infoViewModel: InfoViewModel = hiltViewModel(),
    resumeIndex: Int?
) {
    val infoResults by infoViewModel.infoResults.collectAsState()
    val episodeList by infoViewModel.episodeList.collectAsState()

    val selectedSeason by infoViewModel.selectedSeason.collectAsState()
    var lastUrl by rememberSaveable { mutableStateOf(url) }
    val switchValue by infoViewModel.switchValue.collectAsState()
    val switchConfig by infoViewModel.switchConfig.collectAsState()

    LaunchedEffect(infoResults, selectedSeason, switchValue) {
        when (infoResults) {
            is Resource.Success -> {
                val urls = infoResults.data?.epListURLs ?: listOf()
                if (switchConfig?.options?.size == 2 && infoViewModel.cachedSwitchResults.firstOrNull()?.size == 2) {
                    // We will force the viewmodel to emit the cached results
                    infoViewModel.toggleSwitch(switchValue)
                    return@LaunchedEffect
                }

                selectedSeason?.let {
                    if ((lastUrl != it.url) && (infoViewModel.getMediaList().find { media ->
                            media.title == it.name
                        } == null)) {
                        infoViewModel.getInfo(title, it.url)
                        lastUrl = it.url
                    } else {
                        // Make sure that we don't reload the episodes if we already have them
                        if (episodeList is Resource.Uninitialized) infoViewModel.getEpisodes(
                            urls, 0
                        )
                    }
                } ?: infoViewModel.getEpisodes(urls, 0)
            }

            is Resource.Uninitialized -> {
                infoViewModel.getInfo(title, selectedSeason?.url ?: url)
            }

            is Resource.Error -> {
                Log.e("InfoView", "Error: ${infoResults.message}")
                snackbarLambda(
                    SnackbarModel(
                        isError = true,
                        message = infoResults.message ?: "Unknown error",
                    )
                )
            }

            else -> {}
        }
    }

    LaunchedEffect(episodeList) {
        if (infoViewModel.paginatedAll && infoViewModel.seasonCount <= 1 && (switchConfig?.options?.size == 2 && infoViewModel.cachedSwitchResults.firstOrNull()?.size == 2)) {
            // We have no further episodes to load
            // so don't need the webview anymore.
            infoViewModel.epListHandler.destroy()
        }

        if (episodeList is Resource.Success) {
            val urls = infoViewModel.getMediaList().find { it.title == selectedSeason?.name }?.list
                ?: episodeList.data?.getOrNull(0)?.list ?: listOf()
            resumeIndex?.let {
                urls.getOrNull(it)?.let { resumeUrl ->
                    val bundle = WatchBundle(
                        mediaUuid = infoViewModel.FILE_PREFIX.toString(),
                        selectedMediaIndex = it,
                        url = resumeUrl.url,
                        infoUrl = url,
                        mediaTitle = URLDecoder.decode(
                            title, "UTF-8"
                        )
                    )

                    appViewModel.runAsync {
                        infoViewModel.saveMediaBundle()
                    }

                    navigator.navigate(
                        WatchViewDestination(
                            bundle,
                        )
                    )
                }
            }
        }
    }

    val scrollState = rememberScrollState()
    val gradient = Brush.verticalGradient(
        0f to MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
        0.6f to MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        1f to MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
    )

    var isDescriptionBoxExpanded by rememberSaveable { mutableStateOf(false) }
    var descriptionLineCount by rememberSaveable { mutableIntStateOf(0) }

    val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()

    // Parallax effect for the banner
    val leftOffset =
        -(LocalConfiguration.current.screenWidthDp * 2) + (scrollState.value * 1.1).toInt()
    val offsetX by animateIntOffsetAsState(
        targetValue = if (scrollState.value >= 700) IntOffset(
            scrollState.value * 2, 0
        ) else IntOffset.Zero,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "offsetX"
    )
    val topBarOffset by animateIntOffsetAsState(
        targetValue = if (leftOffset >= 0) IntOffset.Zero else if (scrollState.value >= 700) IntOffset(
            leftOffset, 0
        ) else IntOffset(-(LocalConfiguration.current.screenWidthDp * 4), 0),
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "topBarOffset"
    )

    var isSeasonDropdown by rememberSaveable { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    ShimmerInfo(
        isLoading = infoResults is Resource.Loading,
        contentAfterLoading = {
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .fillMaxSize()
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .offset {
                                offsetX
                            }) {
                        SubcomposeAsyncImage(
                            modifier = Modifier.apply {
                                if (infoResults.data?.banner.isNullOrBlank()) Modifier.blur(2.dp)
                            },
                            model = infoResults.data?.banner ?: infoResults.data?.poster ?: "",
                            contentScale = ContentScale.Crop,
                            contentDescription = "${infoResults.data?.titles?.primary} Banner",
                        ) {
                            SubcomposeAsyncImageContent(
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(gradient)
                        )
                    }
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp)
                    ) {
                        Row(
                            Modifier
                                .padding(top = 180.dp, bottom = 8.dp)
                                .offset {
                                    offsetX
                                }, horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AsyncImage(
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(180.dp)
                                    .clip(MaterialTheme.shapes.medium),
                                model = infoResults.data?.poster ?: "",
                                contentScale = ContentScale.Crop,
                                contentDescription = "${infoResults.data?.titles?.primary} Poster",
                            )
                            Column(
                                Modifier.align(Alignment.Bottom),
                                verticalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                Text(
                                    infoResults.data?.altTitles?.firstOrNull() ?: "",
                                    color = MaterialTheme.colorScheme.onSurface.copy(
                                        0.7F
                                    ),
                                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    infoResults.data?.titles?.primary ?: "",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(
                                        top = 12.dp, bottom = 8.dp
                                    )
                                ) {
                                    Text(
                                        infoResults.data?.status ?: "",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "${infoResults.data?.totalMediaCount ?: ""} ${infoResults.data?.mediaType ?: ""}",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } // top info

                        Text(infoResults.data?.description ?: "",
                            modifier = Modifier
                                .clickable(
                                    enabled = true,
                                    onClickLabel = "Expand Description",
                                    role = Role.Switch
                                ) {
                                    isDescriptionBoxExpanded = !isDescriptionBoxExpanded
                                }
                                .animateContentSize(),
                            color = MaterialTheme.colorScheme.onSurface.copy(0.7F),
                            fontSize = 15.sp,
                            lineHeight = 16.sp,
                            maxLines = if (!isDescriptionBoxExpanded) 9 else Int.MAX_VALUE,
                            overflow = TextOverflow.Ellipsis,
                            onTextLayout = { textLayoutResult ->
                                descriptionLineCount = textLayoutResult.lineCount
                            })

                        AnimatedVisibility(
                            visible = descriptionLineCount >= 9, modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(
                                onClick = {
                                    isDescriptionBoxExpanded = !isDescriptionBoxExpanded
                                }, modifier = Modifier.wrapContentWidth(Alignment.End)
                            ) {
                                Text(
                                    if (!isDescriptionBoxExpanded) "Show More" else "Show Less",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.End,
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = switchConfig?.options != null
                        ) {
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Switch(checked = switchValue, onCheckedChange = {
                                    infoViewModel.viewModelScope.launch {
                                        infoViewModel.toggleSwitch()
                                    }
                                })
                                Text(
                                    switchConfig?.options?.get(
                                        if (SwitchConfig.isToggled(
                                                switchValue, switchConfig!!
                                            )
                                        ) 1 else 0
                                    ) ?: "",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(
                                        vertical = 6.dp, horizontal = 12.dp
                                    )
                                )
                            }
                        }

                        AnimatedVisibility(visible = !infoResults.data?.seasons.isNullOrEmpty()) {
                            ExposedDropdownMenuBox(expanded = isSeasonDropdown, onExpandedChange = {
                                isSeasonDropdown = it
                            }, modifier = Modifier.fillMaxWidth()) {
                                TextField(
                                    value = selectedSeason?.name ?: "Season 1",
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                            expanded = isSeasonDropdown
                                        )
                                    },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )

                                ExposedDropdownMenu(
                                    expanded = isSeasonDropdown,
                                    onDismissRequest = { isSeasonDropdown = false },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    infoResults.data?.seasons?.forEach { season ->
                                        DropdownMenuItem(text = {
                                            Text(
                                                season.name
                                            )
                                        },
                                            onClick = {
                                                isSeasonDropdown = false
                                                // reload the viewmodel with the new data
                                                coroutineScope.launch {
                                                    infoViewModel.changeSeason(
                                                        season
                                                    )
                                                }
                                            },
                                            modifier = Modifier
                                                .align(Alignment.CenterHorizontally)
                                                .fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            infoResults.data?.mediaType ?: "",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                        // TODO: dynamic maxheight based on screen
                        ShimmerEpisodes(isLoading = episodeList is Resource.Uninitialized || episodeList is Resource.Loading,
                            contentAfterLoading = {
                                LazyColumn(
                                    Modifier.heightIn(max = 600.dp),
                                    verticalArrangement = Arrangement.spacedBy(20.dp)
                                ) {
                                    itemsIndexed(
                                        items = infoViewModel.getMediaList()
                                            .find { it.title == selectedSeason?.name }?.list
                                            ?: episodeList.data?.getOrNull(0)?.list ?: listOf()
                                    ) { index, item ->
                                        if (resumeIndex != null) navigator.popBackStack(
                                            HistoryViewDestination, false
                                        )
                                        EpisodeItem(item,
                                            infoResults.data?.poster ?: "",
                                            Modifier.clickable {
                                                val bundle = WatchBundle(
                                                    mediaUuid = infoViewModel.FILE_PREFIX.toString(),
                                                    selectedMediaIndex = index,
                                                    url = item.url,
                                                    infoUrl = url,
                                                    mediaTitle = URLDecoder.decode(
                                                        title, "UTF-8"
                                                    )
                                                )

                                                // We must run using the appViewModel's coroutine scope
                                                // since the InfoViewModel will be destroyed when we
                                                // navigate to the WatchView
                                                appViewModel.runAsync {
                                                    infoViewModel.saveMediaBundle()
                                                }

                                                navigator.navigate(
                                                    WatchViewDestination(
                                                        bundle
                                                    )
                                                )
                                            })
                                    }
                                }
                            })
                    }
                }
            }
        },
    )

    TopAppBar(modifier = Modifier
        .fillMaxWidth()
        .offset {
            topBarOffset
        }, title = {
        Text(
            infoResults.data?.titles?.primary ?: "",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(
                vertical = 6.dp, horizontal = 12.dp
            )
        )
    }, colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surface,
    ),
        // This action is used to add padding for the title (Not clickable). It's a hacky solution but it works
        actions = {
            IconButton(onClick = {}, modifier = Modifier
                .clip(
                    CircleShape
                )
                .size(24.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }) { }) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Back",
                    tint = Color.Transparent,
                )
            }
        })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = systemBarsPadding.calculateTopPadding() * 2, end = 16.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        IconButton(
            onClick = {
                navigator.popBackStack()
            },
            modifier = Modifier
                .clip(
                    CircleShape
                )
                .size(24.dp)
                .background(MaterialTheme.colorScheme.primary),
        ) {
            Icon(
                modifier = Modifier
                    .padding(0.dp)
                    .size(20.dp),
                imageVector = Icons.Rounded.Close,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun EpisodeItem(
    item: InfoResult.MediaItem, imageAlternative: String, modifier: Modifier = Modifier
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(
                MaterialTheme.colorScheme.surfaceColorAtElevation(
                    3.dp
                )
            )
            .then(modifier)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(
                12.dp
            )
        ) {
            AsyncImage(
                modifier = Modifier
                    .width(160.dp)
                    .height(90.dp)
                    .clip(MaterialTheme.shapes.medium),
                model = item.image ?: imageAlternative,
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                contentDescription = "${item.title} Thumbnail",
            )
            Column(Modifier.heightIn(max = 90.dp)) {
                Column(
                    modifier = Modifier.fillMaxHeight(
                        0.7F
                    ), verticalArrangement = Arrangement.SpaceAround
                ) {
                    Text(
                        item.title ?: "Episode 1",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            bottom = 6.dp, end = 6.dp
                        )
                ) {
                    Text(
                        "Episode ${
                            item.number.toString().substringBefore(".0")
                        }", color = MaterialTheme.colorScheme.onSurface.copy(
                            0.7F
                        ), fontSize = 12.sp, fontWeight = FontWeight.SemiBold
                    )

                    // TODO: Add duration
//                    Text(
//                        item., color = MaterialTheme.colorScheme.onSurface.copy(
//                            0.7F
//                        ), fontSize = 12.sp, fontWeight = FontWeight.SemiBold
//                    )
                }
            }
        }
        if (item.description?.isNotBlank() == true) {
            Text(
                "${item.description}",
                color = MaterialTheme.colorScheme.onSurface.copy(
                    0.7F
                ),
                fontSize = 12.sp,
                lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(all = 12.dp)
            )
        }
    }
}