package com.chouten.app.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp

/**
 * HistoryEntry is a data class that represents a single entry in the history table.
 *
 * @param moduleId: String - The ID of the module used for viewing the media
 * @param parentUrl: String - The URL of the Info page
 * @param mediaIndex: Int - The index of the media
 * @param entryTitle: String - The title of the Entry
 * @param entryImage: String - The image for the Entry
 * @param entryDiscriminator: String - The discriminator for the Entry (e.g Episode, Chapter)
 * @param entryProgress: Double - How far the user has read/watched the Entry (% elapsed)
 * @param entryDuration: Long - The total duration of the Entry (the multiplier for entryProgress)
 * @param entryLastUpdated: Timestamp - The last time the Entry was updated
 */
@Entity(
    primaryKeys = ["moduleId", "parentUrl", "mediaIndex"]
)
data class HistoryEntry(
    val moduleId: String,
    val parentUrl: String,
    val mediaIndex: Int,
    val entryTitle: String,
    val entryImage: String,
    val entryDiscriminator: String,
    val entryProgress: Double,
    val entryDuration: Long,
    val entryLastUpdated: Timestamp
)