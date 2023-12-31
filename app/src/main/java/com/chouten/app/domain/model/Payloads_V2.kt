package com.chouten.app.domain.model

import com.chouten.app.domain.repository.WebviewHandler
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The payloads and actions for format version 2
 * @see WebviewHandler
 */
object Payloads_V2 {
    @Serializable
    enum class Action_V2 {
        @SerialName("HTTPRequest")
        HTTP_REQUEST,

        @SerialName("result")
        RESULT,

        @SerialName("error")
        ERROR,

        @SerialName("logic")
        LOGIC,

        @SerialName("homepage")
        GET_HOMEPAGE,

        @SerialName("search")
        SEARCH,

        @SerialName("info")
        GET_INFO,

        @SerialName("switchConfig")
        GET_SWITCH_CONFIG,

        @SerialName("metadata")
        GET_METADATA,

        @SerialName("eplist")
        GET_EPISODE_LIST,

        @SerialName("video")
        GET_VIDEO,

        @SerialName("server")
        GET_SERVER,
    }

    /**
     * Generic wrapper to implement the [WebviewHandler.Companion.ActionPayload] interface
     * for result payloads.
     * @param T the type of the result
     * @property action The action that the payload is for
     * @property result The result
     * @see WebviewHandler.Companion.ActionPayload
     */
    @Serializable
    data class GenericPayload<T>(
        override val action: Action_V2,
        val result: Result<T>,
    ) : WebviewHandler.Companion.ActionPayload<Action_V2> {
        @Serializable
        data class Result<T>(
            val action: Action_V2,
            val result: T,
        )
    }
}
