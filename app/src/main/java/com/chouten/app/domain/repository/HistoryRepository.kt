package com.chouten.app.domain.repository

import com.chouten.app.domain.model.HistoryEntry
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    /**
     * Returns a [Flow] of all [HistoryEntry]s.
     * @return [Flow] of all [HistoryEntry]s.
     */
    fun getHistory(): Flow<List<HistoryEntry>>

    /**
     * Returns a [HistoryEntry] by url.
     * @param url The url of the [HistoryEntry].
     * @return [HistoryEntry] by url.
     */
    suspend fun getHistoryByUrl(url: String): HistoryEntry?

    /**
     * Inserts a [HistoryEntry] into the database.
     * @param historyEntry The [HistoryEntry] to insert.
     * @throws android.database.sqlite.SQLiteConstraintException if the [HistoryEntry] already exists.
     */
    @Throws(android.database.sqlite.SQLiteConstraintException::class)
    suspend fun insertHistoryEntry(historyEntry: HistoryEntry)

    /**
     * Updates an existing [HistoryEntry] in the database.
     * @param historyEntry The [HistoryEntry] to update.
     */
    suspend fun updateHistoryEntry(historyEntry: HistoryEntry)

    /**
     * Deletes a [HistoryEntry] from the database.
     * @param historyEntry The [HistoryEntry] to delete.
     */
    suspend fun deleteHistoryEntry(historyEntry: HistoryEntry)
}