package com.chouten.app.common

import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.chouten.app.domain.model.ModuleModel
import com.chouten.app.domain.model.Version
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.sql.Timestamp

class TypeConverters {
    @TypeConverter
    fun fromTimestamp(value: Long): Timestamp {
        return Timestamp(value)
    }

    @TypeConverter
    fun dateToTimestamp(value: Timestamp): Long {
        return value.time
    }

    @TypeConverter
    fun listStringToString(value: List<String>): String = Json.encodeToString(value)

    @TypeConverter
    fun stringToListString(value: String): List<String> = Json.decodeFromString(value)

    @TypeConverter
    fun versionToString(value: Version): String = Json.encodeToString(value)

    @TypeConverter
    fun stringToVersion(value: String): Version = Json.decodeFromString(value)

    @TypeConverter
    fun metadataToString(value: ModuleModel.ModuleMetadata): String = Json.encodeToString(value)

    @TypeConverter
    fun stringToMetadata(value: String): ModuleModel.ModuleMetadata = Json.decodeFromString(value)

    @TypeConverter
    fun moduleCodeToString(value: ModuleModel.ModuleCode): String = Json.encodeToString(value)

    @TypeConverter
    fun stringToModuleCode(value: String): ModuleModel.ModuleCode = Json.decodeFromString(value)
}