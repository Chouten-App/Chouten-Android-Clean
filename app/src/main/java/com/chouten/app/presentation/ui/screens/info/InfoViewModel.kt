package com.chouten.app.presentation.ui.screens.info

import kotlinx.serialization.Serializable

@Serializable
data class InfoResult(
    val id: String?,
    val titles: Titles,
    val epListURLs: List<String>,
    val altTitles: List<String>?,
    val description: String?,
    val poster: String,
    val banner: String?,
    val status: String?,
    val totalMediaCount: Int?,
    val mediaType: String,
    val seasons: List<Season>?,
    val mediaList: List<MediaListItem>?,
) {
    @Serializable
    data class MediaListItem(
        val title: String, val list: List<MediaItem>
    )

    @Serializable
    data class Titles(
        val primary: String, val secondary: String?
    )

    @Serializable
    data class MediaItem(
        val url: String,
        val number: Float?,
        val title: String?,
        val description: String?,
        val image: String?,
    ) {
        override fun toString(): String {
            return Json.encodeToString(serializer(), this);
        }
    }

    @Serializable
    data class Season(
        val name: String,
        val url: String,
    )
}
