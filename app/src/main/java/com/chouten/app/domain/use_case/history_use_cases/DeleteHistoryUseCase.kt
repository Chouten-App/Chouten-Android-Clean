package com.chouten.app.domain.use_case.history_use_cases

import com.chouten.app.domain.model.HistoryEntry
import com.chouten.app.domain.repository.HistoryRepository
import javax.inject.Inject

class DeleteHistoryUseCase @Inject constructor(
    private val historyRepository: HistoryRepository
) {
    /**
     * Deletes a history entry.
     * @param historyEntry: [HistoryEntry] The history entry to delete.
     */
    suspend operator fun invoke(historyEntry: HistoryEntry) {
        historyRepository.deleteHistoryEntry(historyEntry)
    }

    /**
     * Deletes a history entry by its URL.
     * Gets the history entry by its URL and deletes it.
     * @param url: [String] The URL of the history entry to delete.
     */
    suspend operator fun invoke(url: String) {
        historyRepository.getHistoryByUrl(url)?.let {
            historyRepository.deleteHistoryEntry(it)
        }
    }
}