package com.chouten.app.di

import android.app.Application
import androidx.room.Room
import com.chouten.app.data.data_source.history.HistoryDatabase
import com.chouten.app.data.data_source.log.LogDatabase
import com.chouten.app.data.repository.HistoryRepositoryImpl
import com.chouten.app.data.repository.LogRepositoryImpl
import com.chouten.app.data.repository.NavigationRepositoryImpl
import com.chouten.app.data.repository.SnackbarRepositoryImpl
import com.chouten.app.domain.repository.HistoryRepository
import com.chouten.app.domain.repository.LogRepository
import com.chouten.app.domain.repository.NavigationRepository
import com.chouten.app.domain.repository.SnackbarRepository
import com.chouten.app.domain.use_case.history_use_cases.DeleteAllHistoryUseCase
import com.chouten.app.domain.use_case.history_use_cases.DeleteHistoryUseCase
import com.chouten.app.domain.use_case.history_use_cases.GetHistoryByUrlUseCase
import com.chouten.app.domain.use_case.history_use_cases.GetHistoryUseCase
import com.chouten.app.domain.use_case.history_use_cases.HistoryUseCases
import com.chouten.app.domain.use_case.history_use_cases.InsertHistoryUseCase
import com.chouten.app.domain.use_case.history_use_cases.UpdateHistoryUseCase
import com.chouten.app.domain.use_case.log_use_cases.DeleteAllLogsUseCase
import com.chouten.app.domain.use_case.log_use_cases.DeleteLogByIdUseCase
import com.chouten.app.domain.use_case.log_use_cases.GetLogByIdUseCase
import com.chouten.app.domain.use_case.log_use_cases.GetLogWithinRangeUseCase
import com.chouten.app.domain.use_case.log_use_cases.GetLogsUseCase
import com.chouten.app.domain.use_case.log_use_cases.InsertLogUseCase
import com.chouten.app.domain.use_case.log_use_cases.LogUseCases
import com.chouten.app.domain.use_case.navigation_use_cases.GetActiveDestinationUseCase
import com.chouten.app.domain.use_case.navigation_use_cases.GetNavigationItemsUseCase
import com.chouten.app.domain.use_case.navigation_use_cases.NavigationUseCases
import com.chouten.app.domain.use_case.navigation_use_cases.SetActiveNavigationItemUseCase
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
            deleteAllHistory = DeleteAllHistoryUseCase(historyRepository),
            updateHistory = UpdateHistoryUseCase(historyRepository),
        )
    }

    @Singleton
    @Provides
    fun provideLogDatabase(app: Application): LogDatabase {
        return Room.databaseBuilder(
            app, LogDatabase::class.java, LogDatabase.DATABASE_NAME
        ).build()
    }

    @Singleton
    @Provides
    fun provideLogRepository(db: LogDatabase): LogRepository {
        return LogRepositoryImpl(db.logDao)
    }

    @Singleton
    @Provides
    fun provideLogUseCases(logRepository: LogRepository): LogUseCases {
        return LogUseCases(
            getLogs = GetLogsUseCase(logRepository),
            getLogById = GetLogByIdUseCase(logRepository),
            getLogInRange = GetLogWithinRangeUseCase(logRepository),
            insertLog = InsertLogUseCase(logRepository),
            deleteLogById = DeleteLogByIdUseCase(logRepository),
            deleteAllLogs = DeleteAllLogsUseCase(logRepository)
        )
    }

    @Singleton
    @Provides
    fun provideNavigationUseCases(navigationRepository: NavigationRepository): NavigationUseCases {
        return NavigationUseCases(
            getNavigationItems = GetNavigationItemsUseCase(navigationRepository),
            getActiveDestination = GetActiveDestinationUseCase(navigationRepository),
            setActiveNavigationItem = SetActiveNavigationItemUseCase(navigationRepository)
        )
    }

    @Singleton
    @Provides
    fun provideSnackbarRepository(): SnackbarRepository {
        return SnackbarRepositoryImpl()
    }
}