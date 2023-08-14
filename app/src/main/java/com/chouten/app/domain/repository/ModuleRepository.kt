package com.chouten.app.domain.repository

import android.net.Uri
import java.io.File

interface ModuleRepository {
    /**
     * Returns a list of all modules in the module folder
     * A module is viewed any folder in the module folder
     * Will not filter out invalid modules (e.g modules which are older than the supported version)
     * @return List<File>
     * @throws IOException if the module folder cannot be read (e.g permissions)
     */
    suspend fun getModuleDirs(): List<File>

    /**
     * Adds a module to the module folder
     * @param uri: Uri - The uri of the module to add
     * @throws IllegalArgumentException if the module already exists
     * @throws IllegalArgumentException if the uri is not valid
     * @throws IOException if the folder cannot be written (e.g permissions)
     */
    fun addModule(uri: Uri)

    /**
     * Removes a module from the module folder
     * @param uri: Uri - The uri of the module to remove
     * @throws IllegalArgumentException if the uri is not valid, or the module does not exist
     * within the module folder
     * @throws IOException if the folder cannot be deleted (e.g permissions)
     */
    fun removeModule(uri: Uri)
}