package com.chouten.app.presentation.ui.screens.watch

import android.app.Application
import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chouten.app.common.Resource
import com.chouten.app.domain.model.Payloads_V2
import com.chouten.app.domain.proto.moduleDatastore
import com.chouten.app.domain.repository.WebviewHandler
import com.chouten.app.domain.use_case.module_use_cases.ModuleUseCases
import com.chouten.app.presentation.ui.screens.info.InfoResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject
import kotlinx.serialization.Serializable
import java.lang.IllegalStateException

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

@HiltViewModel
class WatchViewModel @Inject constructor(
    val serverHandler: WebviewHandler<Payloads_V2.Action_V2, Payloads_V2.GenericPayload<List<WatchResult.ServerData>>>,
    val videoHandler: WebviewHandler<Payloads_V2.Action_V2, Payloads_V2.GenericPayload<WatchResult>>,
    val moduleUseCases: ModuleUseCases,
    val savedStateHandle: SavedStateHandle,
    val application: Application,
) : ViewModel() {

    private var url: String = ""

    val servers: StateFlow<Resource<List<WatchResult.ServerData>>> = savedStateHandle.getStateFlow(
        SERVER_RESULTS, Resource.Uninitialized()
    )

    val sources: StateFlow<Resource<WatchResult>> = savedStateHandle.getStateFlow(
        VIDEO_RESULTS, Resource.Uninitialized()
    )

    init {
        viewModelScope.launch {
            serverHandler.initialize(application) { res ->
                if (res.action == Payloads_V2.Action_V2.ERROR) {
                    viewModelScope.launch {
                        savedStateHandle[SERVER_RESULTS] = Resource.Error(
                            message = "Failed to get servers for $url", data = null
                        )
                    }
                    return@initialize
                }
                viewModelScope.launch {
                    savedStateHandle[SERVER_RESULTS] = Resource.Success(res.result.result)
                }
            }

            videoHandler.initialize(application) { res ->
                if (res.action == Payloads_V2.Action_V2.ERROR) {
                    viewModelScope.launch {
                        savedStateHandle[VIDEO_RESULTS] = Resource.Error(
                            message = "Failed to get sources for $url", data = null
                        )
                    }
                    return@initialize
                }
                viewModelScope.launch {
                    savedStateHandle[VIDEO_RESULTS] = Resource.Success(res.result.result)
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
        url = bundle.media.getOrElse(bundleId) {
            if (bundle.media.isEmpty()) {
                null
            } else
            throw IllegalArgumentException("Selected media index is out of bounds")
        }?.list?.getOrElse(0) {
            throw IllegalStateException("Media list is empty")
        }?.url ?: bundle.url

        Log.d("WatchViewModel", "URL is $url")
        serverHandler.load(
            getCode(),
            WebviewHandler.Companion.WebviewPayload(
                action = Payloads_V2.Action_V2.GET_SERVER,
                query = url
            )
        )
    }

    suspend fun getSource(url: String) {
        Log.d("WatchViewModel", "URL is $url")
        videoHandler.load(
            getCode(),
            WebviewHandler.Companion.WebviewPayload(
                action = Payloads_V2.Action_V2.GET_VIDEO,
                query = url
            )
        )
    }

    companion object {
        private const val SERVER_RESULTS = "serverResults"
        private const val VIDEO_RESULTS = "videoResults"
    }
}