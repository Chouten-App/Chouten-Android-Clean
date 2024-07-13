package com.chouten.app.di

import android.app.Application
import com.chouten.app.data.repository.ModuleEngineImpl
import com.chouten.app.domain.repository.ModuleEngine
import com.lagradost.nicehttp.Requests
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object ModuleEngine {
    @Singleton
    @Provides
    fun provideModuleEngine(context: Application, requests: Requests): ModuleEngine =
        ModuleEngineImpl(context, requests)
}