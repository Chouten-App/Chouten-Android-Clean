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

/**
 * WebviewHandler for handling interfacing modules with the native app via JS.
 * @param Action the enum containing all the actions that the webview handler can handle
 * @param ResultPayload the payload that the webview handler returns to the callback
 * @property formatVersion The version that the webview handler is targeting
 * @property callback The callback that the app uses to handle data from the webview handler
 * @see com.chouten.app.domain.model.Payloads_V2.Action_V2
 * @see com.chouten.app.domain.model.Payloads_V2.GenericPayload
 * @see com.chouten.app.data.repository.WebviewHandlerImpl
 */
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
     * @param payload The webview payload to pass to the webview handler
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
     * @sample com.chouten.app.data.repository.WebviewHandlerImpl.getCommonCode
     */
    suspend fun getCommonCode(context: Context): String

    companion object {

        /**
         * The payload used by the webview for requesting the app to
         * make a HTTP Request.
         * The app will return a [ResponsePayload] to the webview with the same [requestId]
         * @param Action the enum containing all the actions that the webview handler can handle
         * @property requestId The request ID
         * @property action The action
         * @property url The URL to make the request to
         * @property persist Whether the request should persist after the webview is destroyed
         * @property headers The headers to send with the request
         * @property result The result to return to the webview
         * @property method The HTTP method to use
         * @property body The body to send with the request
         */
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

        /**
         * The payload used by the app to return a response to the webview
         * @property requestId The request ID to link with the corresponding [RequestPayload]
         * @property responseText The response text
         * @see RequestPayload
         */
        @Serializable
        data class ResponsePayload(
            @SerialName("reqId") val requestId: String, val responseText: String
        )

        /**
         * The base payload used to invoke the webview handler
         * with a [WebviewPayload]
         * @param Action the enum containing all the actions that the webview handler can handle
         * @property requestId The request ID
         * @property action The action
         * @property payload The [WebviewPayload]
         */
        @Serializable
        data class BasePayload<Action : Enum<Action>>(
            @SerialName("reqId") val requestId: String,
            val action: Action,
            val payload: WebviewPayload<Action>
        )

        /**
         * The payload used by the app to send a payload to the webview
         * without requiring a [RequestPayload]
         * @param Action the enum containing all the actions that the webview handler can handle
         * @property query The query
         * @property action The action
         */
        @Serializable
        data class WebviewPayload<Action : Enum<Action>>(
            val query: String, val action: Action,
        )

        /**
         * The interface used by the app to build a specific ResultPayload
         * @param Action the enum containing all the actions that the webview handler can handle
         * @property action The action
         * @see com.chouten.app.domain.model.Payloads_V2.GenericPayload
         */
        interface ActionPayload<Action : Enum<Action>> {
            val action: Action
        }
    }
    }


/**
 * More safe way to post a web message to the webview
 * by using the [WebviewHandler.Companion.BasePayload] class
 * @param Action the enum containing all the actions that the webview handler can handle
 * @param webMessage The payload to post to the webview
 * @param targetOrigin The target origin
 * @param serializer The serializer to use for the [Action]
 * @see WebviewHandler.Companion.BasePayload
 */
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

/**
 * More safe way to post a web message to the webview
 * by using the [WebviewHandler.Companion.ResponsePayload] class
 * @param webMessage The payload to post to the webview
 * @param targetOrigin The target origin
 * @see WebviewHandler.Companion.ResponsePayload
 */
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