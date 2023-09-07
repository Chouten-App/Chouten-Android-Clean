package com.chouten.app.presentation.ui.screens.more.screens.appearance_screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chouten.app.R
import com.chouten.app.common.MoreNavGraph
import com.chouten.app.common.UiText
import com.chouten.app.common.isDarkTheme
import com.chouten.app.domain.proto.AppearancePreferences
import com.chouten.app.presentation.ui.components.common.VerticalDivider
import com.chouten.app.presentation.ui.components.preferences.PreferenceEnumPopup
import com.chouten.app.presentation.ui.components.preferences.PreferenceToggle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@MoreNavGraph()
@Destination()
@Composable
fun AppearanceView(
    navigator: DestinationsNavigator, viewModel: AppearanceViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val preferencesFlow = remember(viewModel.selectedAppearance) {
        viewModel.getAppearancePreferences(context)
    }

    LaunchedEffect(viewModel.selectedAppearance) {
        preferencesFlow.collectLatest {
            viewModel.selectedAppearance = it.appearance
            viewModel.isDynamicColor = it.isDynamicColor
            viewModel.isAmoled = it.isAmoled
        }
    }

    val isDynamicColor = rememberSaveable(viewModel.isDynamicColor) {
        mutableStateOf(viewModel.isDynamicColor)
    }

    val selectedTheme = rememberSaveable(viewModel.selectedAppearance) {
        mutableStateOf(viewModel.selectedAppearance)
    }

    val isAmoled = rememberSaveable(viewModel.isAmoled) {
        mutableStateOf(viewModel.isAmoled)
    }

    val isDarkTheme = isSystemInDarkTheme()

    val switchIcon: Pair<ImageVector, UiText.StringRes> =
        remember(viewModel.selectedAppearance, isDarkTheme) {
            val lightIcon = Icons.Filled.LightMode
            val darkIcon = Icons.Filled.DarkMode

            when (viewModel.selectedAppearance) {
                AppearancePreferences.Appearance.LIGHT -> Pair(
                    lightIcon, UiText.StringRes(R.string.light_theme)
                )

                AppearancePreferences.Appearance.DARK -> Pair(
                    darkIcon, UiText.StringRes(R.string.dark_theme)
                )

                else -> {
                    // If the device is in dark mode, show the dark theme icon.
                    // Otherwise, show the light theme icon.
                    if (isDarkTheme) {
                        Pair(
                            darkIcon, UiText.StringRes(R.string.dark_theme)
                        )
                    } else {
                        Pair(
                            lightIcon, UiText.StringRes(R.string.light_theme)
                        )
                    }
                }
            }
        }


    Scaffold(modifier = Modifier
        .fillMaxSize()
        .consumeWindowInsets(WindowInsets.systemBars),
        topBar = {
            TopAppBar(title = { Text(UiText.StringRes(R.string.appearance).string()) },
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
        }) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {

            PreferenceToggle(headlineContent = {
                Text(UiText.StringRes(R.string.use_dynamic_theming).string())
            }, supportingContent = {
                Text(UiText.StringRes(R.string.use_material_you_dynamic_theming).string())
            }, onToggle = {
                coroutineScope.launch {
                    viewModel.updateDynamicTheme(
                        context, it
                    )
                }
            }, initial = isDynamicColor.value
            )

            PreferenceEnumPopup<AppearancePreferences.Appearance>(title = {
                Text(
                    UiText.StringRes(R.string.appearance).string()
                )
            },
                headlineContent = { Text(UiText.StringRes(R.string.appearance).string()) },
                supportingContent = {
                    Text(
                        UiText.StringRes(R.string.appearance_list_description).string()
                    )
                },
                trailingContent = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        VerticalDivider(
                            height = 32.dp, modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Switch(checked = when (viewModel.selectedAppearance) {
                            AppearancePreferences.Appearance.LIGHT -> false
                            AppearancePreferences.Appearance.DARK -> true
                            else -> isSystemInDarkTheme()
                        }, onCheckedChange = {
                            selectedTheme.value = if (it) {
                                AppearancePreferences.Appearance.DARK
                            } else {
                                AppearancePreferences.Appearance.LIGHT
                            }
                            coroutineScope.launch {
                                viewModel.updateAppearance(context, selectedTheme.value)
                            }
                        }, thumbContent = {
                            Icon(
                                switchIcon.first,
                                switchIcon.second.string(),
                                modifier = Modifier.size(SwitchDefaults.IconSize)
                            )
                        })

                    }
                },
                icon = {
                    Icon(
                        switchIcon.first,
                        switchIcon.second.string(),
                    )
                },
                initial = selectedTheme.value,
                onSelectionChange = {},
                onSelectedConfirm = {
                    coroutineScope.launch {
                        viewModel.updateAppearance(context, it)
                    }
                },
                transformLabel = {
                    Text(it.displayName.string(), style = MaterialTheme.typography.bodyLarge)
                })

            AnimatedVisibility(
                visible = context.isDarkTheme(selectedTheme.value)
            ) {
                PreferenceToggle(headlineContent = {
                    Text(UiText.StringRes(R.string.use_amoled_theme).string())
                }, supportingContent = {
                    Text(
                        UiText.StringRes(R.string.use_amoled_improve_message).string()
                    )
                }, onToggle = {
                    coroutineScope.launch {
                        viewModel.updateAmoled(
                            context, it
                        )
                    }
                }, initial = isAmoled.value
                )
            }
        }
    }
}