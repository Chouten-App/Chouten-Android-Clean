package com.chouten.app.presentation.ui.screens.history

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chouten.app.domain.model.HistoryEntry
import com.chouten.app.domain.use_case.history_use_cases.HistoryUseCases
import com.chouten.app.domain.use_case.module_use_cases.ModuleUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyUseCases: HistoryUseCases,
    private val moduleUseCases: ModuleUseCases
) : ViewModel() {
    val history: MutableStateFlow<Map<String, Pair<String, List<HistoryEntry>>>> = MutableStateFlow(mapOf())

    init {
        Log.d("HistoryViewModel", "We are in init")
        viewModelScope.launch {
            val modules = moduleUseCases.getModuleUris()
            Log.d("HistoryViewModel", "We are in the viewmodel scope")
            historyUseCases.getHistory().firstOrNull()?.let {
                Log.d("HistoryViewModel", "We are going through the entries")
                history.emit(mapOf())
                val historyMap = mutableMapOf<String,  Pair<String, List<HistoryEntry>>>()
                it.filter { entry -> !history.first().keys.contains(entry.moduleId) }.forEach { entry ->
                    Log.d("HistoryViewModel", "Adding onto ${entry.moduleId}")
                    historyMap[entry.moduleId] = Pair(modules.find { it.id == entry.moduleId }?.name ?: "N/A", (historyMap[entry.moduleId]?.second ?: listOf()) + listOf(entry))
                }
                history.emit(historyMap.toMap())
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

    suspend fun deleteHistoryEntry(id: String,  url: String, index: Int) {
        historyUseCases.deleteHistory(id, url, index)
    }

    suspend fun deleteAllHistoryEntries() {
        historyUseCases.deleteAllHistory()
    }

    suspend fun updateHistoryEntry(historyEntry: HistoryEntry) {
        historyUseCases.updateHistory(historyEntry)
    }
}