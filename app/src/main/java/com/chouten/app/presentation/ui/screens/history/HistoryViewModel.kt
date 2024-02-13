package com.chouten.app.presentation.ui.screens.history

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chouten.app.domain.model.HistoryEntry
import com.chouten.app.domain.model.LogEntry
import com.chouten.app.domain.model.ModuleModel
import com.chouten.app.domain.use_case.history_use_cases.HistoryUseCases
import com.chouten.app.domain.use_case.log_use_cases.LogUseCases
import com.chouten.app.domain.use_case.module_use_cases.ModuleUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyUseCases: HistoryUseCases,
    private val moduleUseCases: ModuleUseCases,
    private val logUseCases: LogUseCases
) : ViewModel() {
    val history: MutableStateFlow<Map<String, Pair<String, List<HistoryEntry>>>> =
        MutableStateFlow(mapOf())

    lateinit var modules: List<ModuleModel>

    init {
        viewModelScope.launch {
            modules = moduleUseCases.getModuleUris()
            historyUseCases.getHistory().firstOrNull()?.let {
                history.emit(mapOf())
                val historyMap = mutableMapOf<String, Pair<String, List<HistoryEntry>>>()
                it.filterNot { entry -> history.first().keys.contains(entry.moduleId) }
                    .forEach { entry ->
                        historyMap[entry.moduleId] =
                            Pair(modules.find { module -> module.id == entry.moduleId }?.name
                                ?: run {
                                    logUseCases.insertLog(LogEntry(
                                        entryHeader = "Ignored History",
                                        entryContent = "Ignored History Entry by Module ID ${entry.moduleId}"
                                    ))
                                    return@forEach
                                }, (historyMap[entry.moduleId]?.second ?: listOf()) + listOf(entry)
                            )
                    }
                history.emit(historyMap)
            }
        }
    }

    suspend fun insertHistoryEntry(historyEntry: HistoryEntry) {
        try {
            historyUseCases.insertHistory(historyEntry)
        } catch (e: SQLiteConstraintException) {
            Log.e("HistoryDB", "Attempted to insert duplicate entry: $historyEntry")
            e.printStackTrace()
        }
    }

    suspend fun deleteHistoryEntry(historyEntry: HistoryEntry) {
        historyUseCases.deleteHistory(historyEntry)
    }

    suspend fun deleteHistoryEntry(id: String, url: String, index: Int) {
        historyUseCases.deleteHistory(id, url, index)
    }

    suspend fun deleteAllHistoryEntries() {
        historyUseCases.deleteAllHistory()
    }

    suspend fun updateHistoryEntry(historyEntry: HistoryEntry) {
        historyUseCases.updateHistory(historyEntry)
    }
}