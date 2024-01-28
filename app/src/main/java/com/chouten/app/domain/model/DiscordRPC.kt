package com.chouten.app.domain.model

import android.util.Log
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * https://discord.com/developers/docs/topics/gateway
 */

@Serializable
enum class DiscordRPCActivityType {
    GAME, STREAMING, LISTENING, WATCHING, CUSTOM, COMPETING;

    override fun toString(): String {
        val superString = super.toString()
        return superString.firstOrNull()?.uppercase()
            ?.plus(superString.slice(1..superString.length)) ?: super.toString()
    }

    fun getType(): Int = ordinal
}

@Serializable
enum class DiscordRPCStatusType {
    ONLINE, DO_NOT_DISTURB, IDLE, INVISIBLE, OFFLINE;

    override fun toString(): String {
        return when (this) {
            ONLINE -> "online"
            DO_NOT_DISTURB -> "dnd"
            IDLE -> "idle"
            INVISIBLE -> "invisible"
            OFFLINE -> "offline"
        }
    }
}

@Serializable
data class DiscordRPCGActivityStructure(
    val name: String,
    val type: DiscordRPCActivityType,
    val url: String?,
    @SerialName("created_at") val createdAt: Int,
    val timestamps: DiscordRPCTimestampStructure?,
    @SerialName("application_id") val applicationId: Int?,
    val details: String?,
    val state: String?,
    val emoji: DiscordRPCEmojiStructure,
    /*
    TODO: Implement the other objects
    party?	party object	Information for the current party of the player
    assets?	assets object	Images for the presence and their hover texts
    secrets?	secrets object	Secrets for Rich Presence joining and spectating
    instance?	boolean	Whether or not the activity is an instanced game session
    flags?	integer	Activity flags ORd together, describes what the payload includes
    buttons?	array of buttons	Custom buttons shown in the Rich Presence (max 2)
     */
)

@Serializable
data class DiscordRPCEmojiStructure(
    val name: String, val id: Int?, val animated: Boolean?
)

@Serializable
data class DiscordRPCTimestampStructure(
    val start: Int?, val end: Int?
)

@Serializable
data class DiscordRPCGatewayIdentityStructure(
    val token: String,
    val properties: DiscordRPCGatewayIdentityConnectionProps,
    val compress: Boolean? = false,
    @SerialName("large_threshold") val largeThreshold: Int = 50,
    val shard: Array<String>?,
    val presence: DiscordRPCGatewayUpdatePresence?,
    val intents: Int
)

@Serializable
data class DiscordRPCGatewayUpdatePresence(
    val since: Int?,
    val activities: Array<DiscordRPCGActivityStructure>,
    val status: DiscordRPCStatusType,
    val afk: Boolean
)

@Serializable
data class DiscordRPCGatewayIdentityConnectionProps(
    val os: String = "linux", val browser: String = "unknown", val device: String = "unknown"
)

@Serializable
abstract class DiscordRPCGatewayEventData

@Serializable
data class DiscordRPCGatewayInitialHelloData(
    @SerialName("heartbeat_interval") val heartbeatInterval: Int,
    @SerialName("_trace") val trace: List<String>
) : DiscordRPCGatewayEventData()

@Serializable
class DiscordRPCGatewaySkeletonData : DiscordRPCGatewayEventData()

@Serializable
data class DiscordRPCGatewayEvent(
    @SerialName("op") val opCode: DiscordRPCGatewayOpCode,
    @SerialName("d") val eventData: DiscordRPCGatewayEventData,
    @SerialName("s") val sequenceNumber: Int?,
    @SerialName("t") val eventName: String?
) {
    @Serializable(with = DiscordRPCGatewayOpCodeSerializer::class)
    abstract class DiscordRPCGatewayOpCode(open val code: Int) {
        abstract val eventData: DiscordRPCGatewayEventData
    }
    //        DISPATCH(0), INITIAL_HELLO(10), HEARTBEAT_REQUEST(1), HEARTBEAT_ACK(11), IDENTIFY(2), ERROR(
//            -1
//        ),
//        PRESENCE_UPDATE(3);
//        class DISPATCH : DiscordRPCGatewayOpCode(0)
    data class INTIIAL_HELLO(override val eventData: DiscordRPCGatewayInitialHelloData) :
        DiscordRPCGatewayOpCode(10)
}

object DiscordRPCGatewayOpCodeSerializer : KSerializer<DiscordRPCGatewayEvent.DiscordRPCGatewayOpCode> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("DiscordRPCGatewayOpCode", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): DiscordRPCGatewayEvent.DiscordRPCGatewayOpCode {
        Log.d("DiscordRPC", "The decoder contains: ${decoder.decodeString()}")
        TODO("Not yet implemented")
    }

    override fun serialize(
        encoder: Encoder,
        value: DiscordRPCGatewayEvent.DiscordRPCGatewayOpCode
    ) {
        TODO("Not yet implemented")
    }

}