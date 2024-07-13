package com.chouten.app.data.data_source.module

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.DeleteTable
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.chouten.app.domain.model.ModuleModel

@Database(
    entities = [ModuleModel::class],
    version = 2,
    exportSchema = true,
)
@TypeConverters(com.chouten.app.common.TypeConverters::class)
abstract class ModuleDatabase : RoomDatabase() {
    abstract val moduleDao: ModuleDao

    companion object {
        const val DATABASE_NAME = "module_db"
    }
}