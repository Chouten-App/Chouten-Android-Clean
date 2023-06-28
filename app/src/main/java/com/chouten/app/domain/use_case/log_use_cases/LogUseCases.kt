package com.chouten.app.domain.use_case.log_use_cases

data class LogUseCases(
    val getLogs: GetLogsUseCase,
    val getLogById: GetLogByIdUseCase,
    val getLogInRange: GetLogWithinRangeUseCase,
    val insertLog: InsertLogUseCase,
    val deleteLogById: DeleteLogByIdUseCase,
    val deleteAllLogs: DeleteAllLogsUseCase
)