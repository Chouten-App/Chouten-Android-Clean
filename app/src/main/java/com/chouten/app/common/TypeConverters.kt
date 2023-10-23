package com.chouten.app.common

import androidx.room.TypeConverter
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
}