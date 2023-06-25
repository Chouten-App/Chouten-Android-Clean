package com.chouten.app.presentation.ui.screens.history

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.lifecycle.ViewModel
import com.chouten.app.domain.model.HistoryEntry
import com.chouten.app.domain.use_case.history_use_cases.HistoryUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyUseCases: HistoryUseCases
) : ViewModel() {
    val history = historyUseCases.getHistory()

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

    suspend fun deleteHistoryEntry(url: String) {
        historyUseCases.deleteHistory(url)
    }

    suspend fun updateHistoryEntry(historyEntry: HistoryEntry) {
        historyUseCases.updateHistory(historyEntry)
    }
}