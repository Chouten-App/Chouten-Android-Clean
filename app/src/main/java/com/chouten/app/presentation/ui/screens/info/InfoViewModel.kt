package com.chouten.app.presentation.ui.screens.info

import android.app.Application
import android.os.Parcelable
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
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URLDecoder
import javax.inject.Inject

@Serializable
@Parcelize
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
) : Parcelable {
    @Serializable
    @Parcelize
    data class MediaListItem(
        val title: String, val list: List<MediaItem>
    ) : Parcelable

    @Serializable
    @Parcelize
    data class Titles(
        val primary: String, val secondary: String?
    ) : Parcelable

    @Serializable
    @Parcelize
    data class MediaItem(
        val url: String,
        val number: Float?,
        val title: String?,
        val description: String?,
        val image: String?,
    ) : Parcelable {
        override fun toString(): String {
            return Json.encodeToString(serializer(), this);
        }
    }

    @Serializable
    @Parcelize
    data class Season(
        val name: String,
        val url: String,
    ) : Parcelable
}


@HiltViewModel
class InfoViewModel @Inject constructor(
    val application: Application,
    private val moduleUseCases: ModuleUseCases,
    private val metadataHandler: WebviewHandler<Payloads_V2.Action_V2, Payloads_V2.GenericPayload<InfoResult>>,
    val epListHandler: WebviewHandler<Payloads_V2.Action_V2, Payloads_V2.GenericPayload<List<InfoResult.MediaListItem>>>,
    private val logUseCases: LogUseCases,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private var _title = ""
    private var _url = ""

    val infoResults: StateFlow<Resource<InfoResult>> =
        savedStateHandle.getStateFlow("infoResults", Resource.Uninitialized())

    val episodeList: StateFlow<Resource<List<InfoResult.MediaListItem>>> =
        savedStateHandle.getStateFlow("epListResults", Resource.Uninitialized())

    /**
     * Whether or not the episode list has been paginated to the end
     * NOTE: This does NOT mean that the webview itself has finished returning
     * all of it's episodes.
     * @see paginatedAll
     */
    private var _paginatedAll = false

    /**
     * Whether or not the webview has finished returning all of it's episodes
     */
    var paginatedAll = false
        private set

    var selectedSeason: InfoResult.Season? = null
        private set

    var seasonCount = 0
        private set

    init {
        viewModelScope.launch {
            metadataHandler.logFn = { log(content = it) }
            epListHandler.logFn = { log(content = it) }

            metadataHandler.initialize(application) { res ->
                if (res.action == Payloads_V2.Action_V2.ERROR) {
                    viewModelScope.launch {
                        log(content = "Failed to get info for $_title.\n${res.result.result}")
                        savedStateHandle["infoResults"] = Resource.Error(
                            message = "Failed to get info for $_title", data = null
                        )
                    }
                    return@initialize
                }
                viewModelScope.launch {
                    if (selectedSeason == null) selectedSeason =
                        res.result.result.seasons?.firstOrNull()
                    seasonCount = res.result.result.seasons?.size ?: 1
                    savedStateHandle["infoResults"] = Resource.Success(res.result.result)
                }
            }

        }
    }

    private suspend fun getCode(): String {
        return moduleUseCases.getModuleUris().find {
            it.id == application.moduleDatastore.data.firstOrNull()?.selectedModuleId
        }?.code?.info?.getOrNull(0)?.code ?: ""
    }

    suspend fun getInfo(title: String, url: String) {
        withContext(Dispatchers.IO) {
            _title = URLDecoder.decode(title, "UTF-8")
            _url = URLDecoder.decode(url, "UTF-8")
        }
        metadataHandler.load(
            getCode(), WebviewHandler.Companion.WebviewPayload(
                query = _url, action = Payloads_V2.Action_V2.GET_METADATA
            )
        )
    }

    suspend fun getEpisodes(eplistUrls: List<String>, offset: Int = 0) {
        epListHandler.initialize(application) { res ->
            if (res.action == Payloads_V2.Action_V2.ERROR) {
                viewModelScope.launch {
                    log(content = "Failed to get episodes for $_title.\n${res.result.result}")
                    savedStateHandle["epListResults"] = Resource.Error(
                        message = "Failed to get episodes for $_title", data = null
                    )
                }
                return@initialize
            }
            viewModelScope.launch {
                // Combine the results of `savedStateHandle["epListResults"]` and `res.result.result`
                // into a single list. This is done because we don't load all the episodes at the same
                // time - previous episodes may be contained in the savedStateHandle and we don't want
                // to lose them.
                val episodes: MutableList<InfoResult.MediaListItem> =
                    savedStateHandle.get<Resource<List<InfoResult.MediaListItem>>>("epListResults")?.data?.toMutableList()
                        ?: mutableListOf()
                episodes.addAll(res.result.result)
                savedStateHandle["epListResults"] = Resource.Success(episodes)
                if (_paginatedAll) {
                    paginatedAll = true
                }
            }
        }
        epListHandler.load(
            getCode(), WebviewHandler.Companion.WebviewPayload(
                query = eplistUrls.getOrNull(offset) ?: "",
                action = Payloads_V2.Action_V2.GET_EPISODE_LIST
            )
        )

        // We have finished loading all the episodes
        if (offset + 1 == eplistUrls.size) {
            _paginatedAll = true
        }
    }

    fun changeSeason(season: InfoResult.Season) {
        selectedSeason = infoResults.value.data?.seasons?.find { it == season }
        savedStateHandle["infoResults"] = Resource.Uninitialized<InfoResult>()
        savedStateHandle["epListResults"] = Resource.Uninitialized<List<InfoResult.MediaListItem>>()
    }

    fun log(title: String = "Webview Handler", content: String) {
        viewModelScope.launch {
            logUseCases.insertLog(
                LogEntry(
                    entryHeader = title, entryContent = content
                )
            )
        }
    }
}