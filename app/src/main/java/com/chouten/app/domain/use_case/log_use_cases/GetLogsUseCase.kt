package com.chouten.app.domain.use_case.log_use_cases

import com.chouten.app.common.OrderType
import com.chouten.app.domain.model.LogEntry
import com.chouten.app.domain.repository.LogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetLogsUseCase @Inject constructor(
    private val logRepository: LogRepository
) {
    operator fun invoke(order: OrderType = OrderType.Descending): Flow<List<LogEntry>> {
        return logRepository.getLogs().map {
            when (order) {
                is OrderType.Ascending -> it.sortedBy { logEntry -> logEntry.id }
                is OrderType.Descending -> it.sortedByDescending { logEntry -> logEntry.id }
            }
        }
    }
}