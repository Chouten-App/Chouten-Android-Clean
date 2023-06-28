package com.chouten.app.domain.use_case.log_use_cases

import com.chouten.app.domain.repository.LogRepository
import javax.inject.Inject

class DeleteLogByIdUseCase @Inject constructor(
    private val logRepository: LogRepository
) {
    @Throws(IllegalArgumentException::class)
    suspend operator fun invoke(id: Int) {
        if (id < 1) {
            throw IllegalArgumentException("Invalid Log ID. ID cannot be less than 1.")
        }
        logRepository.deleteLogEntry(id)
    }
}