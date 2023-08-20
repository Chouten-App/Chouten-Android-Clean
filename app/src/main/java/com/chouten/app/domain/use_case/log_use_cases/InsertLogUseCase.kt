package com.chouten.app.domain.use_case.log_use_cases

import com.chouten.app.domain.model.LogEntry
import com.chouten.app.domain.repository.LogRepository
import javax.inject.Inject

class InsertLogUseCase @Inject constructor(
    private val logRepository: LogRepository
) {
    @Throws(IllegalArgumentException::class)
    suspend operator fun invoke(logEntry: LogEntry) {
        logRepository.insertLogEntry(logEntry)
    }
}