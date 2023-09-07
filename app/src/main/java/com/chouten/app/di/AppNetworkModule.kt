package com.chouten.app.di

import android.app.Application
import android.os.Build
import com.lagradost.nicehttp.Requests
import com.lagradost.nicehttp.ResponseParser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlin.reflect.KClass


@Module
@InstallIn(SingletonComponent::class)
object AppNetworkModule {

    @Singleton
    @Provides
    fun provideHttpClient(application: Application): Requests {
        val context = application.applicationContext
        val cache = Cache(
            File(
                context.cacheDir, "http_cache"
            ), 1024L * 1024L * 5L // 5 MiB (not MB)
        )

        val okHttpClient = OkHttpClient.Builder().apply {
            followRedirects(true)
            followSslRedirects(true)
            cache(cache)
        }.build()

        val defaultHeaders = mapOf(
            "User-Agent" to "Mozilla/5.0 (Linux; Android %s; %s) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.127 Mobile Safari/537.36".format(
                Build.VERSION.RELEASE, Build.MODEL
            )
        )

        val mapper = object : ResponseParser {

            @OptIn(ExperimentalSerializationApi::class)
            val json = Json {
                isLenient = true
                ignoreUnknownKeys = true
                explicitNulls = false
            }

            @OptIn(InternalSerializationApi::class)
            override fun <T : Any> parse(text: String, kClass: KClass<T>): T {
                return json.decodeFromString(kClass.serializer(), text)
            }

            override fun <T : Any> parseSafe(text: String, kClass: KClass<T>): T? {
                return try {
                    parse(text, kClass)
                } catch (e: Exception) {
                    null
                }
            }

            override fun writeValueAsString(obj: Any): String {
                return json.encodeToString(obj)
            }

            inline fun <reified T> parse(text: String): T {
                return json.decodeFromString(text)
            }
        }

        return Requests(
            okHttpClient,
            defaultHeaders,
            defaultCacheTime = 6,
            defaultCacheTimeUnit = TimeUnit.HOURS,
            responseParser = mapper
        )
    }
}