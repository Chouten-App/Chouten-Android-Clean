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
     * Returns a list of [HistoryEntry] by parent url.
     * @param url The parent url of the [HistoryEntry].
     * @return [List<HistoryEntry>?] by url.
     */
    suspend fun getHistoryByUrl(url: String): List<HistoryEntry>?

    /**
     * Returns a single [HistoryEntry] or null using the composite primary key of the entry
     * @param id: String - The Module ID used for the entry
     * @param url: String - The parent url of the entry (info page url)
     * @param index: Int - The (0-based) media index of the entry
     * @return A single [HistoryEntry] or null
     */
    suspend fun getHistoryByPKey(id: String, url: String, index: Int): HistoryEntry?

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

    /**
     * Deletes **all** [HistoryEntry]s from the database.
     * **Use with caution**
     */
    suspend fun deleteAllHistoryEntries()
}