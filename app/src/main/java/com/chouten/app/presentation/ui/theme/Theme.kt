package com.chouten.app.presentation.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import com.chouten.app.common.FallbackThemes
import com.chouten.app.common.isDarkTheme
import com.chouten.app.domain.proto.AppearancePreferences
import com.chouten.app.presentation.ui.screens.more.screens.appearance_screen.AppearanceViewModel
import com.t8rin.dynamic.theme.ColorTuple
import com.t8rin.dynamic.theme.DynamicTheme
import com.t8rin.dynamic.theme.rememberDynamicThemeState
import dagger.hilt.android.internal.managers.FragmentComponentManager.findActivity

@Composable
fun ChoutenTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val viewModel: AppearanceViewModel = hiltViewModel(
        findActivity(context) as ViewModelStoreOwner
    )

    val currentAppearance by viewModel.getAppearancePreferences(context).collectAsState(
        initial = AppearancePreferences.DEFAULT
    )

    val dynamicColor = rememberSaveable(currentAppearance) { currentAppearance.isDynamicColor }
    val isAmoled = rememberSaveable(currentAppearance) { currentAppearance.isAmoled }
    val isDarkTheme = rememberSaveable(currentAppearance) { context.isDarkTheme(currentAppearance) }

    val colorTuple = remember {
        ColorTuple(
            FallbackThemes.LIGHT.primary
        )
    }
    val themeState = rememberDynamicThemeState(
        colorTuple
    )

    DynamicTheme(
        state = themeState,
        defaultColorTuple = colorTuple,
        content = content,
        dynamicColor = dynamicColor,
        isDarkTheme = isDarkTheme,
        amoledMode = isAmoled
    )
}
