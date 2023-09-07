package com.chouten.app.domain.repository

import android.net.Uri

interface ModuleRepository {
    /**
     * Returns a list of all modules in the module folder
     * A module is viewed any folder in the module folder
     * Will not filter out invalid modules (e.g modules which are older than the supported version)
     * @return List<Uri>
     * @throws IOException if the module folder cannot be read (e.g permissions)
     */
    suspend fun getModuleDirs(): List<Uri>

    /**
     * Adds a module to the module folder
     * @param uri: Uri - The uri of the module to add
     * @return Uri - The uri of the new module folder which was created
     * @throws IllegalArgumentException if the module already exists
     * @throws IllegalArgumentException if the uri is not valid
     * @throws IOException if the folder cannot be written (e.g permissions)
     */
    suspend fun addModule(uri: Uri): Uri

    /**
     * Removes a module from the module folder
     * @param uri: Uri - The uri of the module to remove
     * @throws IllegalArgumentException if the uri is not valid, or the module does not exist
     * within the module folder
     * @throws IOException if the folder cannot be deleted (e.g permissions)
     */
    suspend fun removeModule(uri: Uri)
}