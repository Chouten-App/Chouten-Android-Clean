package com.chouten.app.common

import android.content.Context
import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.chouten.app.domain.proto.AppearancePreferences
import com.chouten.app.domain.proto.appearanceDatastore
import kotlinx.coroutines.flow.firstOrNull


/**
 * Animates the color scheme from the current color scheme to the new color scheme.
 * This MAY not be performant.
 */
@Composable
fun ColorScheme.animate(): ColorScheme {
    // TODO: Is 300ms too long?
    // Being too long may cause some colour banding
    // or appear to be laggy.
    val animSpec = remember {
        tween<Color>(durationMillis = 300, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f))
    }

    @Composable
    fun animateColor(color: Color): Color =
        animateColorAsState(
            targetValue = color,
            animationSpec = animSpec,
            label = "Theme Color Transition"
        ).value

    return ColorScheme(
        primary = animateColor(this.primary),
        onPrimary = animateColor(this.onPrimary),
        primaryContainer = animateColor(this.primaryContainer),
        onPrimaryContainer = animateColor(this.onPrimaryContainer),
        inversePrimary = animateColor(this.inversePrimary),
        secondary = animateColor(this.secondary),
        onSecondary = animateColor(this.onSecondary),
        secondaryContainer = animateColor(this.secondaryContainer),
        onSecondaryContainer = animateColor(this.onSecondaryContainer),
        tertiary = animateColor(this.tertiary),
        onTertiary = animateColor(this.onTertiary),
        tertiaryContainer = animateColor(this.tertiaryContainer),
        onTertiaryContainer = animateColor(this.onTertiaryContainer),
        background = animateColor(this.background),
        onBackground = animateColor(this.onBackground),
        surface = animateColor(this.surface),
        onSurface = animateColor(this.onSurface),
        surfaceVariant = animateColor(this.surfaceVariant),
        onSurfaceVariant = animateColor(this.onSurfaceVariant),
        surfaceTint = animateColor(this.surfaceTint),
        inverseSurface = animateColor(this.inverseSurface),
        inverseOnSurface = animateColor(this.inverseOnSurface),
        error = animateColor(this.error),
        onError = animateColor(this.onError),
        errorContainer = animateColor(this.errorContainer),
        onErrorContainer = animateColor(this.onErrorContainer),
        outline = animateColor(this.outline),
        outlineVariant = animateColor(this.outlineVariant),
        scrim = animateColor(this.scrim)
    )
}

/**
 * Returns true if the app is in dark theme.
 * Not to be confused with the system theme.
 * @param prefs The AppearancePreferences data class.
 * @see AppearancePreferences
 * @see isSystemInDarkTheme
 */
fun Context.isDarkTheme(prefs: AppearancePreferences): Boolean {
    val uiMode = applicationContext.resources.configuration.uiMode
    val uiDark = Configuration.UI_MODE_NIGHT_YES
    val isSystemDark = uiMode.and(Configuration.UI_MODE_NIGHT_MASK) == uiDark
    // We need to check if the user has set the theme to dark or light
    // or system default. This will be stored within the AppearancePreferences
    // proto.
    return when (prefs.appearance) {
        AppearancePreferences.Appearance.SYSTEM -> isSystemDark
        AppearancePreferences.Appearance.DARK -> true
        AppearancePreferences.Appearance.LIGHT -> false
    }
}

/**
 * Returns true if the app is in dark theme.
 * Not to be confused with the system theme.
 * @return true if the app is in dark theme OR, if the preference is not set,
 * if the OS is in Dark Mode.
 * @see isSystemInDarkTheme
 */
suspend fun Context.isDarkTheme(): Boolean {
    val uiMode = applicationContext.resources.configuration.uiMode
    val uiDark = Configuration.UI_MODE_NIGHT_YES
    val isSystemDark = uiMode.and(Configuration.UI_MODE_NIGHT_MASK) == uiDark
    // We need to check if the user has set the theme to dark or light
    // or system default. This will be stored within the AppearancePreferences
    // proto.
    appearanceDatastore.data.firstOrNull()?.let {
        return when (it.appearance) {
            AppearancePreferences.Appearance.SYSTEM -> isSystemDark
            AppearancePreferences.Appearance.DARK -> true
            AppearancePreferences.Appearance.LIGHT -> false
        }
    } ?: return isSystemDark
}