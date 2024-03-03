package com.chouten.app.domain.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
@Entity
data class ModuleModel(
    /**
     * The id of the module.
     * This is used to identify the module in the app
     * and should be unique; however, this is not enforced
     * as the app does generate the id itself.
     */
    @PrimaryKey
    val id: String,

    /**
     * The type of module.
     * Expected values are "source" or "meta".
     */
    @SerialName("moduleType") val type: ModuleType,

    /**
     * The data type of the module.
     * Expected values are "Video", "Book", or "Text".
     */
    @SerialName("type") val dataType: ModuleDataType,

    /**
     * Subtypes of the module. These are used to identify
     * what the module can provide. There are no restrictions
     * on what these can be.
     * For example, the module subtype "anime" could be used to
     * identify a module that provides anime data.
     */
    val subtypes: List<String>,

    /**
     * The name of the module.
     * This is used to identify the module in the app.
     */
    val name: String,

    /**
     * The version of the module.
     * This is used to identify the module in the app.
     */
    val version: Version,

    /**
     * The format version for the code of the module.
     * This is used to determine if a module is compatible
     * with the current version of the app.
     */
    val formatVersion: Int,

    /**
     * The URL queried to check for updates to the module.
     */
    val updateUrl: String,

    /**
     * The metadata for the module
     * This is used to provide information about the module
     * such as the author and icon.
     * NOTE: The name of this within the JSON is "general".
     */
    @SerialName("general") val metadata: ModuleMetadata,

    /**
     * The source code for the module.
     */
    var code: ModuleCode? = null,
) {
    @Serializable
    data class ModuleMetadata(
        /**
         * The author of the module.
         */
        val author: String,

        /**
         * The icon for the module.
         * This is a bitmap encoded as a byte array.
         * Must be named using icon.(png|jpg|jpeg) in the root of the module directory.
         */
        var icon: ByteArray? = null,

        /**
         * The languages which the module supports.
         * Languages are identified by their ISO 639-1 code.
         * For example, "en" for English (not "en-US"/"en-GB").
         */
        val lang: List<String>,

        /**
         * The background colour used for the module.
         * This is a hex colour code, including the #.
         * For example, #FFFFFF for white.
         */
        @SerialName("bgColor") val backgroundColor: String,

        /**
         * The foreground colour used for the module.
         * This is a hex colour code, including the #.
         * For example, #000000 for black.
         */
        @SerialName("fgColor") val foregroundColor: String,
    )

    @Serializable
    data class ModuleCode(
        /**
         * The code for the home page of the module.
         */
        val home: List<ModuleCodeblock> = listOf(),

        /**
         * The code for the search page of the module.
         */
        val search: List<ModuleCodeblock> = listOf(),

        /**
         * The code for the info page of the module.
         */
        val info: List<ModuleCodeblock> = listOf(),

        /**
         *  The code for the media consume page of the module.
         */
        val mediaConsume: List<ModuleCodeblock> = listOf(),
    ) {
        @Serializable
        data class ModuleCodeblock(
            /**
             * The JavaScript imports for the codeblock.
             * These are imported from within the codeblock itself
             * via an event `loadScript()`
             */
            val imports: List<String>? = listOf(),

            /**
             * The code for the codeblock.
             */
            var code: String,
        )
    }

    @Serializable
    enum class ModuleType {
        /**
         * A module which provides data.
         */
        @SerialName("source")
        SOURCE,

        /**
         * A module which provides metadata for other modules (e.g search mappings).
         */
        @SerialName("meta")
        META,
    }

    @Serializable
    enum class ModuleDataType {
        /**
         * A module which provides video data.
         */
        @SerialName("Video")
        VIDEO,

        /**
         * A module which provides raw text data.
         */
        @SerialName("Text")
        TEXT,

        /**
         * A module which provides book data.
         */
        @SerialName("Book")
        BOOK,
    }

    /**
     * Converts the module to a JSON string.
     */
    override fun toString(): String = Json.encodeToString(serializer(), this)

    companion object {
        /**
         * The minimum format version supported by the app.
         * This is used to determine if a module is compatible
         * with the current version of the app.
         * The value is inclusive (e.g if 2, 2 is the minimum working version).
         */
        const val MIN_FORMAT_VERSION = 2

        /**
         * The maximum format version supported by the app.
         * This is used to determine if a module is compatible
         * with the current version of the app.
         * The value is inclusive (e.g if 3, 3 is the maximum working version).
         */
        const val MAX_FORMAT_VERSION = 2

        /**
         * Convert the ByteArray icon to a Bitmap.
         * @return The icon as a Bitmap.
         */
        fun ModuleMetadata.getIcon(): Bitmap? {
            // TODO: Use a default icon if the icon is null
            return icon?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
        }
    }
}