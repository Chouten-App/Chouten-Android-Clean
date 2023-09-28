package com.chouten.app.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.compositionLocalOf
import com.chouten.app.presentation.ui.ChoutenApp

/**
 * CompositionLocal for the app's padding values.
 * This is used to provide padding values to screens that are not part of the navigation graph.
 * The [PaddingValues] used comes from the Scaffold in [ChoutenApp]
 * This should be used for padding in any screen that does not want its content
 * to appear behind either of the system bars.
 */
val LocalAppPadding = compositionLocalOf { PaddingValues() }