package com.chouten.app.presentation.ui.theme

import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chouten.app.common.FallbackThemes
import com.chouten.app.common.isDarkTheme
import com.chouten.app.domain.proto.AppearancePreferences
import com.chouten.app.presentation.ui.screens.more.screens.appearance_screen.AppearanceViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
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

    val theme = currentAppearance.theme
    val dynamicColor = currentAppearance.isDynamicColor
    val isAmoled = currentAppearance.isAmoled

    val systemUiController = rememberSystemUiController()
    val statusBarColor = theme.surface
    val navigationBarColor = theme.surfaceColorAtElevation(3.dp)

    LaunchedEffect(systemUiController, currentAppearance) {
        val luminance = statusBarColor.luminance()
        val darkIcons = luminance >= 0.5f

        systemUiController.setStatusBarColor(
            color = Color.Transparent, darkIcons = darkIcons
        )

        systemUiController.setNavigationBarColor(
            color = navigationBarColor, darkIcons = darkIcons
        )
    }

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
