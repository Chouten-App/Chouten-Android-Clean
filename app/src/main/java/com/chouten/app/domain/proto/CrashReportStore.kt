package com.chouten.app.domain.proto

import android.content.Context
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

@JvmInline
@Serializable
value class CrashReportUUID(val uuid: String)

/**
 * @param reportPath The absolute path to the crash report.
 */
@JvmInline
@Serializable
value class CrashReport(val reportPath: String)

/**
 * Data class representing the user's crash report store.
 * @param unsentCrashReport The last unsent crash report.
 * @param lastCrashReport The last crash report (identified by the Report UUID).
 * @param enabled Whether crash reporting is enabled
 * @param hasBeenRequested Whether the user has been asked to enable crash reporting.
 */
@Serializable
data class CrashReportStore @OptIn(ExperimentalSerializationApi::class) constructor(
    @EncodeDefault val unsentCrashReport: CrashReport? = null,
    @EncodeDefault val lastCrashReport: CrashReportUUID? = null,
    @EncodeDefault val enabled: Boolean = false,
    @EncodeDefault val hasBeenRequested: Boolean = false
) {
    companion object {
        val DEFAULT = CrashReportStore()
    }
}

val Context.crashReportStore by dataStore(
    fileName = "crash_report_store.pb",
    serializer = CrashReportStoreSerializer
)

/**
 * Serializer for [CrashReportStore].
 */
object CrashReportStoreSerializer : Serializer<CrashReportStore> {
    override val defaultValue: CrashReportStore
        get() = CrashReportStore.DEFAULT

    override suspend fun readFrom(input: InputStream): CrashReportStore {
        input.bufferedReader().use {
            return Json.decodeFromString(CrashReportStore.serializer(), it.readText())
        }
    }

    override suspend fun writeTo(t: CrashReportStore, output: OutputStream) {
        output.bufferedWriter().use {
            it.write(Json.encodeToString(CrashReportStore.serializer(), t))
        }
    }

}