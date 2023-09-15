package com.chouten.app.domain.proto

import android.content.Context
import androidx.datastore.dataStore
import com.chouten.app.data.data_source.user_preferences.ModulePreferencesSerializer
import kotlinx.serialization.Serializable

/**
 * Data class representing the user's module preferences.
 * @param selectedModuleId The selected module UUID.
 * @param autoUpdatingModules The list of module UUIDs that are set to auto update.
 */
@Serializable
data class ModulePreferences(
    val selectedModuleId: String, val autoUpdatingModules: List<String>
) {
    companion object {
        val DEFAULT = ModulePreferences(
            selectedModuleId = "", autoUpdatingModules = emptyList()
        )
    }
}

val Context.moduleDatastore by dataStore(
    fileName = "module_preferences.pb",
    serializer = ModulePreferencesSerializer,
)