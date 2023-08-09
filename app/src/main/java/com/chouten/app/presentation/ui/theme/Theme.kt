package com.chouten.app.presentation.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chouten.app.common.animate
import com.chouten.app.common.isDarkTheme
import com.chouten.app.domain.proto.AppearancePreferences
import com.chouten.app.presentation.ui.screens.more.screens.appearance_screen.AppearanceViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val DarkColorScheme =
    darkColorScheme(
        primary = md_dark_primary,
        onPrimary = md_dark_onPrimary,
        primaryContainer = md_dark_primaryContainer,
        onPrimaryContainer = md_dark_onPrimaryContainer,
        inversePrimary = md_dark_inversePrimary,
        secondary = md_dark_secondary,
        onSecondary = md_dark_onSecondary,
        secondaryContainer = md_dark_secondaryContainer,
        onSecondaryContainer = md_dark_onSecondaryContainer,
        tertiary = md_dark_tertiary,
        onTertiary = md_dark_onTertiary,
        tertiaryContainer = md_dark_tertiaryContainer,
        onTertiaryContainer = md_dark_onTertiaryContainer,
        background = md_dark_background,
        onBackground = md_dark_onBackground,
        surface = md_dark_surface,
        onSurface = md_dark_onSurface,
        surfaceVariant = md_dark_surfaceVariant,
        onSurfaceVariant = md_dark_onSurfaceVariant,
        surfaceTint = md_dark_surfaceTint,
        inverseSurface = md_dark_inverseSurface,
        inverseOnSurface = md_dark_inverseOnSurface,
        error = md_dark_error,
        onError = md_dark_onError,
        errorContainer = md_dark_errorContainer,
        onErrorContainer = md_dark_onErrorContainer,
        outline = md_dark_outline,
        outlineVariant = md_dark_outlineVariant,
        scrim = md_dark_scrim,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = md_light_primary,
        onPrimary = md_light_onPrimary,
        primaryContainer = md_light_primaryContainer,
        onPrimaryContainer = md_light_onPrimaryContainer,
        inversePrimary = md_light_inversePrimary,
        secondary = md_light_secondary,
        onSecondary = md_light_onSecondary,
        secondaryContainer = md_light_secondaryContainer,
        onSecondaryContainer = md_light_onSecondaryContainer,
        tertiary = md_light_tertiary,
        onTertiary = md_light_onTertiary,
        tertiaryContainer = md_light_tertiaryContainer,
        onTertiaryContainer = md_light_onTertiaryContainer,
        background = md_light_background,
        onBackground = md_light_onBackground,
        surface = md_light_surface,
        onSurface = md_light_onSurface,
        surfaceVariant = md_light_surfaceVariant,
        onSurfaceVariant = md_light_onSurfaceVariant,
        surfaceTint = md_light_surfaceTint,
        inverseSurface = md_light_inverseSurface,
        inverseOnSurface = md_light_inverseOnSurface,
        error = md_light_error,
        onError = md_light_onError,
        errorContainer = md_light_errorContainer,
        onErrorContainer = md_light_onErrorContainer,
        outline = md_light_outline,
        outlineVariant = md_light_outlineVariant,
        scrim = md_light_scrim,
    )


@Composable
fun ChoutenTheme(
    viewModel: AppearanceViewModel = hiltViewModel(),
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val currentAppearance by viewModel.getAppearancePreferences(context).collectAsState(
        initial = AppearancePreferences.DEFAULT
    )

    val dynamicColor = currentAppearance.isDynamicColor
    val isAmoled = currentAppearance.isAmoled

    val colorScheme = remember(currentAppearance.appearance, darkTheme, dynamicColor) {
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                if (currentAppearance.appearance == AppearancePreferences.Appearance.DARK) dynamicDarkColorScheme(
                    context
                ) else if (currentAppearance.appearance == AppearancePreferences.Appearance.SYSTEM) {
                    if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(
                        context
                    )
                } else dynamicLightColorScheme(context)
            }

            context.isDarkTheme(currentAppearance) -> DarkColorScheme
            AppearancePreferences.Appearance.LIGHT == currentAppearance.appearance -> LightColorScheme
            else -> LightColorScheme
        }
    }.let {
        if (isAmoled && context.isDarkTheme(currentAppearance)) {
            it.copy(
                surface = Color.Black,
                background = Color.Black,
            )
        } else it
    }

    val systemUiController = rememberSystemUiController()
    val statusBarColor = colorScheme.surface
    val navigationBarColor = colorScheme.surfaceColorAtElevation(3.dp)

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

    MaterialTheme(
        colorScheme = colorScheme.animate(), typography = Typography, content = content
    )
}
