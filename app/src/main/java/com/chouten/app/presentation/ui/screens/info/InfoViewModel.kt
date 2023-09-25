package com.chouten.app.presentation.ui.screens.info

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chouten.app.common.Resource
import com.chouten.app.domain.model.LogEntry
import com.chouten.app.domain.model.Payloads_V2
import com.chouten.app.domain.proto.moduleDatastore
import com.chouten.app.domain.repository.WebviewHandler
import com.chouten.app.domain.use_case.log_use_cases.LogUseCases
import com.chouten.app.domain.use_case.module_use_cases.ModuleUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URLDecoder
import javax.inject.Inject

@Serializable
data class InfoResult(
    val id: String?,
    val titles: Titles,
    val epListURLs: List<String>,
    val altTitles: List<String>?,
    val description: String?,
    val poster: String,
    val banner: String?,
    val status: String?,
    val totalMediaCount: Int?,
    val mediaType: String,
    val seasons: List<Season>?,
    val mediaList: List<MediaListItem>?,
) {
    @Serializable
    data class MediaListItem(
        val title: String, val list: List<MediaItem>
    )

    @Serializable
    data class Titles(
        val primary: String, val secondary: String?
    )

    @Serializable
    data class MediaItem(
        val url: String,
        val number: Float?,
        val title: String?,
        val description: String?,
        val image: String?,
    ) {
        override fun toString(): String {
            return Json.encodeToString(serializer(), this);
        }
    }

    @Serializable
    data class Season(
        val name: String,
        val url: String,
    )
}


@HiltViewModel
class InfoViewModel @Inject constructor(
    val application: Application,
    val moduleUseCases: ModuleUseCases,
    val metadataHandler: WebviewHandler<Payloads_V2.Action_V2, Payloads_V2.GenericPayload<InfoResult>>,
    val epListHandler: WebviewHandler<Payloads_V2.Action_V2, Payloads_V2.GenericPayload<List<InfoResult.MediaListItem>>>,
    private val logUseCases: LogUseCases,
    val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private var _title = ""
    private var _url = ""

    val infoResults: StateFlow<Resource<InfoResult>> =
        savedStateHandle.getStateFlow("infoResults", Resource.Uninitialized())

    val episodeList: StateFlow<Resource<InfoResult.MediaListItem>> =
        savedStateHandle.getStateFlow("epListResults", Resource.Uninitialized())

    private lateinit var code: String

    init {
        viewModelScope.launch {
            val logLambda: (String) -> Unit = { message ->
                viewModelScope.launch {
                    logUseCases.insertLog(
                        LogEntry(
                            entryHeader = "Webview Log", entryContent = message
                        )
                    )
                }
            }

            metadataHandler.logFn = logLambda
            epListHandler.logFn = logLambda

            metadataHandler.initialize(application) { res ->
                if (res.action == Payloads_V2.Action_V2.ERROR) {
                    viewModelScope.launch {
                        savedStateHandle["infoResults"] = Resource.Error(
                            message = "Failed to get info for $_title", data = null
                        )
                    }
                    return@initialize
                }
                viewModelScope.launch {
                    savedStateHandle["infoResults"] = Resource.Success(res.result.result)
                }
            }

            code = moduleUseCases.getModuleUris().find {
                it.id == application.moduleDatastore.data.firstOrNull()?.selectedModuleId
            }?.code?.info?.getOrNull(0)?.code ?: return@launch
        }
    }

    suspend fun getInfo(title: String, url: String) {
        withContext(Dispatchers.IO) {
            _title = URLDecoder.decode(title, "UTF-8")
            _url = URLDecoder.decode(url, "UTF-8")
        }
        metadataHandler.load(
            code, WebviewHandler.Companion.WebviewPayload(
                query = _url, action = Payloads_V2.Action_V2.GET_METADATA
            )
        )
    }

    suspend fun getEpisodes(eplistUrls: List<String>, offset: Int = 0) {
        metadataHandler.destroy()
        epListHandler.initialize(application) { res ->
            if (res.action == Payloads_V2.Action_V2.ERROR) {
                viewModelScope.launch {
                    savedStateHandle["epListResults"] = Resource.Error(
                        message = "Failed to get episodes for $_title", data = null
                    )
                }
                return@initialize
            }
            viewModelScope.launch {
                savedStateHandle["epListResults"] = Resource.Success(res.result.result)
            }
        }
        epListHandler.load(
            code, WebviewHandler.Companion.WebviewPayload(
                query = eplistUrls.getOrNull(0) ?: "",
                action = Payloads_V2.Action_V2.GET_EPISODE_LIST
            )
        )
    }
}