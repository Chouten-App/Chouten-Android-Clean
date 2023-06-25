package com.chouten.app.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date

/**
 * HistoryEntry is a data class that represents a single entry in the history table.
 *
 * @param entryUrl: String - The Unique URL for the Entry
 * @param entryTitle: String - The title of the Entry
 * @param entryImage: String - The image for the Entry
 * @param entryDiscriminator: String - The discriminator for the Entry (e.g Episode, Chapter)
 * @param entryProgress: Timestamp - How far the user has read/watched the Entry
 * @param entryDuration: Timestamp - The total duration of the Entry
 * @param entryLastUpdated: Timestamp - The last time the Entry was updated
 */
@Entity
data class HistoryEntry(
    @PrimaryKey val entryUrl: String,
    val entryTitle: String,
    val entryImage: String,
    val entryDiscriminator: String,
    val entryProgress: Date,
    val entryDuration: Date,
    val entryLastUpdated: Date
)