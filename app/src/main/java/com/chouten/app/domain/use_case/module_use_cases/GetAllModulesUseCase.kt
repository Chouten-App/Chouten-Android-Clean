package com.chouten.app.domain.use_case.module_use_cases

import android.util.Log
import com.chouten.app.domain.model.ModuleModel
import com.chouten.app.domain.repository.ModuleRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class GetAllModulesUseCase @Inject constructor(
    private val moduleRepository: ModuleRepository, private val log: suspend (String) -> Unit
) {
    /**
     * Takes the list of modules from the module repository and validates
     * them against the supported module version.
     * If there is a parsing error or the module is not supported, the module is removed from the list.
     */
    suspend operator fun invoke() {
        // Modules which cannot be parsed or are not supported
        // are removed from the list by returning null to the mapNotNull function
        moduleRepository.getModules().firstOrNull()?.mapNotNull { module ->
            try {
                // Match the module against the constraints of the current version of the app
                // If the module matches, return the module directory uri
                if (!moduleMatcher(module)) {
                    return@mapNotNull null
                }

                log("Successfully parsed module ${module.name}")
                module
            } catch (e: IllegalArgumentException) {
                Log.e("GetAllModulesUseCase", "Could not parse Module")
                log("Could not parse Module\n${e.message}")
                e.printStackTrace()
                null
            } catch (e: Exception) {
                log("Could not parse Module\n${e.message}")
                e.printStackTrace()
                null
            }
        } ?: emptyList()
    }
}

/**
 * Checks if the module is supported by the current version of the app
 * @param metadata The metadata of the module
 * @return true if the module is supported, false otherwise
 */
private fun moduleMatcher(metadata: ModuleModel) =
    metadata.formatVersion >= ModuleModel.MIN_FORMAT_VERSION