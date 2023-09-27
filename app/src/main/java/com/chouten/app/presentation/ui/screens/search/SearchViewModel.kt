package com.chouten.app.presentation.ui.screens.search

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
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
@Parcelize
data class SearchResult(
    val url: String,
    val img: String,
    val title: String,
    val indicatorText: String?,
    val currentCount: Int?,
    val totalCount: Int?,
) : Parcelable

@HiltViewModel
class SearchViewModel @Inject constructor(
    val application: Application,
    val moduleUseCases: ModuleUseCases,
    val webviewHandler: WebviewHandler<Payloads_V2.Action_V2, Payloads_V2.GenericPayload<List<SearchResult>>>,
    private val logUseCases: LogUseCases,
    val savedStateHandle: SavedStateHandle
) : ViewModel() {

    /**
     * Job used for launching the debounced search when the query changes
     */
    private var searchJob: Job? = null

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
                            savedStateHandle["searchResults"] = Resource.Uninitialized("")
                        } else {
                            _searchQuery.emit(it)
                            search()
                        }
                    }
                }
            }
        }

    val searchResults: StateFlow<Resource<List<SearchResult>>> =
        savedStateHandle.getStateFlow("searchResults", Resource.Uninitialized())

    init {
        viewModelScope.launch {
            webviewHandler.logFn = { message ->
                viewModelScope.launch {
                    logUseCases.insertLog(
                        LogEntry(
                            entryHeader = "Webview Log", entryContent = message
                        )
                    )
                }
            }

            webviewHandler.initialize(application) { res ->
                if (res.action == Payloads_V2.Action_V2.ERROR) {
                    viewModelScope.launch {
                        savedStateHandle["searchResults"] = Resource.Error(
                            message = "Failed to search for $searchQuery", data = null
                        )
                    }
                    return@initialize
                }
                viewModelScope.launch {
                    savedStateHandle["searchResults"] = Resource.Success(res.result.result)
                }
            }
        }
    }

    /**
     * Uses the currently active module to search for the query
     */
    suspend fun search() {
        if (searchQuery.isBlank()) {
            savedStateHandle["searchResults"] = Resource.Uninitialized("")
            return
        }

        savedStateHandle["lastSearchQuery"] =
            savedStateHandle.getStateFlow("searchQuery", "").firstOrNull()
        savedStateHandle["searchResults"] = Resource.Loading(null)

        val moduleId = application.moduleDatastore.data.firstOrNull()?.selectedModuleId ?: return
        val module = moduleUseCases.getModuleUris().find {
            it.id == moduleId
        } ?: return
        val code = module.code?.search?.getOrNull(0)?.code ?: ""

        webviewHandler.load(
            code, WebviewHandler.Companion.WebviewPayload(
                query = searchQuery, action = Payloads_V2.Action_V2.SEARCH
            )
        )
        lastUsedModule = module.id
    }
}