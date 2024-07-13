package com.chouten.app.presentation.ui.screens.search

import android.app.Application
import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chouten.app.R
import com.chouten.app.common.Resource
import com.chouten.app.common.UiText
import com.chouten.app.domain.model.LogEntry
import com.chouten.app.domain.proto.moduleDatastore
import com.chouten.app.domain.repository.ModuleEngine
import com.chouten.app.domain.use_case.log_use_cases.LogUseCases
import com.chouten.app.domain.use_case.module_use_cases.ModuleUseCases
import com.lagradost.nicehttp.Requests
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject

@Serializable
@Parcelize
data class SearchResult(
    val url: String,
    val poster: String,
    val title: String,
    val indicator: String? = "",
    @SerialName("current") val currentCount: Int? = null,
    @SerialName("total") val totalCount: Int? = null,
) : Parcelable

@HiltViewModel
class SearchViewModel @Inject constructor(
    val application: Application,
    val moduleUseCases: ModuleUseCases,
    val engine: ModuleEngine,
    private val logUseCases: LogUseCases,
    val savedStateHandle: SavedStateHandle
) : ViewModel() {

    /**
     * Job used for launching the debounced search when the query changes
     */
    private var searchJob: Job? = null

    lateinit var code: String

    /**
     * Used for determining if we should re-search when the module changes
     */
    var lastUsedModule: String = ""
        private set

    /**
     * Use a flow so that we can debounce the search query.
     * Also means we don't need to rely on Compose for non-ui related state management
     */
    val _searchQuery: MutableStateFlow<String> = MutableStateFlow("")

    @OptIn(FlowPreview::class)
    var searchQuery: String = ""
        set(value) {
            field = value
            viewModelScope.launch {
                _searchQuery.emit(value)
                searchJob?.cancel()
                searchJob = launch {
                    _searchQuery.debounce(500).distinctUntilChanged().collectLatest {
                        savedStateHandle["searchQuery"] = it
                        if (it.isBlank()) {
                            searchResults.emit(Resource.Uninitialized())
                        } else {
                            _searchQuery.emit(it)
                            search()
                        }
                    }
                }
            }
        }

    private val searchFlow = MutableStateFlow("[]")
    val searchResults: MutableStateFlow<Resource<List<SearchResult>>> =
        MutableStateFlow(Resource.Uninitialized())

    init {
        viewModelScope.launch {
            reloadCode()
        }
        engine.scope = viewModelScope;
        engine.registerObservable("search", searchFlow)
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
            searchFlow.collectLatest {
                try {
                    searchResults.emit(Resource.Success(Json.decodeFromString<List<SearchResult>>(it)))
                } catch (e: Exception) {
                    searchResults.emit(
                        Resource.Error(
                            UiText.StringRes(R.string.search_error).string(application)
                        )
                    )
                }
            }
        }
    }

    private fun getSearchResult(query: String, page: Int? = 0) {
        engine.evaluateJavascript(
            """
                // Not doing this makes the defaultSource not load ?? Maybe something to do with giving
                // the WebView time to parse the JS?
                if (defaultSource == undefined || typeof defaultSource["search"] != "function") {
                    console.log("Could not load Search Function on `defaultSource`!");
                } else {
                    defaultSource.search('${query}', ${page}).then(res => {
                        console.log("We got our result. Sending payload")
                        if (Native["sendResult"] == null || Native["sendResult"] == undefined) {
                            console.log("sendResult not found");
                        }
                        Native.sendResult(JSON.stringify({key: "search", value: JSON.stringify(res.results)}));
                    });
                }
        """.trimIndent()
        ) {}
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
                logUseCases.insertLog(LogEntry(entryContent = "Failed to find search code for ${module.name}"))
                return@run ""
            }
            withContext(Dispatchers.Main) {
                engine.load(
                    code
                )
            }
        }
    }

    /**
     * Uses the currently active module to search for the query
     */
    suspend fun search() {
        if (searchQuery.isBlank()) {
            searchResults.emit(Resource.Uninitialized())
            return
        }

        savedStateHandle["lastSearchQuery"] =
            savedStateHandle.getStateFlow("searchQuery", "").firstOrNull()
        searchResults.emit(Resource.Loading(null))

        if (lastUsedModule != application.moduleDatastore.data.firstOrNull()?.selectedModuleId) {
            reloadCode()
        }

        getSearchResult(_searchQuery.firstOrNull() ?: "")
        lastUsedModule = application.moduleDatastore.data.firstOrNull()?.selectedModuleId ?: ""
    }
}