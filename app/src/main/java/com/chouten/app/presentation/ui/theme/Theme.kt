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

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
background = Color(0xFFFFFBFE),
surface = Color(0xFFFFFBFE),
onPrimary = Color.White,
onSecondary = Color.White,
onTertiary = Color.White,
onBackground = Color(0xFF1C1B1F),
onSurface = Color(0xFF1C1B1F),
*/
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
        val darkIcons =
            if (currentAppearance.appearance == AppearancePreferences.Appearance.DARK) luminance <= 0.5 else luminance > 0.5

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
