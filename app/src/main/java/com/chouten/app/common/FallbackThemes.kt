package com.chouten.app.common

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import com.chouten.app.presentation.ui.theme.md_dark_background
import com.chouten.app.presentation.ui.theme.md_dark_error
import com.chouten.app.presentation.ui.theme.md_dark_errorContainer
import com.chouten.app.presentation.ui.theme.md_dark_inverseOnSurface
import com.chouten.app.presentation.ui.theme.md_dark_inversePrimary
import com.chouten.app.presentation.ui.theme.md_dark_inverseSurface
import com.chouten.app.presentation.ui.theme.md_dark_onBackground
import com.chouten.app.presentation.ui.theme.md_dark_onError
import com.chouten.app.presentation.ui.theme.md_dark_onErrorContainer
import com.chouten.app.presentation.ui.theme.md_dark_onPrimary
import com.chouten.app.presentation.ui.theme.md_dark_onPrimaryContainer
import com.chouten.app.presentation.ui.theme.md_dark_onSecondary
import com.chouten.app.presentation.ui.theme.md_dark_onSecondaryContainer
import com.chouten.app.presentation.ui.theme.md_dark_onSurface
import com.chouten.app.presentation.ui.theme.md_dark_onSurfaceVariant
import com.chouten.app.presentation.ui.theme.md_dark_onTertiary
import com.chouten.app.presentation.ui.theme.md_dark_onTertiaryContainer
import com.chouten.app.presentation.ui.theme.md_dark_outline
import com.chouten.app.presentation.ui.theme.md_dark_outlineVariant
import com.chouten.app.presentation.ui.theme.md_dark_primary
import com.chouten.app.presentation.ui.theme.md_dark_primaryContainer
import com.chouten.app.presentation.ui.theme.md_dark_scrim
import com.chouten.app.presentation.ui.theme.md_dark_secondary
import com.chouten.app.presentation.ui.theme.md_dark_secondaryContainer
import com.chouten.app.presentation.ui.theme.md_dark_surface
import com.chouten.app.presentation.ui.theme.md_dark_surfaceTint
import com.chouten.app.presentation.ui.theme.md_dark_surfaceVariant
import com.chouten.app.presentation.ui.theme.md_dark_tertiary
import com.chouten.app.presentation.ui.theme.md_dark_tertiaryContainer
import com.chouten.app.presentation.ui.theme.md_light_background
import com.chouten.app.presentation.ui.theme.md_light_error
import com.chouten.app.presentation.ui.theme.md_light_errorContainer
import com.chouten.app.presentation.ui.theme.md_light_inverseOnSurface
import com.chouten.app.presentation.ui.theme.md_light_inversePrimary
import com.chouten.app.presentation.ui.theme.md_light_inverseSurface
import com.chouten.app.presentation.ui.theme.md_light_onBackground
import com.chouten.app.presentation.ui.theme.md_light_onError
import com.chouten.app.presentation.ui.theme.md_light_onErrorContainer
import com.chouten.app.presentation.ui.theme.md_light_onPrimary
import com.chouten.app.presentation.ui.theme.md_light_onPrimaryContainer
import com.chouten.app.presentation.ui.theme.md_light_onSecondary
import com.chouten.app.presentation.ui.theme.md_light_onSecondaryContainer
import com.chouten.app.presentation.ui.theme.md_light_onSurface
import com.chouten.app.presentation.ui.theme.md_light_onSurfaceVariant
import com.chouten.app.presentation.ui.theme.md_light_onTertiary
import com.chouten.app.presentation.ui.theme.md_light_onTertiaryContainer
import com.chouten.app.presentation.ui.theme.md_light_outline
import com.chouten.app.presentation.ui.theme.md_light_outlineVariant
import com.chouten.app.presentation.ui.theme.md_light_primary
import com.chouten.app.presentation.ui.theme.md_light_primaryContainer
import com.chouten.app.presentation.ui.theme.md_light_scrim
import com.chouten.app.presentation.ui.theme.md_light_secondary
import com.chouten.app.presentation.ui.theme.md_light_secondaryContainer
import com.chouten.app.presentation.ui.theme.md_light_surface
import com.chouten.app.presentation.ui.theme.md_light_surfaceTint
import com.chouten.app.presentation.ui.theme.md_light_surfaceVariant
import com.chouten.app.presentation.ui.theme.md_light_tertiary
import com.chouten.app.presentation.ui.theme.md_light_tertiaryContainer

object FallbackThemes {
    val LIGHT = lightColorScheme(
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
    val DARK = darkColorScheme(
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
}