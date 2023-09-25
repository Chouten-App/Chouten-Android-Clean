package com.chouten.app.presentation.ui.screens.home

//@HiltViewModel
//class HomeViewModel @Inject constructor(
//    val application: Application,
//    val moduleUseCases: ModuleUseCases,
//    val webviewHandler: WebviewHandler<Action_V2, GenericPayload<List<SearchResult>>>,
//    private val logUseCases: LogUseCases
//) : ViewModel() {
//    init {
//        viewModelScope.launch {
//            // Get activity context
//            webviewHandler.logFn = { message ->
//                viewModelScope.launch {
//                    logUseCases.insertLog(
//                        LogEntry(
//                            entryHeader = "Webview Log", entryContent = message
//                        )
//                    )
//                }
//            }
//
//            webviewHandler.initialize(application) { res ->
//                if (res.action != Action_V2.RESULT) return@initialize
//                Log.d("WEBVIEW", "RESULT: ${res.result.result}")
//            }
//            moduleUseCases.getModuleUris().find {
//                it.id == application.baseContext.moduleDatastore.data.first().selectedModuleId
//            }?.let { model ->
//                val code: String = model.code?.search?.getOrNull(0)?.code ?: ""
//                webviewHandler.load(
//                    code, WebviewHandler.Companion.WebviewPayload(
//                        query = "Operation Barbershop", action = Action_V2.SEARCH
//                    )
//                )
//            }
//        }
//    }
//}