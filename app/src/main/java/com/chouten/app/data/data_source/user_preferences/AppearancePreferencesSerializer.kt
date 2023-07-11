package com.chouten.app.data.data_source.user_preferences

import android.util.Log
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.chouten.app.domain.proto.AppearancePreferences
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object AppearancePreferencesSerializer : Serializer<AppearancePreferences> {

    override val defaultValue: AppearancePreferences
        get() = AppearancePreferences.DEFAULT

    /**
     * Deserialize the proto from the given [input] stream.
     * Do not call explicity, this is handled by DataStore.
     * Use [context.appearanceDatastore.data] instead.
     * @throws IOException if reading from the [InputStream] fails.
     */
    override suspend fun readFrom(input: InputStream): AppearancePreferences {
        return try {
            input.use {
                Json.decodeFromString(
                    AppearancePreferences.serializer(),
                    it.readBytes().decodeToString()
                )
            }
        } catch (e: SerializationException) {
            // TODO: Display snackbar error message to user
            Log.d("AppearancePreferencesSerializer", "Failed to read proto")
            e.printStackTrace()
            defaultValue
        } catch (e: CorruptionException) {
            // TODO: Display snackbar error message to user
            Log.d("AppearancePreferencesSerializer", "Attempted to read corrupted proto.")
            e.printStackTrace()
            defaultValue
        }
    }

    /**
     * Serialize the [t] proto to the given [output] stream.
     * Do not call explicity, this is handled by DataStore.
     * Use [context.appearanceDatastore.updateData] instead.
     * @throws IOException if writing to the [OutputStream] fails.
     */
    override suspend fun writeTo(t: AppearancePreferences, output: OutputStream) {
        output.use {
            it.write(Json.encodeToString(AppearancePreferences.serializer(), t).encodeToByteArray())
        }
    }
}