package com.chouten.app.domain.use_case.module_use_cases

data class ModuleUseCases(
    val getModuleUris: GetAllModulesUseCase,
    val getModuleDir: GetModuleDirUseCase,
    val addModule: AddModuleUseCase,
    val removeModule: RemoveModuleUseCase
)