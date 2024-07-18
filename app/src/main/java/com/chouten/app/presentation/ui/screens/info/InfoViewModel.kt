package com.chouten.app.presentation.ui.screens.info

import android.app.Application
import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chouten.app.common.Resource
import com.chouten.app.common.UiText
import com.chouten.app.domain.model.LogEntry
import com.chouten.app.domain.proto.moduleDatastore
import com.chouten.app.domain.repository.ModuleEngine
import com.chouten.app.domain.use_case.log_use_cases.LogUseCases
import com.chouten.app.domain.use_case.module_use_cases.ModuleUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import java.net.URLDecoder
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@Serializable
@Parcelize
data class InfoResult(
    val titles: Titles,
    val tags: List<String>? = listOf(),
    val description: String,
    val poster: String,
    val banner: String? = poster,
    val status: MediaStatus,
    val mediaType: MediaType,
    val seasons: List<SeasonData>? = listOf(),
    var mediaList: List<MediaList>? = listOf()
) : Parcelable {

    @Serializable(with = MediaStatusSerializer::class)
    enum class MediaStatus {
        COMPLETED, CURRENT, HIATUS, NOT_RELEASED, UNKNOWN;

        override fun toString(): String {
            return super.name.split("_").joinToString(" ") { s ->
                s.lowercase()
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            }
        }
    }

    class MediaStatusSerializer : KSerializer<MediaStatus> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("mediaStatus", PrimitiveKind.INT)

        override fun deserialize(decoder: Decoder): MediaStatus {
            val v = decoder.decodeInt()
            return MediaStatus.entries.find { it.ordinal == v } ?: throw Exception()
        }

        override fun serialize(encoder: Encoder, value: MediaStatus) {
            encoder.encodeInt(value.ordinal)
        }
    }

    @Serializable(with = MediaTypeSerializer::class)
    enum class MediaType {
        EPISODES, CHAPTERS, UNKNOWN;

        override fun toString(): String {
            return super.name.split("_").joinToString(" ") { s ->
                s.lowercase()
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            }
        }
    }

    class MediaTypeSerializer : KSerializer<MediaType> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("mediaType", PrimitiveKind.INT)

        override fun deserialize(decoder: Decoder): MediaType {
            return MediaType.entries.find { it.ordinal == decoder.decodeInt() } ?: throw Exception()
        }

        override fun serialize(encoder: Encoder, value: MediaType) {
            encoder.encodeInt(value.ordinal)
        }
    }

    @Serializable
    @Parcelize
    data class Titles(
        val primary: String, val secondary: String? = null
    ) : Parcelable

    @Serializable
    @Parcelize
    data class SeasonData(
        val name: String, val url: String, var selected: Boolean?
    ) : Parcelable

    @Serializable
    @Parcelize
    data class MediaList(
        val title: String, var pagination: List<Pagination>
    ) : Parcelable {

        @Serializable
        @Parcelize
        data class MediaItem(
            val url: String,
            val number: Double,
            val title: String? = null,
            val language: String? = null,
            val description: String? = null,
            val thumbnail: String? = null
        ) : Parcelable
    }

    @Serializable
    @Parcelize
    data class Pagination(
        val id: String, val title: String, val items: List<MediaList.MediaItem>
    ) : Parcelable
}

//data class InfoResult(
//    val id: String?,
//    val titles: Titles,
//    val epListURLs: List<String>,
//    val altTitles: List<String>?,
//    val description: String?,
//    val poster: String,
//    val banner: String?,
//    val status: String?,
//    val totalMediaCount: Int?,
//    val mediaType: String,
//    val seasons: List<Season>?,
//    val mediaList: List<MediaListItem>?,
//) : Parcelable {
//    @Serializable
//    @Parcelize
//    data class MediaListItem(
//        val title: String, val list: List<MediaItem>
//    ) : Parcelable
//
//    @Serializable
//    @Parcelize
//    data class Titles(
//        val primary: String, val secondary: String?
//    ) : Parcelable
//
//    @Serializable
//    @Parcelize
//    data class MediaItem(
//        val url: String,
//        val number: Float?,
//        val title: String?,
//        val description: String?,
//        val image: String?,
//    ) : Parcelable {
//        override fun toString(): String {
//            return Json.encodeToString(serializer(), this);
//        }
//    }
//
//    @Serializable
//    @Parcelize
//    data class Season(
//        val name: String,
//        val url: String,
//    ) : Parcelable
//}

