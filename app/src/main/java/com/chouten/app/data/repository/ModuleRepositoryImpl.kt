package com.chouten.app.data.repository

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.chouten.app.domain.proto.filepathDatastore
import com.chouten.app.domain.repository.ModuleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.zip.ZipInputStream
import javax.inject.Inject

class ModuleRepositoryImpl @Inject constructor(
    val context: Context,
    val getModuleDir: suspend (Uri) -> Uri,
) : ModuleRepository {

    /**
     * Returns a list of all modules in the module folder
     * A module is viewed as any folder in the module folder
     * Will not filter out invalid modules (e.g modules which are older than the supported version)
     * @return List<Uri>
     * @throws IOException if the module folder cannot be read (e.g permissions)
     */
    override suspend fun getModuleDirs(): List<Uri> {

        val preferences = context.filepathDatastore.data.first()
        val contentResolver = context.contentResolver

        /*
        moduleDirUri is a ChildDocumentsUri (a uri which points to a directory
        which contains child documents)
         */
        val moduleDirUri = getModuleDir(preferences.CHOUTEN_ROOT_DIR)

        val moduleDirs = mutableListOf<Uri>()
        contentResolver.query(
            moduleDirUri, arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME
            ), null, null, null
        )?.use { cursor ->
            val displayNameIndex =
                cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val documentIdIndex =
                cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            while (cursor.moveToNext()) {

                // Ignore the current directory
                if (cursor.getString(documentIdIndex) == DocumentsContract.getTreeDocumentId(
                        moduleDirUri
                    )
                ) {
                    continue
                }

                val moduleUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                    moduleDirUri, cursor.getString(documentIdIndex)
                )

                // The mime type of the moduleUri is not a directory, we
                // want to skip it
                if (moduleUri == null || DocumentFile.fromTreeUri(
                        context,
                        moduleUri
                    )?.isDirectory != true
                ) {
                    continue
                }

                moduleDirs.add(
                    moduleUri
                )
            }
        } ?: throw IOException("Could not read module directory")
        return moduleDirs
    }

    /**
     * Adds a local module to the module folder
     * Module must be in zipped form (e.g .module)
     * @param uri: Uri - The uri of the module to add
     * @return Uri - The uri of the new module folder which was created
     * @throws IllegalArgumentException if the module already exists
     * @throws IllegalArgumentException if the uri is not valid
     * @throws IOException if the folder cannot be written (e.g permissions)
     */
    override suspend fun addModule(uri: Uri): Uri {
        val contentResolver = context.contentResolver
        val preferences = context.filepathDatastore.data.first()

        /**
         * The base module directory (CHOUTEN_ROOT_DIR/Modules/)
         */
        val baseModuleDirUri = getModuleDir(preferences.CHOUTEN_ROOT_DIR)

        /**
         * The name of the module (e.g Module 0, Module 1, etc)
         */
        val moduleName = "Module ${getModuleDirs().size}"

        /**
         * The module directory (not a child document uri)
         */
        val moduleDirDocument = DocumentsContract.createDocument(
            contentResolver, baseModuleDirUri, DocumentsContract.Document.MIME_TYPE_DIR, moduleName
        ) ?: throw IOException("Could not create module directory")

        /**
         * The working child document uri of the directory for the module
         */
        val moduleDirUri = DocumentsContract.buildChildDocumentsUriUsingTree(
            baseModuleDirUri, DocumentsContract.getDocumentId(moduleDirDocument)
        )

        // Unzip the module uri into the module directory
        withContext(Dispatchers.IO) {
            val inputStream = contentResolver.openInputStream(uri)
                ?: throw IllegalArgumentException("Invalid module uri")
            val zipInputStream = ZipInputStream(inputStream.buffered())

            // Unzip the module and place it in the module directory
            zipInputStream.use { zipInputStream ->
                while (true) {
                    val entry = zipInputStream.nextEntry ?: break

                    /**
                     * The MIME type of the entry. Uses plain/text if the
                     * type cannot be determined and the entry is not a directory.
                     */
                    val entryMimeType =
                        if (entry.isDirectory) DocumentsContract.Document.MIME_TYPE_DIR else MimeTypeMap.getSingleton()
                            ?.getExtensionFromMimeType(entry.name) ?: "plain/text"

                    /**
                     * Match unescaped slashes. For example,
                     * Videos\/Audio/MyVideo.mp4 will match
                     * Videos\/Audio and MyVideo.mp4
                     * Videos/Audio will match Videos and Audio
                     * separately
                     */
                    val unescapedSlashesRegex = Regex("(?<!\\\\)\\/")

                    // If the entry is not a directory, create the file
                    // and the parent directories
                    if (!entry.isDirectory) {
                        // If the amount of unescaped slashes is greater than 1, then the file is nested
                        val isNested = entry.name.split(unescapedSlashesRegex).count() > 1

                        // If nested, create the parent directories
                        val parentDir = if (isNested) {
                            val subdirs = entry.name.split(unescapedSlashesRegex).dropLast(1)
                            val document = contentResolver.query(
                                moduleDirUri, arrayOf(
                                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                                    DocumentsContract.Document.COLUMN_DISPLAY_NAME
                                ), null, null, null
                            ).use { cursor ->
                                // Go through each subdir.
                                // If the subdir does not exist, create it
                                // If the subdir does exist, get the document id
                                // of the subdir
                                var currentSubDir = moduleDirUri
                                for (subdir in subdirs) {
                                    var found = false
                                    while (cursor?.moveToNext() == true) {
                                        val displayNameIndex =
                                            cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                                        val documentIdIndex =
                                            cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                                        if (cursor.getString(displayNameIndex) == subdir) {
                                            currentSubDir =
                                                DocumentsContract.buildChildDocumentsUriUsingTree(
                                                    currentSubDir, cursor.getString(documentIdIndex)
                                                )
                                            found = true
                                            break
                                        }
                                    }
                                    if (!found) {
                                        currentSubDir = DocumentsContract.createDocument(
                                            contentResolver,
                                            currentSubDir,
                                            DocumentsContract.Document.MIME_TYPE_DIR,
                                            subdir
                                        ) ?: throw IOException("Could not create directory")
                                    }
                                }
                                cursor?.moveToFirst()
                                currentSubDir
                            } ?: throw IOException("Could not read module directory")

                            // Build a URI we can add children to
                            DocumentsContract.buildChildDocumentsUriUsingTree(
                                moduleDirUri, DocumentsContract.getDocumentId(document)
                            )
                        } else {
                            // The parent directory is the module directory
                            moduleDirUri
                        }

                        // Create the file
                        val document = DocumentsContract.createDocument(
                            contentResolver,
                            parentDir,
                            entryMimeType,
                            entry.name.split(unescapedSlashesRegex).last()
                        ) ?: throw IOException("Could not create file")

                        // Write the file
                        val outputStream = contentResolver.openOutputStream(document)
                            ?: throw IOException("Could not create file")
                        outputStream.buffered().use { outputStream ->
                            zipInputStream.copyTo(outputStream)
                        }
                        outputStream.close()
                    } else {
                        // Create the directory
                        DocumentsContract.createDocument(
                            contentResolver,
                            moduleDirUri,
                            DocumentsContract.Document.MIME_TYPE_DIR,
                            entry.name.removeSuffix("/")
                        )
                    }
                }
            }

            // Close all streams
            listOf(inputStream, zipInputStream).forEach { it.close() }

        }

        return moduleDirUri
    }

    /**
     * Removes a module from the module folder
     * @param uri: Uri - The uri of the module to remove
     * @throws IllegalArgumentException if the uri is not valid, or the module does not exist
     * within the module folder
     * @throws IOException if the folder cannot be deleted (e.g permissions)
     */
    override suspend fun removeModule(uri: Uri) {
        val moduleDirs = getModuleDirs()
        if (!moduleDirs.contains(uri)) {
            throw IllegalArgumentException("Module does not exist")
        }

        // Remove the suffixed /children from the uri
        val moduleDirUri = uri.toString().removeSuffix("/children").toUri()
        val moduleDocument = DocumentFile.fromTreeUri(context, moduleDirUri)
            ?: throw IllegalArgumentException("Invalid module uri")

        // Delete the module directory
        if (!moduleDocument.delete()) throw IOException("Could not delete module directory")
    }
}