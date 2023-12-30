package com.chouten.app.common

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.chouten.app.domain.model.Version
import com.chouten.app.domain.proto.AppearancePreferences
import com.chouten.app.domain.proto.appearanceDatastore
import kotlinx.coroutines.flow.firstOrNull
import java.util.concurrent.TimeUnit


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
 * @param appearance The AppearancePreferences.Appearance enum.
 * @return true if the app is in dark theme
 * @see isSystemInDarkTheme
 */
fun Context.isDarkTheme(appearance: AppearancePreferences.Appearance): Boolean {
    val uiMode = applicationContext.resources.configuration.uiMode
    val uiDark = Configuration.UI_MODE_NIGHT_YES
    val isSystemDark = uiMode.and(Configuration.UI_MODE_NIGHT_MASK) == uiDark

    return when (appearance) {
        AppearancePreferences.Appearance.SYSTEM -> isSystemDark
        AppearancePreferences.Appearance.DARK -> true
        AppearancePreferences.Appearance.LIGHT -> false
    }
}

/**
 * Returns true if the app is in dark theme.
 * Not to be confused with the system theme.
 * @param prefs The AppearancePreferences data class.
 * @see AppearancePreferences
 * @see isSystemInDarkTheme
 */
fun Context.isDarkTheme(prefs: AppearancePreferences): Boolean {
    return isDarkTheme(prefs.appearance)
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
        return isDarkTheme(it)
    } ?: return isSystemDark
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * Returns a Long formatted as HH:MM:SS (omitting hours if 0)
 * A value of 0 (or less) will return 00:00.
 * @return A formatted string (e.g. 01:23:45 or 23:45)
 */
fun Long.formatMinSec(): String {
    return if (this <= 0L) {
        "00:00"
    } else {
        // Format HH:MM:SS or MM:SS if hours is 0
        if (TimeUnit.MILLISECONDS.toHours(this) > 0) {
            String.format(
                "%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(this),
                TimeUnit.MILLISECONDS.toMinutes(this) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(this)),
                TimeUnit.MILLISECONDS.toSeconds(this) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(this))
            )
        } else {
            String.format(
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(this),
                TimeUnit.MILLISECONDS.toSeconds(this) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(this))
            )
        }
    }
}

/**
 * [Linear Interpolation](https://en.wikipedia.org/wiki/Linear_interpolation) function that moves
 * amount from it's current position to start and amount
 * @param start of interval
 * @param end of interval
 * @param amount e closed unit interval [0, 1]
 */
internal fun lerp(start: Float, end: Float, amount: Float): Float {
    return (1 - amount) * start + amount * end
}

/**
 * Scale x1 from start1..end1 range to start2..end2 range
 */
internal fun scale(start1: Float, end1: Float, pos: Float, start2: Float, end2: Float) =
    lerp(start2, end2, calculateFraction(start1, end1, pos))

/**
 * Scale x.start, x.endInclusive from a1..b1 range to a2..b2 range
 */
internal fun scale(
    start1: Float,
    end1: Float,
    range: ClosedFloatingPointRange<Float>,
    start2: Float,
    end2: Float
) =
    scale(start1, end1, range.start, start2, end2)..scale(
        start1,
        end1,
        range.endInclusive,
        start2,
        end2
    )

/**
 * Calculate fraction for value between a range [end] and [start] coerced into 0f-1f range
 */
fun calculateFraction(start: Float, end: Float, pos: Float) =
    (if (end - start == 0f) 0f else (pos - start) / (end - start)).coerceIn(0f, 1f)

/**
 * Parses a version string into a [Version] object.
 *
 * @param useRegex Flag to determine whether to use regex for parsing.
 *          Not using regex is stricter and will throw an exception for more invalid strings.
 * @return The parsed [Version] object.
 * @throws IllegalArgumentException If the version string is not valid.
 */
fun String.toVersion(useRegex: Boolean = false) = Version(this, useRegex)