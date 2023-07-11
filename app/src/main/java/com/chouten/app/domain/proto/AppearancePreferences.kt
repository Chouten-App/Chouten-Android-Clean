package com.chouten.app.domain.proto

import android.content.Context
import android.os.Build
import androidx.datastore.dataStore
import com.chouten.app.R
import com.chouten.app.common.UiText
import com.chouten.app.data.data_source.user_preferences.AppearancePreferencesSerializer
import kotlinx.serialization.Serializable

/**
 * Data class representing the user's appearance preferences.
 * @param appearance The appearance of the app (light, dark, or system default).
 * @param isDynamicColor Whether the app should use dynamic colors (API 31+ only).
 */
@Serializable
data class AppearancePreferences(
    val appearance: Appearance,
    val isDynamicColor: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
) {
    @Serializable
    enum class Appearance(val displayName: UiText) {
        SYSTEM(UiText.StringRes(R.string.system)),
        LIGHT(UiText.StringRes(R.string.light)),
        DARK(UiText.StringRes(R.string.dark)), ;

        fun toString(context: Context): String {
            return displayName.string(context)
        }
    }


    companion object {
        val DEFAULT = AppearancePreferences(
            appearance = Appearance.SYSTEM,
            isDynamicColor = true,
        )
    }
}

val Context.appearanceDatastore by dataStore(
    fileName = "appearance_preferences.pb",
    serializer = AppearancePreferencesSerializer,
)