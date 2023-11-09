package com.chouten.app

import android.content.Context
import android.util.Log
import android.webkit.MimeTypeMap
import com.chouten.app.domain.proto.CrashReport
import com.chouten.app.domain.proto.CrashReportUUID
import com.chouten.app.domain.proto.ModulePreferences
import com.chouten.app.domain.proto.crashReportStore
import com.chouten.app.domain.proto.moduleDatastore
import com.google.auto.service.AutoService
import com.lagradost.nicehttp.Requests
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink
import org.acra.ReportField
import org.acra.config.CoreConfiguration
import org.acra.data.CrashReportData
import org.acra.sender.ReportSender
import org.acra.sender.ReportSenderFactory
import kotlin.concurrent.thread

typealias EmbedThumbnail = DiscordWebhook.Embed.EmbedImage
typealias EmbedVideo = DiscordWebhook.Embed.EmbedImage

/**
 * A class which can be used to send multipart requests to a Discord Webhook.
 * @see <a href="https://discord.com/developers/docs/resources/webhook">Discord Webhook Documentation</a>
 */
@Serializable
data class DiscordWebhookMultipart(
    @SerialName("payload_json") val payloadJson: DiscordWebhook,
    val files: List<DiscordWebhook.Embed.EmbedFile> = listOf(),
)

/**
 * SOME of the values which can be sent to a Discord Webhook. To add files to the webhook, use the
 * [DiscordWebhookMultipart] class.
 * @see <a href="https://discord.com/developers/docs/resources/webhook">Discord Webhook Documentation</a>
 */
@Serializable
data class DiscordWebhook @OptIn(ExperimentalSerializationApi::class) constructor(
    @EncodeDefault val username: String = "Chouten Crash Reporter",
    @EncodeDefault @SerialName("avatar_url") val avatarUrl: String = "https://www.chouten.app/Icon.png",
    val tts: Boolean = false,
    @EncodeDefault val embeds: List<Embed> = listOf(),
    @EncodeDefault val content: String? = if (embeds.isEmpty()) throw IllegalArgumentException(
        "Atleast one of (content, embeds) is required."
    ) else null,
) {
    @Serializable
    data class Embed @OptIn(ExperimentalSerializationApi::class) constructor(
        @EncodeDefault val title: String = "",
        @EncodeDefault val type: String = "rich",
        @EncodeDefault val description: String? = null,
        @EncodeDefault val url: String? = null,
        @EncodeDefault val timestamp: String? = null,
        @EncodeDefault val color: Int? = null,
        @EncodeDefault val footer: EmbedFooter? = null,
        @EncodeDefault val image: EmbedImage? = null,
        @EncodeDefault val thumbnail: EmbedThumbnail? = null,
        @EncodeDefault val video: EmbedVideo? = null,
        @EncodeDefault val provider: EmbedProvider? = null,
        @EncodeDefault val author: EmbedAuthor? = null,
        @EncodeDefault val fields: List<EmbedField>? = null,
    ) {
        @Serializable
        data class EmbedFooter @OptIn(ExperimentalSerializationApi::class) constructor(
            val text: String,
            @EncodeDefault @SerialName("icon_url") val iconUrl: String? = null,
            @EncodeDefault @SerialName("proxy_icon_url") val proxyIconUrl: String? = null,
        )

        @Serializable
        data class EmbedImage @OptIn(ExperimentalSerializationApi::class) constructor(
            val url: String,
            @EncodeDefault @SerialName("proxy_url") val proxyUrl: String? = null,
            val height: Int? = null,
            val width: Int? = null,
        )

        @Serializable
        data class EmbedFile @OptIn(ExperimentalSerializationApi::class) constructor(
            val fileName: String,
            val fileContents: String,
            @EncodeDefault val contentType: String = MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(fileName) ?: "text/plain"
        )

        @Serializable
        data class EmbedProvider @OptIn(ExperimentalSerializationApi::class) constructor(
            @EncodeDefault val name: String? = null,
            @EncodeDefault val url: String? = null,
        )

        @Serializable
        data class EmbedAuthor @OptIn(ExperimentalSerializationApi::class) constructor(
            val name: String,
            @EncodeDefault @SerialName("url") val url: String? = null,
            @EncodeDefault @SerialName("icon_url") val iconUrl: String? = null,
            @EncodeDefault @SerialName("proxy_icon_url") val proxyIconUrl: String? = null,
        )

        /*
        name	string	name of the field
value	string	value of the field
inline?	boolean	whether or not this field should display inline
         */
        @Serializable
        data class EmbedField @OptIn(ExperimentalSerializationApi::class) constructor(
            val name: String,
            val value: String,
            @EncodeDefault val inline: Boolean = false,
        )
    }
}


