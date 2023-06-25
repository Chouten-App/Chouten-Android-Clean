package com.chouten.app.domain.use_case.history_use_cases

data class HistoryUseCases(
    val getHistory: GetHistoryUseCase,
    val getHistoryByUrl: GetHistoryByUrlUseCase,
    val insertHistory: InsertHistoryUseCase,
    val deleteHistory: DeleteHistoryUseCase,
    val deleteAllHistory: DeleteAllHistoryUseCase,
    val updateHistory: UpdateHistoryUseCase,
)
