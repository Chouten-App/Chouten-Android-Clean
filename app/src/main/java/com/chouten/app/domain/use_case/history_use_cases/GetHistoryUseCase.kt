package com.chouten.app.domain.use_case.history_use_cases

import com.chouten.app.common.OrderType
import com.chouten.app.domain.model.HistoryEntry
import com.chouten.app.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetHistoryUseCase @Inject constructor(
    private val historyRepository: HistoryRepository
) {
    /**
     * Returns a [Flow] of all [HistoryEntry]s.
     * @param historyOrder The order of the [HistoryEntry]s by date (default is [OrderType.Descending]).
     * Descending orders from most recent to least recent.
     * @return [Flow] of all [HistoryEntry]s.
     */
    operator fun invoke(historyOrder: OrderType = OrderType.Descending): Flow<List<HistoryEntry>> {
        return historyRepository.getHistory().map {
            when (historyOrder) {
                is OrderType.Ascending -> it.sortedBy { historyEntry -> historyEntry.entryLastUpdated }
                is OrderType.Descending -> it.sortedByDescending { historyEntry ->
                    historyEntry.entryLastUpdated
                }
            }
        }
    }
}