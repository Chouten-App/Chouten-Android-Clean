package com.chouten.app.domain.use_case.module_use_cases

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.chouten.app.domain.model.ModuleModel
import com.chouten.app.domain.repository.ModuleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class GetAllModulesUseCase @Inject constructor(
    private val moduleRepository: ModuleRepository,
    private val mContext: Context,
    private val log: suspend (String) -> Unit
) {
    private val jsonParser: Json = Json { ignoreUnknownKeys = true }

    private enum class ModuleSubdirectory(val directoryName: String) {
        HOME("Home"), SEARCH("Search"), INFO("Info"), MEDIA("Media"),
    }

    /**
     * Takes the list of modules from the module repository and validates
     * them against the supported module version.
     * If there is a parsing error or the module is not supported, the module is removed from the list.
     */
    suspend operator fun invoke(): List<ModuleModel> {
        val potentialDirs = moduleRepository.getModuleDirs()

        // Modules which cannot be parsed or are not supported
        // are removed from the list by returning null to the mapNotNull function
        return potentialDirs.mapNotNull { moduleDirUri ->
            try {
                // Get the display name of the module
                val displayName =
                    DocumentFile.fromTreeUri(mContext, moduleDirUri)?.name ?: return@mapNotNull null
                
                // Folders ending within .tmp have not completed the
                // add module process and should be ignored
                if (displayName.endsWith(".tmp")) return@mapNotNull null

                val metadata = try {
                    getMetadata(moduleDirUri)
                } catch (e: Exception) {
                    e.printStackTrace()
                    log("Error parsing module $moduleDirUri: ${e.message}")
                    return@mapNotNull null
                }

                // Match the module against the constraints of the current version of the app
                // If the module matches, return the module directory uri
                if (!moduleMatcher(metadata)) {
                    return@mapNotNull null
                }

                // Parse the rest of the module (e.g icon, code)
                // and return the module
                val icon = getIcon(moduleDirUri)

                val homeCode = getModuleCode(moduleDirUri, ModuleSubdirectory.HOME).apply {
                    if (isEmpty()) log("Home code is empty")
                }

                val infoCode = getModuleCode(moduleDirUri, ModuleSubdirectory.INFO).apply {
                    if (isEmpty()) log("Info code is empty")
                }

                val searchCode = getModuleCode(moduleDirUri, ModuleSubdirectory.SEARCH).apply {
                    if (isEmpty()) log("Search code is empty")
                }

                val mediaCode = getModuleCode(moduleDirUri, ModuleSubdirectory.MEDIA).apply {
                    if (isEmpty()) log("Media code is empty")
                }

                log("Successfully parsed module ${metadata.name}")

                metadata.metadata.icon = icon
                metadata.code = ModuleModel.ModuleCode(
                    homeCode, searchCode, infoCode, mediaCode
                )

                metadata
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
        }
    }

    /**
     * Checks if the module is supported by the current version of the app
     * @param metadata The metadata of the module
     * @return true if the module is supported, false otherwise
     */
    private fun moduleMatcher(metadata: ModuleModel) =
        metadata.formatVersion >= ModuleModel.MIN_FORMAT_VERSION

    /**
     * Gets the metadata.json file from the module directory
     * and parses it into a ModuleModel
     * @param moduleDirUri The uri of the module directory
     * @return The metadata of the module
     * @throws IllegalArgumentException if the metadata.json file does not exist / cannot be read
     * @throws IllegalArgumentException if the metadata.json file cannot be parsed
     */
    private fun getMetadata(moduleDirUri: Uri): ModuleModel {
        /**
         * metadataUri is a DocumentUri (a uri which points to a single document)
         * It is used to get the metadata.json file from the module directory
         */
        var metadataUri: Uri? = null

        // Get the metadata.json file from the module directory
        // if it exists
        // Throw an IllegalArgumentException if it does not exist
        mContext.contentResolver.query(
            moduleDirUri, arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME
            ), null, null, null
        )?.use { cursor ->
            val displayNameIndex =
                cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val documentIdIndex =
                cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)

            // Iterate over each row (each file in the module directory)
            while (cursor.moveToNext()) {
                if (cursor.getString(displayNameIndex) == "metadata.json") {
                    val documentId = cursor.getString(documentIdIndex)

                    // We have found the metadata.json file
                    metadataUri = DocumentsContract.buildDocumentUriUsingTree(
                        moduleDirUri, documentId
                    )
                        ?: throw IllegalArgumentException("Could not build metadataUri for $moduleDirUri")
                    break
                }
            }
        }
        // If metadataUri is null, then we could not find the metadata.json file
        if (metadataUri == null) throw IllegalArgumentException("Could not find metadata.json for $moduleDirUri")

        // Can be smart casted to non-null because of the above check
        val inputStream = mContext.contentResolver.openInputStream(metadataUri!!)

        // Read the metadata.json file into a string using a buffer
        val buffer = inputStream?.bufferedReader()?.useLines { lines ->
            lines.fold("") { some, text ->
                "$some\n$text"
            }
        } ?: throw IllegalArgumentException("Could not read metadata.json for $moduleDirUri")

        inputStream.close()

        return jsonParser.decodeFromString<ModuleModel>(buffer)
    }

    /**
     * Gets the icon file from the module directory
     * The icon file is the first file in the module directory which matches the regex "icon.(png|jpg|jpeg)"
     * @param moduleDirUri The uri of the module directory
     * @return The icon of the module as a byte array
     * @throws IllegalArgumentException if the icon file cannot be read
     */
    private fun getIcon(moduleDirUri: Uri): ByteArray? {
        val icon = ByteArrayOutputStream()
        mContext.contentResolver.query(
            moduleDirUri, arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME
            ), null, null, null
        )?.use { cursor ->
            val displayNameIndex =
                cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val documentIdIndex =
                cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)

            // Iterate over each row (each file in the module directory)
            while (cursor.moveToNext()) {
                if (cursor.getString(displayNameIndex).matches(Regex("icon.(png|jpg|jpeg)"))) {
                    val documentId = cursor.getString(documentIdIndex)

                    // We have found the icon file
                    val iconUri = DocumentsContract.buildDocumentUriUsingTree(
                        moduleDirUri, documentId
                    ) ?: throw IllegalArgumentException("Could not build iconUri for $moduleDirUri")

                    // Can be smart casted to non-null because of the above check
                    val inputStream = mContext.contentResolver.openInputStream(iconUri)!!

                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, icon)

                    inputStream.close()
                    break
                }
            }
        }
        return icon.toByteArray()
    }

    /**
     * Gets the code files from within the module directory
     * @param moduleDirUri The uri of the module directory
     * @param subfolder the subfolder of the module directory to get the code files from (e.g HOME)
     * @return The code files of the module as a byte array
     * @throws IllegalArgumentException if the code files cannot be read
     */
    private suspend fun getModuleCode(
        moduleDirUri: Uri, subfolder: ModuleSubdirectory
    ): List<ModuleModel.ModuleCode.ModuleCodeblock> {
        val codeblocks = mutableListOf<ModuleModel.ModuleCode.ModuleCodeblock>()

        /**
         * reading is used to keep track of how many
         * modules are being read
         * Each time a file is being read, reading is incremented
         * Each time a file is finished being read, reading is decremented
         */
        var reading = 0

        var subdirUri: Uri? = null
        // Get the subfolder uri
        mContext.contentResolver.query(
            moduleDirUri, arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME
            ), null, null, null
        )?.use { cursor ->
            val documentNameField =
                cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val documentIdField =
                cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            while (cursor.moveToNext()) {
                if (cursor.getString(documentNameField) == subfolder.directoryName) {
                    val documentId = cursor.getString(documentIdField)
                    subdirUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                        moduleDirUri, documentId
                    )
                        ?: throw IllegalArgumentException("Could not build subdirUri for $moduleDirUri")
                    break
                }
            }
        } ?: throw IllegalArgumentException("Could not find subfolder $subfolder for $moduleDirUri")

        // Get all the js files within the module subfolder
        mContext.contentResolver.query(
            subdirUri!!, arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME
            ), null, null, null, null
        )?.use { cursor ->
            val displayNameIndex =
                cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val documentIdIndex =
                cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            while (cursor.moveToNext()) {
                val documentId = cursor.getString(documentIdIndex)
                val displayName = cursor.getString(displayNameIndex)

                // If the file isn't a js file, skip it
                if (!displayName.endsWith(".js")) continue


                withContext(Dispatchers.IO) {
                    reading += 1
                    val codeUri = DocumentsContract.buildDocumentUriUsingTree(
                        subdirUri, documentId
                    ) ?: throw IllegalArgumentException("Could not build codeUri for $moduleDirUri")

                    // Start reading the file
                    val inputStream = mContext.contentResolver.openInputStream(codeUri)

                    log("Reading $displayName in ${subfolder.directoryName}")

                    inputStream?.bufferedReader()?.useLines {
                        // Read the file into a string using a buffer
                        it.fold("") { some, text ->
                            "$some\n$text"
                        }
                    }?.apply {
                        log("Finished reading $displayName in ${subfolder.directoryName}")
                        val codeblock = ModuleModel.ModuleCode.ModuleCodeblock(
                            null,
                            this,
                        )
                        codeblocks.add(codeblock)
                    }

                    inputStream?.close()
                    reading -= 1
                }
            }
        }

        // Wait until all the files have been read
        while (reading > 0) {
            delay(100)
        }

        Log.d("ModuleManager", "Finished reading all files")
        return codeblocks
    }
}