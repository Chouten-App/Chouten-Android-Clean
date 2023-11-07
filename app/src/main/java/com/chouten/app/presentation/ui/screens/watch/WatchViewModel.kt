package com.chouten.app.presentation.ui.screens.watch

import android.app.Application
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chouten.app.domain.model.LogEntry
import com.chouten.app.domain.model.Payloads_V2
import com.chouten.app.domain.proto.moduleDatastore
import com.chouten.app.domain.repository.WebviewHandler
import com.chouten.app.domain.use_case.log_use_cases.LogUseCases
import com.chouten.app.domain.use_case.module_use_cases.ModuleUseCases
import com.chouten.app.presentation.ui.screens.info.InfoResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject

@Serializable
@Parcelize
data class WatchResult(
    val sources: List<Source>,
    val subtitles: List<Subtitles>?,
    val skips: List<SkipTimes>?,
    val headers: Map<String, String>?,
) : Parcelable {

    @Serializable
    @Parcelize
    data class ServerData(
        val title: String, val list: List<Server>
    ) : Parcelable

    @Serializable
    @Parcelize
    data class Server(
        val name: String,
        val url: String,
    ) : Parcelable

    @Serializable
    @Parcelize
    data class Source(
        val file: String,
        val type: String,
    ) : Parcelable

    @Serializable
    @Parcelize
    data class Subtitles(
        val url: String,
        val language: String,
    ) : Parcelable

    @Serializable
    @Parcelize
    data class SkipTimes(
        val start: Double, val end: Double, val type: String
    ) : Parcelable
}

@Serializable
@Parcelize
data class WatchViewModelState(
    val isServerSet: Boolean,
    val isSourceSet: Boolean,
    val error: List<String> = listOf()
) : Parcelable {
    companion object {
        val DEFAULT = WatchViewModelState(
            isServerSet = false,
            isSourceSet = false
        )
    }
}

@HiltViewModel
class WatchViewModel @Inject constructor(
    private val serverHandler: WebviewHandler<Payloads_V2.Action_V2, Payloads_V2.GenericPayload<List<WatchResult.ServerData>>>,
    private val videoHandler: WebviewHandler<Payloads_V2.Action_V2, Payloads_V2.GenericPayload<WatchResult>>,
    private val moduleUseCases: ModuleUseCases,
    val savedStateHandle: SavedStateHandle,
    val application: Application,
    private val logUseCases: LogUseCases
) : ViewModel() {

    private var url: String = ""

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    private val status: StateFlow<WatchViewModelState> =
        savedStateHandle.getStateFlow(STATUS, WatchViewModelState.DEFAULT)

    private val FILE_PREFIX = WatchView.FILE_PREFIX

    // We store the media here since it MAY be too large to store in the savedStateHandle
    private var media: List<InfoResult.MediaListItem> = listOf()

    init {
        // Purge all watch data which is older than 30 minutes
        viewModelScope.launch {
            // TODO: Migrate to some sort of runLogging lambda
            try {
                application.cacheDir.listFiles()?.forEach { file ->
                    if (file.name.endsWith("_lock")) {
                        if (file.lastModified() >= System.currentTimeMillis() - 30 * 60 * 1000) return@forEach
                        // Destroy the lock file and all associated data
                        val uuid = file.name.removeSuffix("_lock")

                        // Don't destroy the in-use files
                        if (uuid == FILE_PREFIX) return@forEach

                        application.cacheDir.resolve("${uuid}_server.json").delete()
                        application.cacheDir.resolve("${uuid}_sources.json").delete()
                        application.cacheDir.resolve("${uuid}_media.json").delete()
                        file.delete()
                    }
                }
            } catch (e: Exception) {
                logUseCases.insertLog(
                    LogEntry(
                        entryHeader = "Failure Clearing Caches",
                        entryContent =
                        "Failed to purge watch data: ${e.message}"
                    )
                )
            }
        }

        // Get the Media Data
        viewModelScope.launch {
            try {
                application.cacheDir.resolve("${FILE_PREFIX}_media.json").useLines { lines ->
                    val text = lines.joinToString("\n")
                    media = json.decodeFromString<List<InfoResult.MediaListItem>>(text)
                }
            } catch (e: Exception) {
                logUseCases.insertLog(
                    LogEntry(
                        entryHeader = "Failure Loading Media",
                        entryContent =
                        "Failed to load media data: ${e.message}"
                    )
                )
                media = listOf()
            }
        }

        viewModelScope.launch {
            serverHandler.initialize(application) { res ->
                if (res.action == Payloads_V2.Action_V2.ERROR) {
                    viewModelScope.launch {
                        val statusValue = status.firstOrNull()
                        savedStateHandle[STATUS] = statusValue?.copy(
                            error = statusValue.error + "Failed to get servers for $url"
                        )
                    }
                    return@initialize
                }
                viewModelScope.launch {
                    val statusValue = status.firstOrNull()

                    val errors = mutableListOf<String>()
                    // Save the servers to a file
                    application.cacheDir.resolve("${FILE_PREFIX}_server.json").bufferedWriter()
                        .use {
                            try {
                                it.write(json.encodeToString(res.result.result))
                            } catch (e: IOException) {
                                errors.add(e.message ?: "Unknown error")
                            }
                        }

                    savedStateHandle[STATUS] = statusValue?.copy(
                        isServerSet = true,
                        error = statusValue.error + errors
                    )
                }
            }

            videoHandler.initialize(application) { res ->
                if (res.action == Payloads_V2.Action_V2.ERROR) {
                    viewModelScope.launch {
                        val statusValue = status.firstOrNull()
                        savedStateHandle[STATUS] = statusValue?.copy(
                            error = statusValue.error + "Failed to get sources for $url"
                        )
                    }
                    return@initialize
                }

                viewModelScope.launch {
                    val statusValue = status.firstOrNull()

                    val errors = mutableListOf<String>()
                    // Save the sources to a file
                    application.cacheDir.resolve("${FILE_PREFIX}_sources.json").bufferedWriter()
                        .use {
                            try {
                                it.write(json.encodeToString(res.result.result))
                            } catch (e: IOException) {
                                errors.add(e.message ?: "Unknown error")
                            }
                        }

                    savedStateHandle[STATUS] = statusValue?.copy(
                        isSourceSet = true,
                        error = statusValue.error + errors
                    )
                }
            }
        }
    }

    private suspend fun getCode(): String {
        return moduleUseCases.getModuleUris().find {
            it.id == application.moduleDatastore.data.firstOrNull()?.selectedModuleId
        }?.code?.mediaConsume?.getOrNull(0)?.code ?: ""
    }

    suspend fun getServers(bundle: WatchBundle, bundleId: Int) {
        url = media.getOrNull(0)?.list?.getOrElse(bundleId) {
            if (media.isEmpty()) {
                null
            } else throw IllegalArgumentException("Selected media index is out of bounds")
        }?.url ?: bundle.url

        serverHandler.load(
            getCode(), WebviewHandler.Companion.WebviewPayload(
                action = Payloads_V2.Action_V2.GET_SERVER, query = url
            )
        )
    }

    suspend fun getSource(url: String) {
        videoHandler.load(
            getCode(), WebviewHandler.Companion.WebviewPayload(
                action = Payloads_V2.Action_V2.GET_VIDEO, query = url
            )
        )
    }

    companion object {
        const val STATUS = "status"
    }
}