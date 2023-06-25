package com.chouten.app.domain.use_case.history_use_cases

import com.chouten.app.domain.model.HistoryEntry
import com.chouten.app.domain.repository.HistoryRepository
import javax.inject.Inject

class GetHistoryByUrlUseCase @Inject constructor(
    private val historyRepository: HistoryRepository
) {
    /**
     * Returns a either a [HistoryEntry] if the entry exists or null if it doesn't.
     */
    suspend operator fun invoke(url: String): HistoryEntry? = historyRepository.getHistoryByUrl(url)
}