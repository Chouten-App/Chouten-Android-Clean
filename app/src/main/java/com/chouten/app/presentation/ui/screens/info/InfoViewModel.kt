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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLDecoder
import java.util.UUID
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

    private val _infoResults: MutableStateFlow<Resource<InfoResult>> =
        MutableStateFlow(Resource.Uninitialized())
    val infoResults: StateFlow<Resource<InfoResult>> = _infoResults

    private val _episodeList: MutableStateFlow<Resource<List<InfoResult.MediaListItem>>> =
        MutableStateFlow(Resource.Uninitialized())
    val episodeList: StateFlow<Resource<List<InfoResult.MediaListItem>>> = _episodeList

    /**
     * The list of media items. Made from concatenating the [infoResults] and [episodeList] data
     */
    fun getMediaList(): List<InfoResult.MediaListItem> {
        return runBlocking {
            infoResults.firstOrNull()?.data?.mediaList?.plus(
                episodeList.firstOrNull()?.data ?: listOf()
            ) ?: listOf()
        }
    }

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

    private var _selectedSeason: MutableStateFlow<InfoResult.Season?> = MutableStateFlow(null)
    val selectedSeason: StateFlow<InfoResult.Season?> = _selectedSeason

    var seasonCount = 0
        private set

    /**
     * Prefix for the files used to store the media data
     * The sources, servers and bundle are stored within their own <PREFIX>_<source|server|bundle>.json files
     */
    // TODO: Look into using this to determine if we should re-fetch the data
    lateinit var FILE_PREFIX: UUID

    private lateinit var code: String

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                FILE_PREFIX = UUID.randomUUID().let {
                    // Check if a lock file exists for the current media
                    // If it does, we can't use the UUID
                    var uuid = it
                    while (application.cacheDir.resolve("${uuid}_lock").exists()) {
                        // If it does, we generate a new UUID
                        uuid = UUID.randomUUID()
                    }
                    application.cacheDir.resolve("${uuid}_lock").createNewFile()
                    uuid
                }
            }
        }
        viewModelScope.launch {
            metadataHandler.logFn = { log(content = it) }
            epListHandler.logFn = { log(content = it) }

            metadataHandler.initialize(application) { res ->
                if (res.action == Payloads_V2.Action_V2.ERROR) {
                    viewModelScope.launch {
                        log(content = "Failed to get info for $_title.\n${res.result.result}")
                        _infoResults.emit(
                            Resource.Error(
                                message = "Failed to get info for $_title", data = null
                            )
                        )
                    }
                    return@initialize
                }
                viewModelScope.launch {
                    if (_selectedSeason.firstOrNull() == null) _selectedSeason.emit(
                        res.result.result.seasons?.firstOrNull()
                    )
                    seasonCount = res.result.result.seasons?.size ?: 1
                    _infoResults.emit(infoResults.firstOrNull()?.let {
                        if (it !is Resource.Success) return@let null
                        Resource.Success(
                            it.data.copy(
                                epListURLs = res.result.result.epListURLs
                            )
                        )
                    } ?: Resource.Success(res.result.result))
                }
            }
        }
    }

    private suspend fun getCode(): String {
        return if (!::code.isInitialized) {
            withContext(Dispatchers.IO) {
                val moduleId =
                    application.moduleDatastore.data.firstOrNull()?.selectedModuleId
                        ?: return@withContext ""
                val module = moduleUseCases.getModuleUris().find {
                    it.id == moduleId
                } ?: return@withContext ""
                code = module.code?.info?.getOrNull(0)?.code ?: run {
                    log(content = "Failed to find info code for ${module.name}")
                    return@run ""
                }
                code
            }
        } else code
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
                    _episodeList.emit(
                        Resource.Error(
                            message = "Failed to get episodes for $_title", data = null
                        )
                    )
                }
                return@initialize
            }
            viewModelScope.launch {
                // Combine the results of `episodeList` and `res.result.result`
                // into a single list. This is done because we don't load all the episodes at the same
                // time - previous episodes may be contained in the flow and we don't want
                // to lose them.
                // Using a set means that duplicate entries will not be added to the list.
                val episodes: MutableSet<InfoResult.MediaListItem> =
                    episodeList.firstOrNull()?.data?.toMutableSet()
                        ?: mutableSetOf()
                episodes.addAll(res.result.result)
                if (episodes.size > 1) {
                    val collectedInfoResults = infoResults.firstOrNull()?.data
                    collectedInfoResults?.let {
                        _infoResults.emit(
                            Resource.Success(
                                it.copy(
                                    seasons = (it.seasons?.plus(episodes.mapIndexed { index, season ->
                                        val resultSeason = InfoResult.Season(
                                            name = season.title,
                                            url = season.list.firstOrNull()?.url ?: ""
                                        )
                                        if (it.seasons.size.plus(
                                                it.mediaList?.size ?: 0
                                            ) == 0 && index == 0
                                        ) {
                                            _selectedSeason.emit(resultSeason)
                                        }
                                        resultSeason
                                    })?.toSet()?.toList())
                                )
                            )
                        )
                    }
                }
                _episodeList.emit(Resource.Success(episodes.toList()))
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
        viewModelScope.launch {
            if (season == selectedSeason.firstOrNull()) return@launch
            _selectedSeason.emit(infoResults.value.data?.seasons?.find { it == season })
            // If the media doesn't appear to have been loaded, request it using the season url
            if (getMediaList().find { it.title == season.name } == null) {
                infoResults.firstOrNull()?.data?.let {
                    _infoResults.emit(
                        Resource.Success(
                            it.copy(
                                epListURLs = listOf(season.url)
                            )
                        )
                    )
                }
                _episodeList.emit(
                    Resource.Uninitialized()
                )
            }
        }
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

    suspend fun saveMediaBundle() {
        val season = selectedSeason.firstOrNull()
        val media = getMediaList().sortedBy {
            // We want the selected season to be the first index
            if (it.title == season?.name) {
                0
            } else {
                1
            }
        }
        withContext(Dispatchers.IO) {
            application.applicationContext.cacheDir.resolve("${FILE_PREFIX}_media.json")
                .bufferedWriter().use {
                    it.write(
                        Json.encodeToString(media)
                    )
                }
        }
    }
}