package com.chouten.app.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.chouten.app.BuildConfig
import com.chouten.app.R
import com.chouten.app.domain.repository.ModuleEngine
import com.lagradost.nicehttp.Requests
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

@SuppressLint("SetJavaScriptEnabled")
class ModuleEngineImpl @Inject constructor(val context: Context, val client: Requests) :
    ModuleEngine {

    private lateinit var webView: WebView
    private val commonCode: String =
        context.resources.openRawResource(R.raw.commoncode_v3).bufferedReader().readLines()
            .joinToString("\n")

    override lateinit var scope: CoroutineScope
    override var observables: Map<String, MutableStateFlow<String>> = mapOf()
    override val interceptors: MutableMap<String, (Any) -> Unit> = mutableMapOf()

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    init {
        if (interceptors.containsKey("logger")) {
            interceptors["logger"]!!("Initialising Module Engine")
        }
        if (!::webView.isInitialized) {
            webView = WebView(context)
            webView.settings.javaScriptEnabled = true
            webView.settings.domStorageEnabled = true
            webView.addJavascriptInterface(this, "Native")
            WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
        }
    }

    override fun evaluateJavascript(javascript: String, callback: (String) -> Unit): Boolean? {
        if (interceptors.containsKey("logger")) {
            interceptors["logger"]!!("Evaluating JavaScript: $javascript")
        }
        webView.evaluateJavascript(javascript, callback)
        return null
    }

    override fun load(javascript: String): Boolean? {
        if (interceptors.containsKey("logger")) {
            interceptors["logger"]!!("Loading JavaScript: $javascript")
        }
        webView.loadDataWithBaseURL(
            null, "<script>$javascript;$commonCode;</script>", "text/html;charset=utf-8", "br", null
        )
        return null
    }

    @JavascriptInterface
    fun sendResult(result: String) {
        Log.d("SendResult", "Payload received. $result")
        val decoded = Json.decodeFromString<WebviewBundle<String>>(result)
        observables[decoded.key]?.let { v ->
            scope.launch {
                Log.d("SendResult", "we are supposed to be emitting it..")
                v.emit(decoded.value)
            }
        }
    }

    @JavascriptInterface
    fun request(
        url: String, method: String, headers: Map<String, String>? = null, body: String? = null
    ): String {
        if (interceptors.containsKey("logging")) {
            interceptors["logging"]!!("$method on $url with $headers and $body")
        }
        return when (method.uppercase()) {
            "GET" -> {
                runBlocking {
                    client.get(url, headers ?: mapOf(), body).let {
                        json.encodeToString(
                            Response(
                                statusCode = it.code,
                                body = it.body.string(),
                                contentType = it.headers["Content-Type"].toString()
                            )
                        )
                    }
                }
            }

            "POST" -> {
                runBlocking {
                    client.post(
                        url = url, headers = headers ?: mapOf(), requestBody = body?.toRequestBody()
                    ).let {
                        json.encodeToString(
                            Response(
                                it.code, it.body.string(), it.headers["Content-Type"].toString()
                            )
                        )
                    }
                }


            }

            "PUT" -> {
                runBlocking {
                    client.put(
                        url = url, headers = headers ?: mapOf(), requestBody = body?.toRequestBody()
                    ).let {
                        json.encodeToString(
                            Response(
                                it.code, it.body.string(), it.headers["Content-Type"].toString()
                            )
                        )
                    }
                }
            }

            "DELETE" -> {
                runBlocking {
                    client.delete(
                        url = url, headers = headers ?: mapOf(), requestBody = body?.toRequestBody()
                    ).let {
                        json.encodeToString(
                            Response(
                                it.code, it.body.string(), it.headers["Content-Type"].toString()
                            )
                        )
                    }
                }
            }

            else -> throw IllegalArgumentException("Method \"$method\" not supported.")
        }
    }

    /**
     * Used by the JS code to log a message
     */
    @JavascriptInterface
    fun log(message: String) {
        if (interceptors.containsKey("logger")) {
            interceptors["logger"]!!(message)
        } else {
            Log.d("ModuleEngine", message)
        }
    }

    @Serializable
    data class Response(val statusCode: Int, val body: String, val contentType: String)

    @Serializable
    data class WebviewBundle<T>(val key: String, val value: T)
}