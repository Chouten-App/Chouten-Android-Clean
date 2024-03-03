package com.chouten.app.data.data_source.module

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.chouten.app.domain.model.ModuleModel

@Database(
    entities = [ModuleModel::class],
    version = 1,
)
@TypeConverters(com.chouten.app.common.TypeConverters::class)
abstract class ModuleDatabase : RoomDatabase() {
    abstract val moduleDao: ModuleDao

    companion object {
        const val DATABASE_NAME = "module_db"
    }
}