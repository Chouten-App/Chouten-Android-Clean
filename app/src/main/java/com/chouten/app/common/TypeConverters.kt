package com.chouten.app.common

import androidx.room.TypeConverter
import java.sql.Date

class TypeConverters {
    @TypeConverter
    fun fromTimestamp(value: Long): Date {
        return Date(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: Date): Long {
        return date.time
    }
}