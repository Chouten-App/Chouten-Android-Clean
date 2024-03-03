package com.chouten.app.domain.proto

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.datastore.dataStore
import com.chouten.app.data.data_source.user_preferences.FilePreferencesSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Data class representing the user's file preferences.
 * @param CHOUTEN_ROOT_DIR The root directory of the Chouten app. (e.g /storage/emulated/0/Documents/Chouten).
 * @param IS_CHOUTEN_MODULE_DIR_SET Whether the module directory of the Chouten app has been set.
 * @param SAVE_MODULE_ARTIFACTS Whether to save .module artifacts during module installation
 */
@Serializable
data class FilePreferences(
    @Serializable(with = UriSerializer::class) val CHOUTEN_ROOT_DIR: Uri,
    val IS_CHOUTEN_MODULE_DIR_SET: Boolean = false,
    val SAVE_MODULE_ARTIFACTS: Boolean = true
) {
    companion object {
        val DEFAULT = FilePreferences(
            CHOUTEN_ROOT_DIR = Uri.EMPTY, IS_CHOUTEN_MODULE_DIR_SET = false
        )
    }
}

val Context.filepathDatastore by dataStore(
    fileName = "file_preferences.pb",
    serializer = FilePreferencesSerializer,
)

// Custom Serializer for Uri
object UriSerializer : KSerializer<Uri> {
    override fun deserialize(decoder: Decoder): Uri {
        return decoder.decodeString().toUri()
    }

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("UriSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Uri) {
        encoder.encodeString(value.toString())
    }
}