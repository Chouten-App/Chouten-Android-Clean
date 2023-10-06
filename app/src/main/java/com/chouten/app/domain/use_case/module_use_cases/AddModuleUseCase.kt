package com.chouten.app.domain.use_case.module_use_cases

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.chouten.app.common.OutOfDateAppException
import com.chouten.app.common.OutOfDateModuleException
import com.chouten.app.domain.model.ModuleModel
import com.chouten.app.domain.proto.filepathDatastore
import com.chouten.app.domain.repository.ModuleRepository
import com.lagradost.nicehttp.Requests
import kotlinx.coroutines.flow.first
import java.io.IOException
import javax.inject.Inject

class AddModuleUseCase @Inject constructor(
    private val mContext: Context,
    private val moduleRepository: ModuleRepository,
    private val httpClient: Requests,
    private val log: suspend (String) -> Unit,
    private val jsonParser: suspend (String) -> ModuleModel,
    private val getModuleDir: suspend (Uri) -> Uri,
) {

    /**
     * Adds a module to the module folder
     * @param uri The URI of the module (either a local file or a remote resource)
     * @throws IOException if the module cannot be downloaded or added (e.g duplicate/unsupported version)
     * @throws IllegalArgumentException if the URI is invalid. Not a valid module (e.g not a zip or no metadata)
     */
    suspend operator fun invoke(uri: Uri) {
        val contentResolver = mContext.contentResolver
        val preferences = mContext.filepathDatastore.data.first()
        val moduleDirUri = getModuleDir(preferences.CHOUTEN_ROOT_DIR)

        val moduleDirs = moduleRepository.getModuleDirs()
        val moduleCount = moduleDirs.size

        /**
         * Throw an exception safely, deleting the module directory if it exists
         */
        val safeException: suspend (Exception, Uri?) -> Nothing = { it, toDelete ->
            log("Error adding module: ${it.message}")
            toDelete?.let { deleteUri ->
                DocumentFile.fromTreeUri(
                    mContext, deleteUri.toString().removeSuffix("/children").toUri()
                )?.delete()
            }
            throw it
        }

        // If the URI points to a remote resource, we must first download it
        val isRemote = uri.scheme in setOf("http", "https")

        /**
         * The URI of the module which we will pass on to the module repository
         * If the module is remote, this will be the URI of the module folder
         * If the module is local, the new URI will be null
         */
        val newUri: Uri? = if (isRemote) {
            log("Downloading remote module $uri")

            val byteStream = httpClient.get(
                uri.toString()
            ).body.byteStream()

            // Save the module to the module folder
            val moduleFile = mContext.cacheDir?.resolve(
                uri.lastPathSegment ?:
                // If the module does not have a name, we will generate a random one
                "Module $moduleCount"
            ) ?: throw IOException("Could not create module directory")

            // Write the bytes from the remote resource to the module file
            moduleFile.outputStream().use { outputStream ->
                byteStream.use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            log("Downloaded module to $moduleFile")

            FileProvider.getUriForFile(
                mContext, "${mContext.packageName}.provider", moduleFile
            ) ?: throw IOException("Could not get module URI")
        } else null

        /**
         * The URI of the module which has been added to the module folder
         */
        val newModuleUri = try {
            moduleRepository.addModule(
                newUri ?: uri
            ).also { _ ->
                if (isRemote) {
                    // Delete the module file if we downloaded it
                    mContext.cacheDir?.resolve(
                        uri.lastPathSegment ?:
                        // If the module does not have a name, we will generate a random one
                        "Module $moduleCount"
                    )?.delete()
                }
            }.let { moduleUri ->
                // Rename the module folder to include .tmp as a suffix
                // This is to prevent the module from being parsed by the module repository
                // if it is not fully parsed yet
                val renamed = DocumentsContract.renameDocument(
                    contentResolver,
                    moduleUri,
                    ((newUri ?: uri).lastPathSegment ?: "Module $moduleCount") + ".tmp"
                ) ?: safeException(
                    IOException("Could not rename module folder to <name>.tmp; try clearing module artifacts"),
                    moduleUri
                )

                DocumentsContract.buildChildDocumentsUriUsingTree(
                    moduleUri, DocumentsContract.getDocumentId(renamed)
                ) ?: safeException(
                    IOException("Could not get module folder children"), moduleUri
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Delete the module file if we downloaded it
            if (isRemote) {
                mContext.cacheDir?.resolve(
                    uri.lastPathSegment ?:
                    // If the module does not have a name, we will generate a random one
                    "Module $moduleCount"
                )?.delete()
            }
            safeException(e, null)
        }

        // Add a .nomedia file to the module folder
        if (DocumentsContract.createDocument(
                contentResolver, newModuleUri, "application/octet-stream", ".nomedia"
            ) == null
        ) {
            // No need to throw an exception here, as it does not affect the functionality of the app
            log("Could not create .nomedia file in $newModuleUri")
        }

        // Parse the module and test if it is valid
        // If valid, we can rename the module folder to the module name
        log("Parsing module $newModuleUri")

        // Get the metadata of the module
        val metadataUri: Uri = contentResolver.query(
            newModuleUri, arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME
            ), null, null, null
        )?.use { cursor ->
            val displayNameIndex =
                cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val documentIdIndex =
                cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)

            while (cursor.moveToNext()) {
                val displayName = cursor.getString(displayNameIndex)
                val documentId = cursor.getString(documentIdIndex)

                if (displayName == "metadata.json") {
                    return@use DocumentsContract.buildDocumentUriUsingTree(
                        newModuleUri, documentId
                    )
                }
            }

            // We didn't find the metadata file
            null
        } ?: safeException(IllegalArgumentException("Could not get module metadata"), newModuleUri)

        // Get the metadata file
        val metadataInputStream = contentResolver.openInputStream(metadataUri) ?: safeException(
            IOException("Could not open metadata file"), newModuleUri
        )

        // If something goes wrong, we must delete the module folder

        /**
         * The parsed module
         */
        val module = metadataInputStream.use {
            val stringBuffer = StringBuffer()
            it.bufferedReader().use { reader ->
                var line = reader.readLine()
                while (line != null) {
                    stringBuffer.append(line)
                    line = reader.readLine()
                }
            }

            jsonParser(stringBuffer.toString())
        }

        // Check if the module format version is supported
        if (module.formatVersion < ModuleModel.MIN_FORMAT_VERSION) {
            log(
                """
                Unsupported module format version ${module.formatVersion} for ${module.name}
                Minimum supported version is ${ModuleModel.MIN_FORMAT_VERSION}
                """.trimIndent()
            )
            safeException(
                OutOfDateModuleException("${module.name} is out of date (v${module.formatVersion}). Please update the module"),
                newModuleUri
            )
        } else if (module.formatVersion > ModuleModel.MAX_FORMAT_VERSION) {
            log(
                """
                Unsupported module format version ${module.formatVersion} for ${module.name}
                Current supported version is ${ModuleModel.MAX_FORMAT_VERSION}
                """.trimIndent()
            )
            safeException(
                OutOfDateAppException("This version of Chouten does not support ${module.name} (v${module.formatVersion}). Please update Chouten"),
                newModuleUri
            )
        }

        // Compare the module with the existing modules
        // If the module already exists, we must check for updates
        val metadataUriPairs: List<Pair<Uri, ModuleModel>> = moduleDirs.mapNotNull {
            // We don't want to compare the module with itself
            if (it == newModuleUri) {
                return@mapNotNull null
            }

            contentResolver.query(
                it, arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME
                ), null, null, null
            )?.use { cursor ->
                val displayNameIndex =
                    cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val documentIdIndex =
                    cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)

                while (cursor.moveToNext()) {
                    val displayName =
                        cursor.getString(displayNameIndex) ?: return@mapNotNull null
                    val documentId = cursor.getString(documentIdIndex) ?: return@mapNotNull null

                    if (displayName == "metadata.json") {
                        val otherModuleMetadataUri =
                            DocumentsContract.buildDocumentUriUsingTree(
                                moduleDirUri, documentId
                            ) ?: safeException(
                                IllegalArgumentException("Could not get module metadata URI"),
                                newModuleUri
                            )

                        val otherMetadataIS =
                            contentResolver.openInputStream(otherModuleMetadataUri)
                                ?: safeException(
                                    IOException("Could not open metadata file"),
                                    otherModuleMetadataUri
                                )

                        val parsedModule = try {
                            val stringBuffer = StringBuffer()
                            otherMetadataIS.bufferedReader().use { reader ->
                                var line = reader.readLine()
                                while (line != null) {
                                    stringBuffer.append(line)
                                    line = reader.readLine()
                                }
                            }

                            jsonParser(stringBuffer.toString())
                        } catch (e: Exception) {
                            e.printStackTrace()
                            safeException(
                                IllegalArgumentException("Could not parse module metadata", e),
                                otherModuleMetadataUri
                            )
                        }

                        otherMetadataIS.close()

                        return@mapNotNull (it to parsedModule)
                    }
                }
                return@mapNotNull null
            } ?: safeException(
                IllegalArgumentException("Could not query module folder"), newModuleUri
            )
        }

        // Compare the module with the existing modules
        metadataUriPairs.forEach {
            log("Comparing module ${module.id} (${module.version}) with ${it.second.id} (${it.second.version})")
            // Check if the module already exists
            if (module.id == it.second.id) {
                val ret: Int = try {
                    compareSemVer(module.version, it.second.version)
                } catch (e: IllegalArgumentException) {
                    // Find which module has the invalid version
                    try {
                        compareSemVer("0.0.0", module.version)
                    } catch (e: IllegalArgumentException) {
                        // The module being installed has an invalid version
                        e.printStackTrace()
                        safeException(e, newModuleUri)
                    }

                    try {
                        compareSemVer("0.0.0", it.second.version)
                    } catch (e: IllegalArgumentException) {
                        e.printStackTrace()
                        // The existing module has an invalid version
                        // We can make a note of it and let the module be installed
                        log("Module ${it.second.name} (${it.second.id}) has an invalid version (${it.second.version})")
                        1
                    }
                }

                when (ret) {
                    // Module being installed is newer than the existing module
                    1 -> {
                        // Delete the old module
                        if (DocumentFile.fromSingleUri(mContext, it.first)?.delete() == false) {
                            safeException(
                                IOException("Could not delete module ${it.second.name} (${it.second.id})"),
                                newModuleUri
                            )
                        }
                        log("Updated module ${module.name} (${module.id})")
                    }

                    // Module being installed is the same version as the existing module
                    0 -> {
                        safeException(
                            IllegalArgumentException("Module ${module.name} (${module.id}) already exists"),
                            newModuleUri
                        )
                    }

                    // Module being installed is older than the existing module
                    else -> {
                        safeException(
                            IllegalArgumentException("Module ${module.name} (${module.id}) is older than the existing module"),
                            newModuleUri
                        )
                    }
                }
            }
        }

        // Rename the module folder to the module name

        /**
         * A DocumentFile of newModuleUri
         */
        val moduleFolder = DocumentFile.fromTreeUri(
            mContext, newModuleUri.toString().removeSuffix("/children").toUri()
        ) ?: safeException(IOException("Could not get module folder"), newModuleUri)

        // Not being able to rename the folder is a critical error as
        // it will cause the module to not be loaded (.tmp suffix will still be there)
        // We must throw a safe exception
        DocumentsContract.renameDocument(
            contentResolver, newModuleUri, module.name
        ) ?: safeException(
            IOException("Could not rename module folder to <name>.tmp; try clearing module artifacts"),
            newModuleUri
        )
    }

    /**
     * Compare two semVer versions
     * @param v1 The first version
     * @param v2 The second version
     * @return 1 if the first version is newer, -1 if the second version is newer, 0 if the versions are the same
     * @throws IllegalArgumentException if the versions are invalid
     */
    private fun compareSemVer(v1: String, v2: String): Int {
        // Return 1 if the first version is newer
        // Return -1 if the second version is newer
        // Return 0 if the versions are the same
        val v1Split = v1.split(".")
        val v2Split = v2.split(".")
        if (v1Split.size != 3 || v2Split.size != 3) {
            throw IllegalArgumentException("Invalid version")
        }

        return if (v1Split[0].toInt() > v2Split[0].toInt()) {
            1
        } else if (v1Split[0].toInt() < v2Split[0].toInt()) {
            -1
        } else if (v1Split[1].toInt() > v2Split[1].toInt()) {
            1
        } else if (v1Split[1].toInt() < v2Split[1].toInt()) {
            -1
        } else if (v1Split[2].toInt() > v2Split[2].toInt()) {
            1
        } else if (v1Split[2].toInt() < v2Split[2].toInt()) {
            -1
        } else {
            0
        }
    }
}