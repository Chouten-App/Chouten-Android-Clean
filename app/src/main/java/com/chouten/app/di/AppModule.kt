package com.chouten.app.di

import android.app.Application
import androidx.room.Room
import com.chouten.app.data.data_source.HistoryDatabase
import com.chouten.app.data.repository.HistoryRepositoryImpl
import com.chouten.app.data.repository.NavigationRepositoryImpl
import com.chouten.app.domain.repository.HistoryRepository
import com.chouten.app.domain.repository.NavigationRepository
import com.chouten.app.domain.use_case.history_use_cases.DeleteHistoryUseCase
import com.chouten.app.domain.use_case.history_use_cases.GetHistoryByUrlUseCase
import com.chouten.app.domain.use_case.history_use_cases.GetHistoryUseCase
import com.chouten.app.domain.use_case.history_use_cases.HistoryUseCases
import com.chouten.app.domain.use_case.history_use_cases.InsertHistoryUseCase
import com.chouten.app.domain.use_case.history_use_cases.UpdateHistoryUseCase
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

    @Singleton
    @Provides
    fun provideHistoryDatabase(app: Application): HistoryDatabase {
        return Room.databaseBuilder(
            app, HistoryDatabase::class.java, HistoryDatabase.DATABASE_NAME
        ).build()
    }

    @Singleton
    @Provides
    fun provideHistoryRepository(db: HistoryDatabase): HistoryRepository {
        return HistoryRepositoryImpl(db.historyDao)
    }

    @Singleton
    @Provides
    fun provideHistoryUseCases(historyRepository: HistoryRepository): HistoryUseCases {
        return HistoryUseCases(
            getHistory = GetHistoryUseCase(historyRepository),
            getHistoryByUrl = GetHistoryByUrlUseCase(historyRepository),
            insertHistory = InsertHistoryUseCase(historyRepository),
            deleteHistory = DeleteHistoryUseCase(historyRepository),
            updateHistory = UpdateHistoryUseCase(historyRepository)
        )
    }
}