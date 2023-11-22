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

/**
 * @param options The (2) options to be displayed in the switch
 * @param default The index of the default option (0 or 1)
 * @param cache Whether or not the webview should cache the result
 * @param includeInfo Whether or not the webview should redo the info request
 */
@Serializable
data class SwitchConfig(
    val options: Array<String>, val default: Int, val cache: Boolean, val includeInfo: Boolean
) {
    companion object {
        /**
         * Whether or not the switch is toggled
         * If the default is 1 and the toggle is "on", the switch is NOT toggled.
         * If the default is 0 and the toggle is "off", the switch is NOT toggled.
         * @param value The value of the switch
         * @param config The config of the switch
         * @return Whether or not the switch is toggled from it's default state
         */
        fun isToggled(value: Boolean, config: SwitchConfig): Boolean {
            return value.xor((config.default == 0))
        }
    }
}

@HiltViewModel
class InfoViewModel @Inject constructor(
    val application: Application,
    private val moduleUseCases: ModuleUseCases,
    private val switchConfigHandler: WebviewHandler<Payloads_V2.Action_V2, Payloads_V2.GenericPayload<SwitchConfig>>,
    private val metadataHandler: WebviewHandler<Payloads_V2.Action_V2, Payloads_V2.GenericPayload<InfoResult>>,
    val epListHandler: WebviewHandler<Payloads_V2.Action_V2, Payloads_V2.GenericPayload<List<InfoResult.MediaListItem>>>,
    private val logUseCases: LogUseCases,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private var _title = ""
    private var _url = ""

    val switchValue: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val cachedSwitchResults: MutableStateFlow<MutableMap<String, Pair<Resource<InfoResult>, Resource<List<InfoResult.MediaListItem>>>>> =
        MutableStateFlow(
            mutableMapOf()
        )

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

    private val _switchConfig: MutableStateFlow<SwitchConfig?> = MutableStateFlow(null)
    val switchConfig: StateFlow<SwitchConfig?> = _switchConfig

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
            switchConfigHandler.logFn = { log(content = it) }
            metadataHandler.logFn = { log(content = it) }
            epListHandler.logFn = { log(content = it) }

            switchConfigHandler.initialize(application) { res ->
                if (res.action == Payloads_V2.Action_V2.ERROR) {
                    viewModelScope.launch {
                        if (code.contains("function getSwitchConfig")) {
                            // Not all modules have a switch config, we should only log if
                            // there is a function but it fails
                            log(content = "Failed to get switch config for $_title.\n${res.result.result}")
                        }
                        switchConfigHandler.destroy()
                    }
                    return@initialize
                }
                viewModelScope.launch {
                    _switchConfig.emit(res.result.result)
                    switchValue.emit(res.result.result.default == 1)

                    metadataHandler.setGenericValue("switchConfig", res.result.result)
                    metadataHandler.setGenericValue("switchValue", res.result.result.default == 1)

                    epListHandler.setGenericValue("switchConfig", res.result.result)
                    epListHandler.setGenericValue("switchValue", res.result.result.default == 1)

                    switchConfigHandler.destroy()
                }
            }
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

                    switchConfig.firstOrNull()?.let { config ->
                        config.options.getOrNull(
                            if (config.isToggled(config = config)) 1 else 0
                        )
                    }?.let {
                        val switchResults = cachedSwitchResults.firstOrNull()
                        if (switchResults?.get(it) == null) {
                            switchResults?.set(
                                it, Pair(
                                    infoResults.firstOrNull() ?: Resource.Uninitialized(),
                                    episodeList.firstOrNull() ?: Resource.Uninitialized()
                                )
                            )?.also {
                                this@InfoViewModel.cachedSwitchResults.emit(switchResults)
                            }
                        }
                    }
                }
            }

            switchConfigHandler.load(
                getCode(), WebviewHandler.Companion.WebviewPayload(
                    query = "", action = Payloads_V2.Action_V2.GET_SWITCH_CONFIG
                )
            )
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
                switchConfig.firstOrNull()?.let { config ->
                    config.options.getOrNull(
                        if (config.isToggled(config = config)) 1 else 0
                    )
                }?.let {
                    cachedSwitchResults.firstOrNull()?.let { switchResults ->
                        if (switchResults[it] == null || switchResults[it]?.second !is Resource.Success) {
                            switchResults[it] = Pair(
                                infoResults.firstOrNull() ?: Resource.Uninitialized(),
                                Resource.Success(episodes.toList())
                            )
                            this@InfoViewModel.cachedSwitchResults.emit(switchResults)
                        }
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
                cachedSwitchResults.emit(
                    mutableMapOf()
                )
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

    suspend fun toggleSwitch(value: Boolean? = null) {
        // We must not the value so we get a double negation
        val toggleValue = value?.not() ?: switchValue.firstOrNull() ?: false
        switchValue.emit(!toggleValue)
        metadataHandler.setGenericValue("switchValue", !toggleValue)
        epListHandler.setGenericValue("switchValue", !toggleValue)

        val config = switchConfig.firstOrNull()
        if (config?.cache == true && cachedSwitchResults.firstOrNull()?.size == 2) {
            val key = config.options.getOrNull(
                if (config.isToggled(!toggleValue)) 1 else 0
            )
            cachedSwitchResults.firstOrNull()?.get(key)?.second?.let {
                _episodeList.emit(it)
            }
            return
        }

        if (config?.includeInfo == true) {
            _infoResults.emit(Resource.Uninitialized())
        }
        _episodeList.emit(Resource.Uninitialized())
    }

    /**
     * Wrapper for [SwitchConfig.Companion.isToggled]
     * @see SwitchConfig.Companion.isToggled
     */
    private fun SwitchConfig.isToggled(
        value: Boolean? = null, config: SwitchConfig? = null
    ): Boolean {
        return runBlocking {
            SwitchConfig.isToggled(
                value ?: switchValue.firstOrNull() ?: return@runBlocking false,
                config ?: switchConfig.firstOrNull() ?: return@runBlocking false
            )
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