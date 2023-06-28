package com.chouten.app.domain.use_case.log_use_cases

import com.chouten.app.domain.repository.LogRepository
import javax.inject.Inject

class DeleteAllLogsUseCase @Inject constructor(
    private val logRepository: LogRepository
) {
    suspend operator fun invoke() {
        logRepository.deleteAllLogEntries()
    }
}