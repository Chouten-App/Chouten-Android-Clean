package com.chouten.app.presentation.ui.screens.watch

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.chouten.app.common.formatMinSec
import com.chouten.app.presentation.ui.components.common.NoRippleInteractionSource


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PlayerControls(
    modifier: Modifier = Modifier,
    title: String?,
    episodeTitle: String?,
    currentModule: String?,
    onBackClick: () -> Unit,
    isVisible: () -> Boolean,
    isPlaying: () -> Boolean,
    isBuffering: () -> Boolean,
    onReplayClick: () -> Unit,
    onPauseToggle: () -> Unit,
    onForwardClick: () -> Unit,
    duration: () -> Long,
    currentTime: () -> Long,
    bufferPercentage: () -> Int,
    qualityLabel: () -> String,
    onSeekChanged: (timeMs: Float) -> Unit,
    onNextEpisode: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    val visible = remember(isVisible()) {
        isVisible()
    }

    AnimatedVisibility(
        modifier = modifier,
        visible = visible || isBuffering(),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Column(
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopControls(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateEnterExit(
                        enter = slideInVertically(initialOffsetY = { it }),
                        exit = slideOutVertically(targetOffsetY = { it })
                    ),
                title = title ?: "",
                episodeTitle = episodeTitle,
                currentModule = currentModule,
                qualityLabel = qualityLabel,
                onBackClick = onBackClick
            )

            CenterControls(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                isPlaying = isPlaying,
                isBuffering = isBuffering,
                onReplayClick = onReplayClick,
                onPauseToggle = onPauseToggle,
                onForwardClick = onForwardClick,
            )

            BottomControls(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateEnterExit(
                        enter = slideInVertically(initialOffsetY = { it }),
                        exit = slideOutVertically(targetOffsetY = { it })
                    ),
                duration = duration,
                currentTime = currentTime,
                bufferPercentage = bufferPercentage,
                onSeekChanged = onSeekChanged,
                onNextEpisode = onNextEpisode,
                onSettingsClick = onSettingsClick
            )

        }
    }
}

