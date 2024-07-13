package com.chouten.app.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
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
    @PrimaryKey val id: String,

    /**
     * The type of module.
     * Expected values are 0 (ModuleType.SOURCE) or 1 (ModuleType.META).
     */
    @SerialName("type") val type: ModuleType,

    /**
     * Subtypes of the module. These are used to identify
     * what the module can provide. There are no restrictions
     * on what these can be.
     * For example, the module subtype "anime" could be used to
     * identify a module that provides anime data.
     */
    val subtypes: List<String>,

    /**
     * The author of the module.
     */
    val author: String,

    /**
     * The name of the module.
     * This is used to identify the module in the app.
     */
    val name: String,

    /**
     * The description of the module
     */
    val description: String,

    /**
     * The icon for the module.
     * This is a bitmap encoded as a byte array.
     * Must be named using icon.(png|jpg|jpeg) in the root of the module directory.
     */
    var icon: ByteArray? = null,

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
    val formatVersion: Int? = 3,

    /**
     * The URL queried to check for updates to the module.
     */
    val updateUrl: String? = null,

    /**
     * The source code for the module.
     */
    var code: String? = null,
) {
    @Serializable(with = ModuleTypeSerializer::class)
    enum class ModuleType {
        /**
         * A module which provides data.
         */
        SOURCE,

        /**
         * A module which provides metadata for other modules (e.g search mappings).
         */
        META,
    }

    class ModuleTypeSerializer : KSerializer<ModuleType> {
        override val descriptor: SerialDescriptor
            get() = PrimitiveSerialDescriptor("moduleType", PrimitiveKind.INT)

        override fun deserialize(decoder: Decoder): ModuleType {
            return decoder.decodeInt().let {
                when (it) {
                    0 -> ModuleType.SOURCE
                    1 -> ModuleType.META
                    else -> throw IllegalArgumentException("ModuleType Enum has no corresponding field with ordinal value $it")
                }
            }
        }

        override fun serialize(encoder: Encoder, value: ModuleType) {
            encoder.encodeInt(value.ordinal)
        }
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
        const val MIN_FORMAT_VERSION = 3

        /**
         * The maximum format version supported by the app.
         * This is used to determine if a module is compatible
         * with the current version of the app.
         * The value is inclusive (e.g if 3, 3 is the maximum working version).
         */
        const val MAX_FORMAT_VERSION = 3
    }
}