@HiltViewModel
class InfoViewModel @Inject constructor(
    val application: Application,
    private val moduleUseCases: ModuleUseCases,
    val engine: ModuleEngine,
    private val logUseCases: LogUseCases,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private var _title = ""
    private var _url = ""

    private val infoFlow = MutableStateFlow("")
    val infoResults: MutableStateFlow<Resource<InfoResult>> =
        MutableStateFlow(Resource.Uninitialized())

    private val episodeFlow = MutableStateFlow("[]")
    val episodeResults: MutableStateFlow<Resource<List<InfoResult.MediaList>>> =
        MutableStateFlow(Resource.Uninitialized())

    /**
     * The list of media items. Made from concatenating the [infoResults] and [episodeList] data
     */
//    fun getMediaList(): List<InfoResult.MediaListItem> {
//        return runBlocking {
//            infoResults.firstOrNull()?.data?.mediaList?.plus(
//                episodeList.firstOrNull()?.data ?: listOf()
//            ) ?: listOf()
//        }
//    }

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

//    private var _selectedSeason: MutableStateFlow<InfoResult.Season?> = MutableStateFlow(null)
//    val selectedSeason: StateFlow<InfoResult.Season?> = _selectedSeason

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
        engine.scope = viewModelScope;
        engine.registerObservable("info", infoFlow)
        engine.registerObservable("episodes", episodeFlow)
        engine.registerInterceptor("logging") {
            viewModelScope.launch {
                logUseCases.insertLog(
                    LogEntry(
                        entryContent = it.toString()
                    )
                )
            }
        }

        viewModelScope.launch {
            reloadCode()
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
            infoFlow.collectLatest {
                if (it.isBlank()) return@collectLatest
                try {
                    Log.d("InfoViewModel", "We have the info result $it")
                    infoResults.emit(Resource.Success(Json {
                        ignoreUnknownKeys = true
                    }.decodeFromString<InfoResult>(it)))
                } catch (e: Exception) {
                    e.printStackTrace()
                    infoResults.emit(
                        Resource.Error(
                            UiText.Literal("Could not get Info Results").string(application)
                        )
                    )
                }
            }
        }

        viewModelScope.launch {
            episodeFlow.collectLatest {
                try {
                    Log.d("InfoViewModel", "We have the episode result $it")
                    episodeResults.emit(Resource.Success(Json {
                        ignoreUnknownKeys = true
                    }.decodeFromString<List<InfoResult.MediaList>>(it)))
                } catch (e: Exception) {
                    e.printStackTrace()
                    infoResults.emit(
                        Resource.Error(
                            UiText.Literal("Could not get Episode Results").string(application)
                        )
                    )
                }
            }
        }
    }

    private suspend fun reloadCode() {
        withContext(Dispatchers.IO) {
            val moduleId = application.moduleDatastore.data.firstOrNull()?.selectedModuleId?.let {
                it.ifBlank { return@let null }
            } ?: return@withContext
            val module = moduleUseCases.getModuleUris().find {
                it.id == moduleId
            } ?: return@withContext
            code = module.code ?: run {
                logUseCases.insertLog(LogEntry(entryContent = "Failed to find info code for ${module.name}"))
                return@run ""
            }
            withContext(Dispatchers.Main) {
                engine.load(
                    code
                )
            }
        }
    }

    suspend fun getInfo(title: String, url: String) {
        withContext(Dispatchers.IO) {
            _title = URLDecoder.decode(title, "UTF-8")
            _url = URLDecoder.decode(url, "UTF-8")
        }
        engine.evaluateJavascript(
            """
                // Not doing this makes the defaultSource not load ?? Maybe something to do with giving
                // the WebView time to parse the JS?
                if (defaultSource == undefined || typeof defaultSource["info"] != "function") {
                    console.log("Could not load Info Function on `defaultSource`!");
                } else {
                    (async function() {
                        var res = await defaultSource.info('$_url');
                        console.log("We got our result. Sending payload")
                        if (Native["sendResult"] == null || Native["sendResult"] == undefined) {
                           console.log("sendResult not found");
                        }
                        Native.sendResult(JSON.stringify({key: "info", value: JSON.stringify(res)}));
                    })();
                }
        """.trimIndent()
        ) {}
    }

    suspend fun getEpisodes(season: InfoResult.SeasonData, offset: Int = 0) {
        engine.evaluateJavascript(
            """
            // Not doing this makes the defaultSource not load ?? Maybe something to do with giving
                // the WebView time to parse the JS?
                if (defaultSource == undefined || typeof defaultSource["info"] != "function") {
                    console.log("Could not load Info Function on `defaultSource`!");
                } else {
                    (async function() {
                        var res = await defaultSource.media('${season.url}');
                        console.log("We got our result. Sending payload")
                        if (Native["sendResult"] == null || Native["sendResult"] == undefined) {
                           console.log("sendResult not found");
                        }
                        Native.sendResult(JSON.stringify({key: "episodes", value: JSON.stringify(res)}));
                    })();
                }
        """.trimIndent()
        ) {}
//        epListHandler.initialize(application) { res ->
//            if (res.action == Payloads_V2.Action_V2.ERROR) {
//                viewModelScope.launch {
//                    log(content = "Failed to get episodes for $_title.\n${res.result.result}")
//                    _episodeList.emit(
//                        Resource.Error(
//                            message = "Failed to get episodes for $_title", data = null
//                        )
//                    )
//                }
//                return@initialize
//            }
//            viewModelScope.launch {
//                // Combine the results of `episodeList` and `res.result.result`
//                // into a single list. This is done because we don't load all the episodes at the same
//                // time - previous episodes may be contained in the flow and we don't want
//                // to lose them.
//                // Using a set means that duplicate entries will not be added to the list.
//                val episodes: MutableSet<InfoResult.MediaListItem> =
//                    episodeList.firstOrNull()?.data?.toMutableSet() ?: mutableSetOf()
//                episodes.addAll(res.result.result)
//                if (episodes.size > 1) {
//                    val collectedInfoResults = infoResults.firstOrNull()?.data
//                    collectedInfoResults?.let {
//                        _infoResults.emit(
//                            Resource.Success(
//                                it.copy(
//                                    seasons = (it.seasons?.plus(episodes.mapIndexed { index, season ->
//                                        val resultSeason = InfoResult.Season(
//                                            name = season.title,
//                                            url = season.list.firstOrNull()?.url ?: ""
//                                        )
//                                        if (it.seasons.size.plus(
//                                                it.mediaList?.size ?: 0
//                                            ) == 0 && index == 0
//                                        ) {
//                                            _selectedSeason.emit(resultSeason)
//                                        }
//                                        resultSeason
//                                    })?.toSet()?.toList())
//                                )
//                            )
//                        )
//                    }
//                }
//                _episodeList.emit(Resource.Success(episodes.toList()))
//                if (_paginatedAll) {
//                    paginatedAll = true
//                }
//            }
//        }
//        epListHandler.load(
//            getCode(), WebviewHandler.Companion.WebviewPayload(
//                query = eplistUrls.getOrNull(offset) ?: "",
//                action = Payloads_V2.Action_V2.GET_EPISODE_LIST
//            )
//        )

        // We have finished loading all the episodes
//        if (offset + 1 == eplistUrls.size) {
//            _paginatedAll = true
//        }
    }

