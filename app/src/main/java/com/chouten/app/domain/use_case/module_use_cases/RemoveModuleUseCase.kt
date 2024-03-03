package com.chouten.app.domain.use_case.module_use_cases

import com.chouten.app.domain.model.ModuleModel
import com.chouten.app.domain.repository.ModuleRepository
import javax.inject.Inject

class RemoveModuleUseCase @Inject constructor(
    private val moduleRepository: ModuleRepository,
) {

    /**
     * Removes a module from the module folder
     * @param module: [ModuleModel] - The model of the module to remove
     * @param onRemove: (suspend () -> Unit)? - A callback which is called when the module is removed
     */
    suspend operator fun invoke(module: ModuleModel, onRemove: (suspend () -> Unit)? = null) =
        moduleRepository.removeModule(module.id).also { onRemove?.invoke() }
}