package com.chouten.app.presentation.ui.screens.more.screens.appearance_screen

import android.content.Context
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.chouten.app.domain.proto.AppearancePreferences
import com.chouten.app.domain.proto.appearanceDatastore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class AppearanceViewModel @Inject constructor() : ViewModel() {

    var selectedTheme by mutableStateOf(AppearancePreferences.DEFAULT.theme)
    var isDynamicColor by mutableStateOf(AppearancePreferences.DEFAULT.isDynamicColor)
    var selectedAppearance by mutableStateOf(AppearancePreferences.DEFAULT.appearance)
    var isAmoled by mutableStateOf(AppearancePreferences.DEFAULT.isAmoled)

    /**
     * Gets the appearance preferences from the datastore.
     * @param context The context to use to get the datastore.
     * @return A flow of the appearance preferences.
     */
    fun getAppearancePreferences(context: Context): Flow<AppearancePreferences> {
        return context.appearanceDatastore.data
    }

    /**
     * Updates the theme in the datastore and updates the selected theme in the view model.
     * @param context The context to use to update the datastore.
     * @param theme The new theme to set.
     */
    suspend fun updateTheme(context: Context, theme: ColorScheme) {
        context.appearanceDatastore.updateData { current ->
            selectedTheme = theme
            current.copy(theme = theme)
        }
    }

    /**
     * Updates the dynamic color setting in the datastore
     * and updates the dynamic color setting in the view model.
     * @param context The context to use to update the datastore.
     * @param isDynamicColor Whether the app should use dynamic (material you) colors.
     */
    suspend fun updateDynamicTheme(context: Context, isDynamicColor: Boolean) {
        context.appearanceDatastore.updateData { current ->
            this.isDynamicColor = isDynamicColor
            current.copy(isDynamicColor = isDynamicColor)
        }
    }

    /**
     * Updates the appearance in the datastore and updates the selected appearance in the view model.
     * @param context The context to use to update the datastore.
     * @param appearance The new appearance to set (light, dark, or system default).
     */
    suspend fun updateAppearance(context: Context, appearance: AppearancePreferences.Appearance) {
        context.appearanceDatastore.updateData { current ->
            selectedAppearance = appearance
            current.copy(appearance = appearance)
        }
    }

    /**
     * Updates the amoled setting in the datastore
     * and updates the amoled setting in the view model.
     * @param context The context to use to update the datastore.
     * @param isAmoled Whether the app should use amoled colors (black for surface & background).
     */
    suspend fun updateAmoled(context: Context, isAmoled: Boolean) {
        context.appearanceDatastore.updateData { current ->
            this.isAmoled = isAmoled
            current.copy(isAmoled = isAmoled)
        }
    }
}