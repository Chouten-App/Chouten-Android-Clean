package com.chouten.app.data.data_source

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.chouten.app.domain.model.HistoryEntry

@Database(
    entities = [HistoryEntry::class],
    version = 1,
)
@TypeConverters(com.chouten.app.common.TypeConverters::class)
abstract class HistoryDatabase : RoomDatabase() {
    abstract val historyDao: HistoryDao

    companion object {
        const val DATABASE_NAME = "history_db"
    }
}