package com.chouten.app.domain.repository

import com.chouten.app.domain.model.ModuleModel
import kotlinx.coroutines.flow.Flow

interface ModuleRepository {

    /**
     * Return a list of all useable/installed modules
     */
    suspend fun getModules(): Flow<List<ModuleModel>>

    /**
     * Adds a module to the module database
     * @param moduleModel: [ModuleModel] - The module (model)
     * @throws IllegalArgumentException if the module already exists
     */
    suspend fun addModule(moduleModel: ModuleModel)

    /**
     * Removes a module from the module folder
     * @param moduleId: [String] - The ID of the module to remove
     */
    suspend fun removeModule(moduleId: String)

    /**
     * Update/add properties to a entry in the module database
     * @param moduleModel: [ModuleModel] - The model to update in the database
     */
    suspend fun updateModule(moduleModel: ModuleModel)
}