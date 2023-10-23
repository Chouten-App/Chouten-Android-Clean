package com.chouten.app.data.data_source.log

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chouten.app.domain.model.LogEntry
import kotlinx.coroutines.flow.Flow
import java.sql.Timestamp

@Dao
interface LogDao {
    @Query("SELECT * FROM LogEntry")
    fun getLogs(): Flow<List<LogEntry>>

    /**
     * Get all [LogEntry]s within a given range
     * @param from: Timestamp - The inclusive start of the range
     * @param to: Timestamp - The inclusive end of the range
     */
    @Query("SELECT * FROM LogEntry WHERE entryTimestamp BETWEEN :from AND :to")
    suspend fun getLogWithinRange(from: Timestamp, to: Timestamp): List<LogEntry>

    /**
     * Get a single [LogEntry] by its ID
     * @param id: Int - The ID of the Entry
     */
    @Query("SELECT * FROM LogEntry WHERE id = :id")
    suspend fun getLogById(id: Int): LogEntry?

    /**
     * Insert a new [LogEntry] into the database
     * @param logEntry: LogEntry - The LogEntry to insert
     * @throws android.database.sqlite.SQLiteConstraintException If the LogEntry already exists
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertLogEntry(logEntry: LogEntry)

    /**
     * Delete a [LogEntry] from the database
     */
    @Query("DELETE FROM LogEntry WHERE id = :id")
    suspend fun deleteLogEntry(id: Int)

    /**
     * #### Deletes **all** [LogEntry]s from the database
     * **Use with caution**
     */
    @Query("DELETE FROM LogEntry")
    suspend fun deleteAllLogEntries()
}