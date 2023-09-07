package com.chouten.app.presentation.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.chouten.app.common.FallbackThemes
import com.chouten.app.common.isDarkTheme
import com.chouten.app.domain.proto.AppearancePreferences
import com.chouten.app.presentation.ui.screens.more.screens.appearance_screen.AppearanceViewModel
import com.t8rin.dynamic.theme.ColorTuple
import com.t8rin.dynamic.theme.DynamicTheme
import com.t8rin.dynamic.theme.rememberDynamicThemeState

@Composable
fun ChoutenTheme(
    viewModel: AppearanceViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val currentAppearance by viewModel.getAppearancePreferences(context).collectAsState(
        initial = AppearancePreferences.DEFAULT
    )

    val dynamicColor = currentAppearance.isDynamicColor
    val isAmoled = currentAppearance.isAmoled

    val themeState = rememberDynamicThemeState()
    DynamicTheme(
        state = themeState,
        defaultColorTuple = ColorTuple(
            FallbackThemes.LIGHT.primary
        ),
        content = content,
        dynamicColor = dynamicColor,
        isDarkTheme = context.isDarkTheme(currentAppearance),
        amoledMode = isAmoled
    )
}
