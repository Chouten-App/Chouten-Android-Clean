package com.chouten.app.data.data_source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.chouten.app.domain.model.HistoryEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM HistoryEntry")
    fun getHistory(): Flow<List<HistoryEntry>>

    /**
     * Get a single [HistoryEntry] by its URL
     * @param url: String - The URL of the Entry
     */
    @Query("SELECT * FROM HistoryEntry WHERE entryUrl = :url")
    suspend fun getHistoryByUrl(url: String): HistoryEntry?

    /**
     * Insert a new [HistoryEntry] into the database
     * - Do not use this method to update an existing HistoryEntry.
     * - See [updateHistoryEntry] for updating an existing HistoryEntry
     * @param historyEntry: HistoryEntry - The HistoryEntry to insert
     * @throws android.database.sqlite.SQLiteConstraintException If the HistoryEntry already exists
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertHistoryEntry(historyEntry: HistoryEntry)

    /**
     * Update an existing [HistoryEntry] in the database
     * @param historyEntry: HistoryEntry - The HistoryEntry to update
     */
    @Update
    suspend fun updateHistoryEntry(historyEntry: HistoryEntry)

    /**
     * Delete a [HistoryEntry] from the database
     * @param historyEntry: HistoryEntry - The HistoryEntry to delete
     */
    @Delete
    suspend fun deleteHistoryEntry(historyEntry: HistoryEntry)
}