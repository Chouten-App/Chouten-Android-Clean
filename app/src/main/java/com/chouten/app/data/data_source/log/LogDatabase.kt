package com.chouten.app.data.data_source.log

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.chouten.app.domain.model.LogEntry

@Database(
    entities = [LogEntry::class],
    version = 1,
)
@TypeConverters(com.chouten.app.common.TypeConverters::class)
abstract class LogDatabase : RoomDatabase() {
    abstract val logDao: LogDao

    companion object {
        const val DATABASE_NAME = "log_db"
    }
}