package com.chouten.app.presentation.ui.screens.info

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }

    val transition = rememberInfiniteTransition()
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
        )
    )

    background(
        brush = Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                Color(0xFF8F8B8B),
                MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
            ),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
        )
    ).onGloballyPositioned {
        size = it.size
    }
}

@Composable
fun ShimmerInfo(
    isLoading: Boolean, contentAfterLoading: @Composable () -> Unit, modifier: Modifier = Modifier
) {
    val gradient = Brush.verticalGradient(
        0f to MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
        0.6f to MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        1f to MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
    )

    if (isLoading) {
        Column(
            modifier = modifier.fillMaxSize()
        ) {
            Box(Modifier.fillMaxSize()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .shimmerEffect()
                ) {
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
                        Modifier.padding(top = 180.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(180.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .shimmerEffect(),
                        )
                        Column(
                            Modifier.align(Alignment.Bottom),
                            verticalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            // Alt title box
                            Box(
                                modifier = Modifier
                                    .width(50.dp)
                                    .height(20.dp)
                            )
                            // Title box
                            Box(
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(25.dp)
                                    .shimmerEffect()
                            )
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(
                                    top = 12.dp, bottom = 8.dp
                                )
                            ) {
                                // Status box
                                Box(
                                    modifier = Modifier
                                        .width(50.dp)
                                        .height(20.dp)
                                        .shimmerEffect()
                                )
                                // Episodes count box
                                Box(
                                    modifier = Modifier
                                        .width(45.dp)
                                        .height(20.dp)
                                )
                            }
                        }
                    } // top info
                    // Description box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                            .shimmerEffect()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                            .shimmerEffect()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                            .shimmerEffect()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(20.dp)
                            .shimmerEffect()
                    )

                    Box(
                        modifier = Modifier
                            .padding(vertical = 20.dp)
                            .width(80.dp)
                            .height(40.dp)
                            .shimmerEffect()
                    )
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(
                                12.dp
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(160.dp)
                                    .height(90.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .shimmerEffect(),
                            )
                            Column(Modifier.heightIn(max = 90.dp)) {
                                Column(
                                    modifier = Modifier.fillMaxHeight(
                                        0.7F
                                    ), verticalArrangement = Arrangement.SpaceAround
                                ) {

                                    // episode title box
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(20.dp)
                                            .shimmerEffect()
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

                                    // episode number box
                                    Box(
                                        modifier = Modifier
                                            .width(40.dp)
                                            .height(20.dp)
                                            .shimmerEffect()
                                    )
                                    // episode duration box
                                    Box(
                                        modifier = Modifier
                                            .width(40.dp)
                                            .height(20.dp)
                                            .shimmerEffect()
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp)
                                .shimmerEffect()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp)
                                .shimmerEffect()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(20.dp)
                                .shimmerEffect()
                        )
                    }
                }
            }
        }
    } else contentAfterLoading()

}

@Composable
fun ShimmerEpisodes(
    isLoading: Boolean, contentAfterLoading: @Composable () -> Unit, modifier: Modifier = Modifier
) {
    if (isLoading) {
        Column(
            modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(
                    12.dp
                )
            ) {
                Box(
                    modifier = Modifier
                        .width(160.dp)
                        .height(90.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .shimmerEffect(),
                )
                Column(Modifier.heightIn(max = 90.dp)) {
                    Column(
                        modifier = Modifier.fillMaxHeight(
                            0.7F
                        ), verticalArrangement = Arrangement.SpaceAround
                    ) {

                        // episode title box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp)
                                .shimmerEffect()
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

                        // episode number box
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(20.dp)
                                .shimmerEffect()
                        )
                        // episode duration box
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(20.dp)
                                .shimmerEffect()
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .shimmerEffect()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .shimmerEffect()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(20.dp)
                    .shimmerEffect()
            )
        }
    } else contentAfterLoading()
}