package com.chouten.app.presentation.ui.screens.more.screens.appearance_screen

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.chouten.app.domain.proto.AppearancePreferences
import com.chouten.app.domain.proto.appearanceDatastore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class AppearanceViewModel @Inject constructor() : ViewModel() {

    val isDynamicColor = mutableStateOf(AppearancePreferences.DEFAULT.isDynamicColor)
    val selectedAppearance = mutableStateOf(AppearancePreferences.DEFAULT.appearance)

    /**
     * Gets the appearance preferences from the datastore.
     * @param context The context to use to get the datastore.
     * @return A flow of the appearance preferences.
     */
    fun getAppearancePreferences(context: Context): Flow<AppearancePreferences> {
        return context.appearanceDatastore.data
    }

    /**
     * Updates the dynamic color setting in the datastore
     * and updates the dynamic color setting in the view model.
     * @param context The context to use to update the datastore.
     * @param isDynamicColor Whether the app should use dynamic (material you) colors.
     */
    suspend fun updateDynamicTheme(context: Context, isDynamicColor: Boolean) {
        context.appearanceDatastore.updateData { current ->
            this.isDynamicColor.value = isDynamicColor
            current.copy(isDynamicColor = isDynamicColor)
        }
    }

    /**
     * Updates the theme in the datastore and updates the selected theme in the view model.
     * @param context The context to use to update the datastore.
     * @param appearance The new appearance to set (light, dark, or system default).
     */
    suspend fun updateTheme(context: Context, appearance: AppearancePreferences.Appearance) {
        context.appearanceDatastore.updateData { current ->
            selectedAppearance.value = appearance
            current.copy(appearance = appearance)
        }
    }
}