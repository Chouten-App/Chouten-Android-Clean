package com.chouten.app.domain.use_case.module_use_cases

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.chouten.app.domain.model.ModuleModel
import com.chouten.app.domain.repository.ModuleRepository
import javax.inject.Inject

/**
 * Removes a module from the module folder
 * @param uri: [Uri] - The uri of the module to remove
 * @param onRemove: (suspend () -> Unit)? - A callback which is called when the module is removed
 * @throws IllegalArgumentException if the uri is not valid, or the module does not exist
 * within the module folder
 * @throws IOException if the folder cannot be deleted (e.g permissions)
 */
class RemoveModuleUseCase @Inject constructor(
    private val context: Context,
    private val moduleRepository: ModuleRepository,
    private val jsonParser: suspend (String) -> ModuleModel,
    private val log: suspend (String) -> Unit,
) {
    /**
     * Removes a module from the module folder
     * @param uri: [Uri] - The uri of the module to remove
     * @param onRemove: (suspend () -> Unit)? - A callback which is called when the module is removed
     * @throws IllegalArgumentException if the uri is not valid, or the module does not exist
     * within the module folder
     * @throws IOException if the folder cannot be deleted (e.g permissions)
     */
    suspend operator fun invoke(uri: Uri, onRemove: (suspend () -> Unit)? = null) {
        moduleRepository.removeModule(uri)
        onRemove?.let { it() } ?: log("Removed module $uri")
    }

    /**
     * Removes a module from the module folder
     * @param module: [ModuleModel] - The model of the module to remove
     * @param onRemove: (suspend () -> Unit)? - A callback which is called when the module is removed
     * @throws IllegalArgumentException if the uri is not valid, or the module does not exist
     * within the module folder
     * @throws IOException if the folder cannot be deleted (e.g permissions)
     * @see [RemoveModuleUseCase.invoke]
     */
    suspend operator fun invoke(module: ModuleModel, onRemove: (suspend () -> Unit)? = null) {
        moduleRepository.getModuleDirs().forEach { moduleUri ->
            // We need to parse the module metadata to get the module id
            // and compare it to the module id of the module we want to remove
            val contentResolver = context.contentResolver
            val metadataUri: Uri = contentResolver.query(
                moduleUri, arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME
                ), null, null, null
            )?.use { cursor ->
                val displayNameIndex =
                    cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val documentIdIndex =
                    cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                while (cursor.moveToNext()) {
                    if (cursor.getString(displayNameIndex) == "metadata.json") {
                        return@use DocumentsContract.buildChildDocumentsUriUsingTree(
                            moduleUri, cursor.getString(documentIdIndex)
                        )
                    }
                }
                null
            } ?: throw IllegalArgumentException("Invalid URI $moduleUri")

            val inputStream = contentResolver.openInputStream(metadataUri)
                ?: throw IllegalArgumentException("Invalid URI")
            val metadata = jsonParser(inputStream.bufferedReader().use { it.readText() })
            inputStream.close()

            if (metadata.id == module.id) {
                log("Removing module ${module.name}")
                moduleRepository.removeModule(moduleUri).also {
                    onRemove?.let { it() } ?: log("Removed module ${module.name}")
                }
                return@forEach
            }
        }
    }
}