class DiscordReporter(
    private val client: Requests
) : ReportSender {

    private fun String.bold() = "**$this**"
    private fun String.italic() = "*$this*"

    override fun send(context: Context, errorContent: CrashReportData) {
        thread {
            runBlocking {
                context.crashReportStore.updateData { preferences ->
                    preferences.copy(
                        lastCrashReport = CrashReportUUID(
                            uuid = errorContent.getString(ReportField.REPORT_ID)
                                ?: "NO_UUID_FOUND-0000"
                        )
                    )
                }
            }
        }
        thread {
            runBlocking {
                // We don't want to send the report if the user has disabled it
                if (context.crashReportStore.data.firstOrNull()?.enabled != true) return@runBlocking
                val parsed = DiscordWebhookMultipart(
                    payloadJson = DiscordWebhook(
                        username = "Chouten Crash Reporter",
                        embeds = listOf(
                            DiscordWebhook.Embed(
                                title = "Client Details",
                                description = listOf(
                                    "App Version: ${
                                        errorContent.getString(ReportField.APP_VERSION_NAME)
                                    } (${errorContent.getString(ReportField.APP_VERSION_CODE)})",
                                    "Android Version: ${
                                        errorContent.getString(
                                            ReportField.ANDROID_VERSION
                                        )
                                    }",
                                    "Device: ${errorContent.getString(ReportField.BRAND)} ${
                                        errorContent.getString(
                                            ReportField.PHONE_MODEL
                                        )
                                    }"
                                ).joinToString(separator = "\n") { it.bold() },
                                color = 0x23A8F2,
                                thumbnail = EmbedThumbnail(
                                    url = "https://www.chouten.app/Icon.png"
                                ),
                                footer = DiscordWebhook.Embed.EmbedFooter(
                                    text = "Report ID: ${errorContent.getString(ReportField.REPORT_ID)}."
                                ),
                            ), DiscordWebhook.Embed(
                                title = "Crash Details",
                                description = null,
                                fields = listOfNotNull(
                                    errorContent.getString(ReportField.USER_CRASH_DATE)?.let {
                                        DiscordWebhook.Embed.EmbedField(
                                            name = "Crashed At:", value = it
                                        )
                                    },
                                    DiscordWebhook.Embed.EmbedField(
                                        name = "Crashed within 10s?", value = run {
                                            // The time given by ACRA looks like "2023-11-08T13:00:02.213+00:00"
                                            val time =
                                                errorContent.getString(ReportField.USER_CRASH_DATE)
                                                    ?.split("T")?.getOrNull(1)?.substringBefore(".")
                                            val launchTime =
                                                errorContent.getString(ReportField.USER_APP_START_DATE)
                                                    ?.split("T")?.getOrNull(1)?.substringBefore(".")
                                            // The times will now be in the format HH:MM:SS.MS
                                            // We want to convert this to milliseconds
                                            val timeMillis = time?.split(":")?.map { it.toInt() }
                                                ?.let { (hours, minutes, seconds) ->
                                                    (hours * 60 * 60 * 1000) + (minutes * 60 * 1000) + (seconds * 1000)
                                                }
                                            val launchTimeMillis =
                                                launchTime?.split(":")?.map { it.toInt() }
                                                    ?.let { (hours, minutes, seconds) ->
                                                        (hours * 60 * 60 * 1000) + (minutes * 60 * 1000) + (seconds * 1000)
                                                    }
                                            if (timeMillis != null && launchTimeMillis != null) {
                                                (timeMillis - launchTimeMillis) < 10000
                                            } else {
                                                null
                                            }.toString()
                                        }, inline = true
                                    ),
                                    context.moduleDatastore.data.firstOrNull()?.let {
                                        DiscordWebhook.Embed.EmbedField(
                                            name = "Selected Module ID",
                                            value = if (it.selectedModuleId == ModulePreferences.DEFAULT.selectedModuleId) {
                                                "None"
                                            } else {
                                                it.selectedModuleId
                                            }
                                        )
                                    },
                                ),
                                color = 0xB263A5,
                            )
                        ),
                    ), files = listOfNotNull(errorContent.getString(ReportField.STACK_TRACE)?.let {
                        DiscordWebhook.Embed.EmbedFile(
                            fileName = "crash.log", fileContents = it
                        )
                    })
                )

                client.post(
                    // The URL is stored as a secret
                    url = BuildConfig.WEBHOOK_URL, requestBody = MultipartBody.Builder().apply {
                        setType(MultipartBody.FORM)
                        addFormDataPart(
                            "payload_json", Json.encodeToString(parsed.payloadJson)
                        )
                        parsed.files.forEach { embedFile ->
                            addFormDataPart(name = embedFile.fileName,
                                filename = embedFile.fileName,
                                body = object : RequestBody() {
                                    override fun contentType(): MediaType? {
                                        return embedFile.contentType.toMediaTypeOrNull()
                                    }

                                    override fun writeTo(sink: BufferedSink) {
                                        sink.writeUtf8(embedFile.fileContents)
                                    }
                                })
                        }
                    }.build()
                ).apply {
                    if (!isSuccessful && code != 200) {
                        Log.e(
                            "DiscordReporter",
                            "Failed to send crash report to the Developers! Code: $code"
                        )
                        context.crashReportStore.updateData { preferences ->
                            preferences.copy(
                                unsentCrashReport = CrashReport(
                                    reportPath = context.cacheDir.resolve(
                                        "crash-${errorContent.getString(ReportField.REPORT_ID)}.json"
                                    ).apply {
                                        bufferedWriter().use {
                                            it.write(errorContent.toJSON())
                                        }
                                    }.absolutePath
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@AutoService(ReportSenderFactory::class)
class DiscordReportSenderFactory : ReportSenderFactory {
    override fun create(context: Context, config: CoreConfiguration): ReportSender {
        return DiscordReporter(Requests())
    }

    override fun enabled(config: CoreConfiguration): Boolean {
        return true
    }
}