@Composable
fun TopControls(
    modifier: Modifier = Modifier,
    title: String?,
    episodeTitle: String?,
    qualityLabel: () -> String,
    currentModule: String?,
    onBackClick: () -> Unit
) {

    val quality = remember(qualityLabel()) {
        qualityLabel()
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
    ) {
        // back button
        IconButton(modifier = Modifier.size(40.dp), onClick = onBackClick) {
            Icon(
                modifier = Modifier.fillMaxSize(),
                imageVector = Icons.Rounded.ChevronLeft,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        // column with episode title and anime title
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .align(Alignment.CenterVertically)
        ) {
            if (episodeTitle != null) {
                Text(
                    text = episodeTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = Color.White.copy(alpha = 0.8f)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Module Name, with the resolution
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .align(Alignment.CenterVertically),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = currentModule ?: "No Module",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = quality,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = Color.White.copy(alpha = 0.8f)
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CenterControls(
    modifier: Modifier = Modifier,
    isPlaying: () -> Boolean,
    isBuffering: () -> Boolean,
    onReplayClick: () -> Unit,
    onPauseToggle: () -> Unit,
    onForwardClick: () -> Unit
) {

    val isVideoPlaying = remember(isPlaying()) {
        isPlaying()
    }

    Box(modifier = modifier.background(Color.Transparent)) {
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                modifier = Modifier.size(40.dp), onClick = {
                    onReplayClick()
                }, interactionSource = NoRippleInteractionSource()
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 10.dp, end = 10.dp, top = 15.dp, bottom = 10.dp)
                        .zIndex(1f),
                    text = "10",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Icon(
                    modifier = Modifier
                        .fillMaxSize(),
                    imageVector = Icons.Rounded.Replay,
                    contentDescription = "Replay",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(90.dp))

            Box {
                androidx.compose.animation.AnimatedVisibility(
                    visible = isBuffering(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(80.dp)
                            .zIndex(2f),
                        strokeWidth = 4.dp,
                        color = Color.White
                    )
                }

                //pause/play toggle button
                IconButton(modifier = Modifier.size(80.dp), onClick = onPauseToggle) {
                    Icon(
                        modifier = Modifier.fillMaxSize(),
                        imageVector = if (isVideoPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = "Replay",
                        tint = Color.White
                    )
                }

            }
            Spacer(modifier = Modifier.width(90.dp))

            //forward button
            IconButton(
                modifier = Modifier
                    .size(40.dp)
                    .graphicsLayer {
                        // flip the replay icon to make it a forward icon
                        rotationY = 180f
                    }, onClick = onForwardClick
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 10.dp, end = 10.dp, top = 15.dp, bottom = 10.dp)
                        .zIndex(1f)
                        .graphicsLayer { rotationY = -180f },
                    text = "10",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Icon(
                    modifier = Modifier.fillMaxSize(),
                    imageVector = Icons.Rounded.Replay,
                    contentDescription = "Replay",
                    tint = Color.White
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomControls(
    modifier: Modifier = Modifier,
    duration: () -> Long,
    currentTime: () -> Long,
    bufferPercentage: () -> Int,
    onSeekChanged: (timeMs: Float) -> Unit,
    onNextEpisode: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    val duration = remember(duration()) { duration() }
    val videoTime = remember(currentTime()) { currentTime() }
    val buffer = remember(bufferPercentage()) { bufferPercentage() }

    var isBeingDragged by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterVertically),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${videoTime.formatMinSec()} / ${duration.formatMinSec()}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                /**
                Row {
                IconButton(
                modifier = Modifier.size(40.dp),
                onClick = {},
                ) {
                Icon(
                modifier = Modifier.fillMaxSize(0.70F),
                imageVector = Icons.Rounded.Dns,
                contentDescription = "Replay",
                tint = Color.White
                )
                }

                IconButton(
                modifier = Modifier
                .size(40.dp)
                .rotate(-90F),
                onClick = {},
                ) {
                Icon(
                modifier = Modifier.fillMaxSize(0.70F),
                imageVector = Icons.Rounded.WebStories,
                contentDescription = "Replay",
                tint = Color.White
                )
                }

                IconButton(
                modifier = Modifier.size(40.dp),
                onClick = onSettingsClick,
                ) {
                Icon(
                modifier = Modifier.fillMaxSize(0.70F),
                imageVector = Icons.Rounded.Settings,
                contentDescription = "Replay",
                tint = Color.White
                )
                }

                IconButton(
                modifier = Modifier.size(40.dp),
                onClick = onNextEpisode,
                ) {
                Icon(
                modifier = Modifier.fillMaxSize(),
                imageVector = Icons.Rounded.FastForward,
                contentDescription = "Next Episode",
                tint = Color.White
                )
                }
                }
                 **/
            }

            val sliderInteractionSource = NoRippleInteractionSource()
            Box(
                modifier = Modifier
                    .height(20.dp)
                    // Use an interaction source so that the slider
                    // has some lenience when being dragged
                    .clickable {
                        sliderInteractionSource.tryEmit(
                            PressInteraction.Press(pressPosition = Offset.Unspecified)
                        )
                    }
            ) {
                // Slider to display buffered progress
                Slider(
                    modifier = Modifier
                        .fillMaxWidth(),
                    valueRange = 0f..duration.coerceAtLeast(0L).toFloat(),
                    value = buffer.toFloat(),
                    onValueChange = {},
                    thumb = {},
                    colors = SliderDefaults.colors(
                        thumbColor = Color.Transparent,
                        activeTrackColor = Color.White.copy(alpha = 0.5f),
                        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                    ),
                    enabled = false
                )

                // Slider to display video progress
                Slider(
                    // Create an alignment line using the start of the slider
                    // so that the <progress>/<duration> text is aligned with the slider
                    interactionSource = sliderInteractionSource,
                    modifier = Modifier
                        .fillMaxWidth(),
                    valueRange = 0f..duration.coerceAtLeast(0L).toFloat(),
                    value = videoTime.toFloat(),
                    onValueChange = {
                        isBeingDragged = true
                        onSeekChanged(it)
                    },
                    onValueChangeFinished = {
                        isBeingDragged = false
                    },
                    thumb = {
                        if (isBeingDragged) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(Color.White, MaterialTheme.shapes.small)
                            )
                        }
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                    ),
                    enabled = true
                )
            }
        }
    }
}