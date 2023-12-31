package com.chouten.app.di

import com.chouten.app.data.repository.WebviewHandlerImpl
import com.chouten.app.domain.model.Payloads_V2
import com.chouten.app.domain.repository.WebviewHandler
import com.chouten.app.presentation.ui.screens.info.InfoResult
import com.chouten.app.presentation.ui.screens.info.SwitchConfig
import com.chouten.app.presentation.ui.screens.search.SearchResult
import com.chouten.app.presentation.ui.screens.watch.WatchResult
import com.lagradost.nicehttp.Requests
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object WebviewModuleV2 {

    @Singleton
    @Provides
    fun provideSearchWebviewHandler(client: Requests): WebviewHandler<Payloads_V2.Action_V2, Payloads_V2.GenericPayload<List<SearchResult>>> {
        return WebviewHandlerImpl(client) { action, result: String ->
            Payloads_V2.GenericPayload(action, Json.decodeFromString(result))
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Provides
    fun provideSwitchConfigWebviewHandler(client: Requests): WebviewHandler<Payloads_V2.Action_V2, Payloads_V2.GenericPayload<SwitchConfig>> {
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
        }
        return WebviewHandlerImpl(client) { action, result: String ->
            Payloads_V2.GenericPayload(action, json.decodeFromString(result))
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Provides
    fun provideInfoMetadataWebviewHandler(client: Requests): WebviewHandler<Payloads_V2.Action_V2, Payloads_V2.GenericPayload<InfoResult>> {
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
        }
        return WebviewHandlerImpl(client) { action, result: String ->
            Payloads_V2.GenericPayload(action, json.decodeFromString(result))
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Provides
    fun provideInfoEpListWebviewHandler(client: Requests): WebviewHandler<Payloads_V2.Action_V2, Payloads_V2.GenericPayload<List<InfoResult.MediaListItem>>> {
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
        }
        return WebviewHandlerImpl(client) { action, result: String ->
            Payloads_V2.GenericPayload(action, json.decodeFromString(result))
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Provides
    fun provideServerWebviewHandler(client: Requests): WebviewHandler<Payloads_V2.Action_V2, Payloads_V2.GenericPayload<List<WatchResult.ServerData>>> {
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
        }
        return WebviewHandlerImpl(client) { action, result: String ->
            Payloads_V2.GenericPayload(action, json.decodeFromString(result))
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Provides
    fun provideSourceWebviewHandler(client: Requests): WebviewHandler<Payloads_V2.Action_V2, Payloads_V2.GenericPayload<WatchResult>> {
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
        }
        return WebviewHandlerImpl(client) { action, result: String ->
            Payloads_V2.GenericPayload(action, json.decodeFromString(result))
        }
    }
}
