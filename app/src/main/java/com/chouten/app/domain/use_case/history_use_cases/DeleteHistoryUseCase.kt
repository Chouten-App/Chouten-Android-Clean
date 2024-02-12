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
     * Deletes a history entry by its composite primary key.
     * Gets the history entry by its key and deletes it.
     * @param url: [String] The Parent URL of the history entry to delete.
     * @param index: [Int] The Media Index of the history entry to delete (0-based)
     */
    suspend operator fun invoke(url: String, index: Int) {
        historyRepository.getHistoryByPKey(url, index)?.let {
            historyRepository.deleteHistoryEntry(it)
        }
    }
}