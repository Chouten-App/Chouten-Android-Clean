package com.chouten.app.domain.repository

import com.chouten.app.domain.model.LogEntry
import kotlinx.coroutines.flow.Flow
import java.sql.Timestamp

interface LogRepository {
    fun getLogs(): Flow<List<LogEntry>>

    /**
     * Get all [LogEntry]s within a given range
     * @param from: Timestamp - The inclusive start of the range
     * @param to: Timestamp - The inclusive end of the range
     */
    suspend fun getLogWithinRange(from: Timestamp, to: Timestamp): List<LogEntry>

    /**
     * Get a single [LogEntry] by its ID
     * @param id: Int - The ID of the Entry
     */
    suspend fun getLogById(id: Int): LogEntry?

    /**
     * Insert a new [LogEntry] into the database
     * @param logEntry: LogEntry - The LogEntry to insert
     * @throws android.database.sqlite.SQLiteConstraintException If the LogEntry already exists
     */
    @Throws(android.database.sqlite.SQLiteConstraintException::class)
    suspend fun insertLogEntry(logEntry: LogEntry)

    /**
     * Delete a [LogEntry] from the database
     * @param id: Int - The ID of the LogEntry to delete
     */
    suspend fun deleteLogEntry(id: Int)

    /**
     * #### Deletes **all** [LogEntry]s from the database
     * **Use with caution**
     */
    suspend fun deleteAllLogEntries()
}