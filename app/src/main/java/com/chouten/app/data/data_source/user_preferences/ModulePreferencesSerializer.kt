package com.chouten.app.data.data_source.user_preferences

import android.util.Log
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.chouten.app.domain.proto.ModulePreferences
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object ModulePreferencesSerializer : Serializer<ModulePreferences> {

    override val defaultValue: ModulePreferences
        get() = ModulePreferences.DEFAULT

    /**
     * Deserialize the proto from the given [input] stream.
     * Do not call explicity, this is handled by DataStore.
     * Use [context.moduleDatastore.data] instead.
     * @throws IOException if reading from the [InputStream] fails.
     */
    override suspend fun readFrom(input: InputStream): ModulePreferences {
        return try {
            input.use {
                Json.decodeFromString(
                    ModulePreferences.serializer(), it.readBytes().decodeToString()
                )
            }
        } catch (e: SerializationException) {
            // TODO: Display snackbar error message to user
            Log.d("ModulePreferencesSerializer", "Failed to read proto")
            e.printStackTrace()
            defaultValue
        } catch (e: CorruptionException) {
            // TODO: Display snackbar error message to user
            Log.d("ModulePreferencesSerializer", "Attempted to read corrupted proto.")
            e.printStackTrace()
            defaultValue
        }
    }

    /**
     * Serialize the [t] proto to the given [output] stream.
     * Do not call explicity, this is handled by DataStore.
     * Use [context.moduleDatastore.updateData] instead.
     * @throws IOException if writing to the [OutputStream] fails.
     */
    override suspend fun writeTo(t: ModulePreferences, output: OutputStream) {
        output.use {
            it.write(
                Json.encodeToString(
                    ModulePreferences.serializer(), t
                ).encodeToByteArray()
            )
        }
    }
}