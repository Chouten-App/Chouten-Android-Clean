package com.chouten.app.data.repository

import android.annotation.SuppressLint
import android.content.Context
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
    private val resultConverter: (Action_V2, String) -> BaseResultPayload
) : WebviewHandler<Action_V2, BaseResultPayload> {


    override val formatVersion: Int = 2

    private lateinit var webview: WebView

    private lateinit var commonCode: String

    override lateinit var callback: (BaseResultPayload) -> Unit

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Initializes the webview handler
     * @param context The context
     */
    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    override suspend fun initialize(
        context: Context, callback: (BaseResultPayload) -> Unit
    ) {
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
     * @return String - The common code for the webview handler
     */
    override suspend fun getCommonCode(context: Context): String {
        val resId = R.raw.commoncode_v2
        context.resources.openRawResource(resId).bufferedReader().use {
            return it.readText()
        }
    }

    /**
     * Passes a payload to the webview handler
     * @param payload The payload to pass to the webview handler
     * @param callback The callback that the app uses to handle data from the webview handler
     * @throws IllegalArgumentException if the payload is invalid
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
     * Loads a webview payload into the handler.
     * @param code The JavaScript code to load into the webview handler
     * @param payload The payload to pass to the webview handler
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
            override fun onPageFinished(view: WebView?, url: String?) {
                val response = WebviewHandler.Companion.BasePayload(
                    requestId = "-1", action = Action_V2.LOGIC, payload = payload
                )

                Log.d("WebviewHandler", "Sending payload to webview: $response")

                webview.postWebMessage(response)
            }
        }

        Log.d("WebviewHandler", "Loading webview with common code: $commonCode")

        webview.loadDataWithBaseURL(
            null, "<script>$commonCode$code</script>", "text/html; charset=utf-8", "br", null
        )

    }

    private suspend fun handleHttpRequest(
        payload: WebviewHandler.Companion.RequestPayload<Action_V2>
    ) {
        Log.d("WebviewHandler", "Handling HTTP request: $payload")
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

    @JavascriptInterface
    fun sendHTTPRequest(data: String) {
        runBlocking {
            Log.d("WebviewHandler", "Received HTTP request: $data")
            try {
                val payload =
                    json.decodeFromString<WebviewHandler.Companion.RequestPayload<Action_V2>>(
                        data
                    )
                Log.d("WebviewHandler", payload.toString())
                submitPayload(payload)
            } catch (e: Exception) {
                val error = resultConverter(
                    Action_V2.ERROR, e.message ?: "Unknown Error"
                )
                callback(error)
            }
        }
    }

    @JavascriptInterface
    fun sendResult(data: String) {
        runBlocking {
            Log.d("WebviewHandler", "Received result: $data")
            try {
                Log.d("WebviewHandler123", "Trying to parse $data")
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

    @JavascriptInterface
    fun log(message: String) {
        Log.d("WebviewHandler", message)
    }
