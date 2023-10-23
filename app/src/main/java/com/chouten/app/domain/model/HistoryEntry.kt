package com.chouten.app.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp

/**
 * HistoryEntry is a data class that represents a single entry in the history table.
 *
 * @param entryUrl: String - The Unique URL for the Entry
 * @param entryTitle: String - The title of the Entry
 * @param entryImage: String - The image for the Entry
 * @param entryDiscriminator: String - The discriminator for the Entry (e.g Episode, Chapter)
 * @param entryProgress: Long - How far the user has read/watched the Entry (% elapsed)
 * @param entryDuration: Long - The total duration of the Entry (the multiplier for entryProgress)
 * @param entryLastUpdated: Timestamp - The last time the Entry was updated
 */
@Entity
data class HistoryEntry(
    @PrimaryKey val entryUrl: String,
    val entryTitle: String,
    val entryImage: String,
    val entryDiscriminator: String,
    val entryProgress: Long,
    val entryDuration: Long,
    val entryLastUpdated: Timestamp
)