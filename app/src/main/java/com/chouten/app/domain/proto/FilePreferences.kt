package com.chouten.app.domain.proto

import android.content.Context
import android.os.Environment
import androidx.datastore.dataStore
import com.chouten.app.data.data_source.user_preferences.FilePreferencesSerializer
import kotlinx.serialization.Serializable

/**
 * Data class representing the user's file preferences.
 * @param CHOUTEN_ROOT_DIR The root directory of the Chouten app. (e.g /storage/emulated/0/Documents/Chouten).
 * @param IS_CHOUTEN_MODULE_DIR_SET Whether the module directory of the Chouten app has been set.
 */
@Serializable
data class FilePreferences(
    val CHOUTEN_ROOT_DIR: String,
    val IS_CHOUTEN_MODULE_DIR_SET: Boolean = false,
) {
    companion object {
        val DEFAULT = FilePreferences(
            /**
             * NOTE: This method is deprecated in API 29 and above.
             * ONLY use this method when there is no other alternative.
             */
            CHOUTEN_ROOT_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                .resolve("Chouten").absolutePath,
            IS_CHOUTEN_MODULE_DIR_SET = false
        )
    }
}

val Context.filepathDatastore by dataStore(
    fileName = "file_preferences.pb",
    serializer = FilePreferencesSerializer,
)