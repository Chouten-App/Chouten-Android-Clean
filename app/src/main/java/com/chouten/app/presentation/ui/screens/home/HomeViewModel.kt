package com.chouten.app.presentation.ui.screens.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chouten.app.domain.model.Payloads_V2.Action_V2
import com.chouten.app.domain.model.Payloads_V2.GenericPayload
import com.chouten.app.domain.proto.moduleDatastore
import com.chouten.app.domain.repository.WebviewHandler
import com.chouten.app.domain.use_case.module_use_cases.ModuleUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
data class SearchResult(
    val url: String,
    val img: String,
    val title: String,
    val indicatorText: String?,
    val currentCount: Int?,
    val totalCount: Int?,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    val application: Application,
    val moduleUseCases: ModuleUseCases,
    val webviewHandler: WebviewHandler<Action_V2, GenericPayload<List<SearchResult>>>
) : ViewModel() {
    init {
        viewModelScope.launch {
            // Get activity context
            webviewHandler.initialize(application) { res ->
                if (res.action != Action_V2.RESULT) return@initialize
                Log.d("WEBVIEW", "RESULT: ${res.result.result}")
            }
            moduleUseCases.getModuleUris().find {
                it.id == application.baseContext.moduleDatastore.data.first().selectedModuleId
            }?.let { model ->
                val code: String = model.code?.search?.getOrNull(0)?.code ?: ""
                webviewHandler.load(
                    code, WebviewHandler.Companion.WebviewPayload(
                        query = "Operation Barbershop", action = Action_V2.SEARCH
                    )
                )
            }
        }
    }
}