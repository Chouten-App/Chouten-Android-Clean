package com.chouten.app.data.repository

import com.chouten.app.data.data_source.HistoryDao
import com.chouten.app.domain.model.HistoryEntry
import com.chouten.app.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow

class HistoryRepositoryImpl(
    private val dao: HistoryDao
) : HistoryRepository {
    override fun getHistory(): Flow<List<HistoryEntry>> {
        return dao.getHistory()
    }

    override suspend fun getHistoryByUrl(url: String): HistoryEntry? {
        return dao.getHistoryByUrl(url)
    }

    override suspend fun insertHistoryEntry(historyEntry: HistoryEntry) {
        return dao.insertHistoryEntry(historyEntry)
    }

    override suspend fun updateHistoryEntry(historyEntry: HistoryEntry) {
        return dao.updateHistoryEntry(historyEntry)
    }

    override suspend fun deleteHistoryEntry(historyEntry: HistoryEntry) {
        return dao.deleteHistoryEntry(historyEntry)
    }
}