//    fun changeSeason(season: InfoResult.Season) {
//        viewModelScope.launch {
//            if (season == selectedSeason.firstOrNull()) return@launch
//            _selectedSeason.emit(infoResults.value.data?.seasons?.find { it == season })
//            // If the media doesn't appear to have been loaded, request it using the season url
//            if (getMediaList().find { it.title == season.name } == null) {
//                cachedSwitchResults.emit(
//                    mutableMapOf()
//                )
//                infoResults.firstOrNull()?.data?.let {
//                    _infoResults.emit(
//                        Resource.Success(
//                            it.copy(
//                                epListURLs = listOf(season.url)
//                            )
//                        )
//                    )
//                }
//                _episodeList.emit(
//                    Resource.Uninitialized()
//                )
//            }
//        }
//    }

    suspend fun saveMediaBundle() {
//        <List<InfoResult.MediaList.MediaItem>>
        val season = episodeResults.firstOrNull()?.data?.firstOrNull()?.pagination?.firstOrNull()?.items ?: listOf()
//        val media = getMediaList().sortedBy {
//            // We want the selected season to be the first index
//            if (it.title == season?.name) {
//                0
//            } else {
//                1
//            }
//        }
        withContext(Dispatchers.IO) {
            application.applicationContext.cacheDir.resolve("${FILE_PREFIX}_media.json")
                .bufferedWriter().use {
                    it.write(
                        Json.encodeToString(season)
                    )
                }
        }
    }
}