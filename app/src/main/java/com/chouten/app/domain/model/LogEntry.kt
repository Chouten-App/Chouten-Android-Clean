package com.chouten.app.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LogEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val entryContent: String,
    val entryHeader: String = entryContent.take(15),
    val entryTimestamp: Long = System.currentTimeMillis()
)