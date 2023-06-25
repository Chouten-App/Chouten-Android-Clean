package com.chouten.app.di

import com.chouten.app.domain.repository.NavigationRepository
import com.chouten.app.data.repository.NavigationRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideNavigationRepository(): NavigationRepository {
        return NavigationRepositoryImpl()
    }
}