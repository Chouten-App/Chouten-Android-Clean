package com.chouten.app.domain.repository

import android.content.Context
import android.net.Uri
import android.webkit.WebMessage
import android.webkit.WebView
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

interface WebviewHandler<Action : Enum<Action>, ResultPayload : WebviewHandler.Companion.ActionPayload<Action>> {
    /**
     * The version that the webview handler
     * is targeting.
     */
    val formatVersion: Int

    /**
     * The callback that the app
     * uses to handle data from the webview handler
     */
    val callback: (ResultPayload) -> Unit

    /**
     * Initializes the webview handler
     * @param context The context
     * @param callback The callback that the app uses to handle data from the webview handler
     */
    suspend fun initialize(context: Context, callback: (ResultPayload) -> Unit)

    /**
     * Loads a webview payload into the handler.
     * @param code The JavaScript code to load into the webview handler
     * @param payload The payload to pass to the webview handler
     */
    suspend fun load(
        code: String, payload: WebviewPayload<Action>
    )

    /**
     * Passes a payload to the webview handler
     * @param payload The payload to pass to the webview handler
     * @throws IllegalArgumentException if the payload is invalid
     */
    suspend fun submitPayload(payload: RequestPayload<Action>)

    /**
     * Destroys the webview handler
     * @throws IllegalStateException if the webview handler is not initialized
     */
    suspend fun destroy()

    /**
     * Returns the common code for the webview handler.
     * The code is dependent on the [formatVersion]
     * @param context The application context
     * @return String - The common code for the webview handler
     */
    suspend fun getCommonCode(context: Context): String

    companion object {
        @Serializable
        data class RequestPayload<Action : Enum<Action>>(
            @SerialName("reqId") val requestId: String,
            val action: Action,
            val url: String,
            @SerialName("shouldExit") val persist: Boolean = false,
            val headers: Map<String, String> = emptyMap(),
            val result: String? = null,
            val method: String = "GET",
            val body: String? = null
        )

        @Serializable
        data class ResponsePayload(
            @SerialName("reqId") val requestId: String, val responseText: String
        )

        @Serializable
        data class BasePayload<Action : Enum<Action>>(
            @SerialName("reqId") val requestId: String,
            val action: Action,
            val payload: WebviewPayload<Action>
        )

        @Serializable
        data class WebviewPayload<Action : Enum<Action>>(
            val query: String, val action: Action,
        )

        interface ActionPayload<Action : Enum<Action>> {
            val action: Action
        }
    }
    }


@OptIn(InternalSerializationApi::class)
inline fun <reified Action : Enum<Action>> WebView.postWebMessage(
    webMessage: WebviewHandler.Companion.BasePayload<Action>,
    targetOrigin: Uri = Uri.parse("*"),
    serializer: KSerializer<Action> = Action::class.serializer()
) {
    postWebMessage(
        WebMessage(
            Json.encodeToString(
                WebviewHandler.Companion.BasePayload.serializer(
                    serializer
                ), webMessage
            )
        ), targetOrigin
    )
}

fun WebView.postWebMessage(
    webMessage: WebviewHandler.Companion.ResponsePayload,
    targetOrigin: Uri = Uri.parse("*"),
) {
    postWebMessage(
        WebMessage(
            Json.encodeToString(
                WebviewHandler.Companion.ResponsePayload.serializer(), webMessage
            )
        ), targetOrigin
    )
}