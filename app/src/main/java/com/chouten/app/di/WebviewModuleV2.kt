package com.chouten.app.di

import com.chouten.app.data.repository.WebviewHandlerImpl
import com.chouten.app.domain.model.Payloads_V2
import com.chouten.app.domain.repository.WebviewHandler
import com.chouten.app.presentation.ui.screens.home.SearchResult
import com.lagradost.nicehttp.Requests
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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
}
