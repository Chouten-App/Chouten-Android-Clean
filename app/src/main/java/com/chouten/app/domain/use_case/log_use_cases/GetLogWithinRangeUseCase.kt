package com.chouten.app.domain.use_case.log_use_cases

import com.chouten.app.domain.repository.LogRepository
import java.sql.Date
import java.sql.Timestamp
import javax.inject.Inject

class GetLogWithinRangeUseCase @Inject constructor(
    private val logRepository: LogRepository
) {
    @Throws(IllegalArgumentException::class)
    suspend operator fun invoke(from: Timestamp, to: Timestamp) {
        if (from > to) {
            throw IllegalArgumentException("Cannot measure log within range where from > to")
        } else if (from > Date(System.currentTimeMillis())) {
            throw IllegalArgumentException("Cannot measure log within range in the future")
        } else if (from < Date(0) || to < Date(0)) {
            throw IllegalArgumentException("Cannot measure log within range before the epoch")
        }
        logRepository.getLogWithinRange(from, to)
    }
}