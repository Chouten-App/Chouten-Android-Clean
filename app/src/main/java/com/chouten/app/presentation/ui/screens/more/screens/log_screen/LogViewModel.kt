package com.chouten.app.presentation.ui.screens.more.screens.log_screen

import androidx.lifecycle.ViewModel
import com.chouten.app.domain.model.LogEntry
import com.chouten.app.domain.use_case.log_use_cases.LogUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import java.sql.Date
import javax.inject.Inject

@HiltViewModel
class LogViewModel @Inject constructor(
    private val logUseCases: LogUseCases
) : ViewModel() {
    val logs = logUseCases.getLogs()
    suspend fun getLogById(id: Int) = logUseCases.getLogById(id)
    suspend fun getLogInRange(from: Date, to: Date) = logUseCases.getLogInRange(from, to)
    suspend fun insertLog(logEntry: LogEntry) = logUseCases.insertLog(logEntry)
    suspend fun deleteLogById(id: Int) = logUseCases.deleteLogById(id)
    suspend fun deleteAllLogs() = logUseCases.deleteAllLogs()
}