package com.chouten.app.presentation.ui.screens.more.screens.log_screen

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.lifecycle.ViewModel
import com.chouten.app.domain.model.LogEntry
import com.chouten.app.domain.proto.filepathDatastore
import com.chouten.app.domain.use_case.log_use_cases.LogUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import java.sql.Timestamp
import javax.inject.Inject

@HiltViewModel
class LogViewModel @Inject constructor(
    private val logUseCases: LogUseCases
) : ViewModel() {
    val logs = logUseCases.getLogs()
    val _expanded: MutableStateFlow<List<Int>> = MutableStateFlow(emptyList())
    val expanded: StateFlow<List<Int>> = _expanded

    suspend fun getLogById(id: Int) = logUseCases.getLogById(id)
    suspend fun getLogInRange(from: Timestamp, to: Timestamp) = logUseCases.getLogInRange(from, to)
    suspend fun insertLog(logEntry: LogEntry) = logUseCases.insertLog(logEntry)
    suspend fun deleteLogById(id: Int) {
        logUseCases.deleteLogById(id)
        _expanded.emit(_expanded.value.filter { it != id })
    }

    suspend fun exportLogs(context: Context): Uri? {
        val dataDir = context.filepathDatastore.data.first()
        val rootDir = DocumentsContract.buildChildDocumentsUriUsingTree(
            dataDir.CHOUTEN_ROOT_DIR, DocumentsContract.getTreeDocumentId(dataDir.CHOUTEN_ROOT_DIR)
        ) ?: return null

        val logDir = context.contentResolver.query(
            rootDir, arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME
            ), null, null, null
        )?.use { cursor ->
            val displayNameIndex =
                cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val documentIdIndex =
                cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)

            while (cursor.moveToNext()) {
                if (cursor.getString(displayNameIndex) == "Logs") {
                    return@use DocumentsContract.buildChildDocumentsUriUsingTree(
                        rootDir, cursor.getString(documentIdIndex)
                    )
                }
            }

            // Create the Directory since it doesn't exist
            DocumentsContract.createDocument(
                context.contentResolver, rootDir, DocumentsContract.Document.MIME_TYPE_DIR, "Logs"
            )
        } ?: return null
        try {
            val file = DocumentsContract.createDocument(
                context.contentResolver,
                logDir,
                "application/json",
                "log_${System.currentTimeMillis()}.json"
            ) ?: throw IOException("Failed to create Log File")

            context.contentResolver.openOutputStream(file)?.use {
                it.bufferedWriter().use { bw ->
                    bw.write(Json.encodeToString(logs.first()))
                }
            }

            return file
        } catch (e: Exception) {
            e.printStackTrace()
            logUseCases.insertLog(
                LogEntry(
                    entryHeader = "Error exporting logs",
                    entryContent = e.message ?: "Unknown error",
                )
            )
            return null
        }
    }

    suspend fun deleteAllLogs() {
        logUseCases.deleteAllLogs()
        _expanded.emit(emptyList())
    }

    suspend fun toggleExpanded(id: Int) {
        if (_expanded.value.contains(id)) {
            _expanded.emit(_expanded.value.filter { it != id })
        } else {
            _expanded.emit(_expanded.value + id)
        }
    }
}