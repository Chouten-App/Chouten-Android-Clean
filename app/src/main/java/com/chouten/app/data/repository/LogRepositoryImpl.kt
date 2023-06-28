package com.chouten.app.data.repository

import com.chouten.app.data.data_source.log.LogDao
import com.chouten.app.domain.model.LogEntry
import com.chouten.app.domain.repository.LogRepository
import kotlinx.coroutines.flow.Flow
import java.sql.Date

class LogRepositoryImpl(
    private val dao: LogDao
) : LogRepository {
    override fun getLogs(): Flow<List<LogEntry>> {
        return dao.getLogs()
    }

    override suspend fun getLogWithinRange(from: Date, to: Date): List<LogEntry> {
        return dao.getLogWithinRange(from, to)
    }

    override suspend fun getLogById(id: Int): LogEntry? {
        return dao.getLogById(id)
    }

    override suspend fun insertLogEntry(logEntry: LogEntry) {
        dao.insertLogEntry(logEntry)
    }

    override suspend fun deleteLogEntry(id: Int) {
        dao.deleteLogEntry(id)
    }

    override suspend fun deleteAllLogEntries() {
        dao.deleteAllLogEntries()
    }
}