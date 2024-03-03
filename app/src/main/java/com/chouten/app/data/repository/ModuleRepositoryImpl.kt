package com.chouten.app.data.repository

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import com.chouten.app.data.data_source.module.ModuleDatabase
import com.chouten.app.domain.model.ModuleModel
import com.chouten.app.domain.repository.ModuleRepository
import javax.inject.Inject

class ModuleRepositoryImpl @Inject constructor(
    val context: Context, val moduleDatabase: ModuleDatabase
) : ModuleRepository {

    /**
     * Return a list of all useable/installed modules
     */
    override suspend fun getModules() = moduleDatabase.moduleDao.getModules()

    /**
     * Adds a module to the module database
     * @param moduleModel: [ModuleModel] - The module (model)
     * @throws IllegalArgumentException if the module already exists
     */
    override suspend fun addModule(moduleModel: ModuleModel) = try {
        moduleDatabase.moduleDao.addModule(moduleModel)
    } catch (e: SQLiteConstraintException) {
        e.printStackTrace()
        throw IllegalArgumentException(e)
    }

    /**
     * Update a module entry in the module database
     * @param moduleModel: [ModuleModel] - The module (model)
     */
    override suspend fun updateModule(moduleModel: ModuleModel) =
        moduleDatabase.moduleDao.updateModule(moduleModel)

    /**
     * Removes a module from the module folder
     * @param moduleId: [String] - The ID of the module to remove
     */
    override suspend fun removeModule(moduleId: String) =
        moduleDatabase.moduleDao.removeModule(moduleId)
}