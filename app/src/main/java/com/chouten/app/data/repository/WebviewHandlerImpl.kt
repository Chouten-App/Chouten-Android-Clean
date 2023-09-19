package com.chouten.app.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import com.chouten.app.R
import com.chouten.app.domain.model.Payloads_V2.Action_V2
import com.chouten.app.domain.repository.WebviewHandler
import com.chouten.app.domain.repository.postWebMessage
import com.lagradost.nicehttp.Requests
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class WebviewHandlerImpl<BaseResultPayload : WebviewHandler.Companion.ActionPayload<Action_V2>> @Inject constructor(
    private val httpClient: Requests,
    /**
     * Converts an object such as { "action": "search", "result": "..." } to the correct payload
     */
    private val resultConverter: (Action_V2, String) -> BaseResultPayload
) : WebviewHandler<Action_V2, BaseResultPayload> {

    override val formatVersion: Int = 2

    override lateinit var callback: (BaseResultPayload) -> Unit

    override lateinit var logFn: (String) -> Unit

    private lateinit var webview: WebView
    private lateinit var commonCode: String


    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Initializes the webview handler
     * @param context The context
     * @param callback The callback that the app uses to handle data from the webview handler
     */
    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    override suspend fun initialize(context: Context, callback: (BaseResultPayload) -> Unit) {
        if (!::webview.isInitialized) {
            webview = WebView(context)
            webview.settings.javaScriptEnabled = true
            webview.settings.domStorageEnabled = true
            webview.addJavascriptInterface(this, "Native")
            WebView.setWebContentsDebuggingEnabled(true)
        }
        if (!::commonCode.isInitialized) {
            commonCode = getCommonCode(context)
        }
        if (!::callback.isInitialized) {
            this.callback = callback
        }
    }

    /**
     * Loads a webview payload into the handler.
     * @param code The JavaScript code to load into the webview handler
     * @param payload The webview payload to pass to the webview handler
     * @throws IllegalStateException if the webview handler is not initialized
     * @throws IllegalStateException if the common code is not initialized
     */
    override suspend fun load(
        code: String, payload: WebviewHandler.Companion.WebviewPayload<Action_V2>
    ) {
        if (!::webview.isInitialized) {
            throw IllegalStateException("Webview handler is not initialized")
        }
        if (!::commonCode.isInitialized) {
            throw IllegalStateException("Common code is not initialized")
        }

        webview.webViewClient = object : WebViewClient() {
            // We need to wait for the webview to finish loading before we can inject the payload
            override fun onPageFinished(view: WebView?, url: String?) {
                val response = WebviewHandler.Companion.BasePayload(
                    requestId = "-1", action = Action_V2.LOGIC, payload = payload
                )

                webview.postWebMessage(response)
            }
        }

        webview.loadDataWithBaseURL(
            null, "<script>$commonCode$code</script>", "text/html; charset=utf-8", "br", null
        )

    }

    /**
     * Passes a payload to the webview handler
     * @param payload The payload to pass to the webview handler
     * @throws IllegalArgumentException if the payload is invalid
     * @throws IllegalStateException if the webview handler is not initialized
     * @throws IllegalStateException if the common code is not initialized
     */
    override suspend fun submitPayload(payload: WebviewHandler.Companion.RequestPayload<Action_V2>) {
        if (!::webview.isInitialized) {
            throw IllegalStateException("Webview handler is not initialized")
        }
        if (!::commonCode.isInitialized) {
            throw IllegalStateException("Common code is not initialized")
        }
        if (!::callback.isInitialized) {
            throw IllegalStateException("Callback is not initialized")
        }

        when (payload.action) {
            Action_V2.HTTP_REQUEST -> handleHttpRequest(payload)
            Action_V2.RESULT -> callback(
                resultConverter(
                    payload.action, payload.result ?: "null"
                )
            )

            Action_V2.ERROR -> destroy()
            else -> throw IllegalArgumentException("Invalid action")
        }
    }

    /**
     * Destroys the webview handler
     * @throws IllegalStateException if the webview handler is not initialized
     */
    override suspend fun destroy() {
        if (::webview.isInitialized) {
            withContext(Dispatchers.Main) {
                webview.clearHistory()
                webview.clearCache(true)
                webview.destroy()
            }
        } else {
            throw IllegalStateException("Webview handler is not initialized")
        }
    }

    /**
     * Returns the common code for the webview handler.
     * The code is dependent on the [formatVersion]
     * @param context The application context
     * @throws Resources.NotFoundException if the common code is not found
     */
    override suspend fun getCommonCode(context: Context): String {
        val resId = R.raw.commoncode_v2
        context.resources.openRawResource(resId).bufferedReader().use {
            return it.readText()
        }
    }

    /**
     * Handles an HTTP request from the webview handler
     * @param payload The HTTP request payload
     * @throws IllegalArgumentException if the method is invalid
     */
    private suspend fun handleHttpRequest(
        payload: WebviewHandler.Companion.RequestPayload<Action_V2>
    ) {
        val responseText = when (payload.method) {
            "GET" -> httpClient.get(url = payload.url, headers = payload.headers)
            "POST" -> httpClient.post(
                url = payload.url,
                headers = payload.headers,
                requestBody = payload.body?.toRequestBody()
            )

            "PUT" -> httpClient.put(
                url = payload.url,
                headers = payload.headers,
                requestBody = payload.body?.toRequestBody()
            )

            "DELETE" -> httpClient.delete(
                url = payload.url,
                headers = payload.headers,
                requestBody = payload.body?.toRequestBody()
            )

            else -> throw IllegalArgumentException("Invalid method")
        }.body.string()

        val response = WebviewHandler.Companion.ResponsePayload(
            requestId = payload.requestId, responseText = responseText
        )

        withContext(Dispatchers.Main) {
            webview.postWebMessage(
                response
            )
        }
    }

    /**
     * Used by the JS code to make the native app perform a HTTP request
     */
    @JavascriptInterface
    fun sendHTTPRequest(data: String) {
        // We need to run this on the main thread because the webview is not thread safe
        runBlocking {
            try {
                val payload =
                    json.decodeFromString<WebviewHandler.Companion.RequestPayload<Action_V2>>(
                        data
                    )
                submitPayload(payload)
            } catch (e: Exception) {
                val error = resultConverter(
                    Action_V2.ERROR, e.message ?: "Unknown Error"
                )
                callback(error)
            }
        }
    }

    /**
     * Used by the JS code to send a result to the native app
     */
    @JavascriptInterface
    fun sendResult(data: String) {
        runBlocking {
            try {
                val parsed = resultConverter(
                    Action_V2.RESULT, data
                )
                callback(parsed)
            } catch (e: Exception) {
                e.printStackTrace()
                val error = resultConverter(
                    Action_V2.ERROR, e.message ?: "Unknown Error"
                )
                callback(error)
            }
        }
    }

    /**
     * Used by the JS code to log a message
     */
    @JavascriptInterface
    fun log(message: String) {
        if (::logFn.isInitialized) {
            logFn(message)
        } else {
            Log.d("WebviewHandler", message)
        }
    }
}
