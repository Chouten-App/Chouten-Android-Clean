package com.chouten.app.domain.use_case.history_use_cases

import com.chouten.app.domain.repository.HistoryRepository
import javax.inject.Inject

class DeleteAllHistoryUseCase @Inject constructor(
    private val historyRepository: HistoryRepository
) {
    suspend operator fun invoke() {
        historyRepository.deleteAllHistoryEntries()
    }
}