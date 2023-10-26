package com.chouten.app.domain.use_case.module_use_cases

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import com.chouten.app.common.ModuleFolderNotFoundException
import java.io.IOException
import javax.inject.Inject

/**
 * Returns the module directory
 * @param baseUri: Uri - The base uri to search for the module directory (e.g CHOUTEN_ROOT_DIR)
 * @return Uri - The uri of the module directory
 * @throws ModuleFolderNotFoundException - If the module directory could not be found
 * @throws IOException - If the module directory could not be created
 * @throws SecurityException - If the app does not have permission to access the module directory
 * @throws IllegalArgumentException - If the baseUri is invalid
 */
class GetModuleDirUseCase @Inject constructor(
    private val context: Context
) {
    operator fun invoke(baseUri: Uri): Uri {
        val contentResolver = context.contentResolver

        // Get the URI which we can query with
        val docUri = if (DocumentFile.isDocumentUri(context, baseUri)) {
            baseUri
        } else {
            DocumentsContract.buildChildDocumentsUriUsingTree(
                baseUri, DocumentsContract.getTreeDocumentId(baseUri)
            )
        } ?: throw IllegalArgumentException("Invalid URI")

        return contentResolver.query(
            docUri,
            arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME
            ),
            null, // Using a selection of "${DocumentsContract.Document.COLUMN_DISPLAY_NAME} = ?" and a selectionArgs of "Modules" does not work
            // for some reason
            null,
            null
        )?.use { cursor ->
            val displayNameIndex =
                cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            // Keep iterating over the rows until we find the Modules directory (or don't)
            while (cursor.moveToNext()) {
                if (cursor.getString(displayNameIndex) == "Modules") {
                    return@use DocumentsContract.buildChildDocumentsUriUsingTree(
                        docUri, cursor.getString(0)
                    )
                }
            }

            // We have not found the Modules directory, so we need to create it
            val _moduleDirUri = DocumentsContract.createDocument(
                contentResolver, docUri, DocumentsContract.Document.MIME_TYPE_DIR, "Modules"
            ) ?: throw IOException("Could not create module directory")
            DocumentsContract.buildChildDocumentsUriUsingTree(
                docUri, DocumentsContract.getTreeDocumentId(_moduleDirUri)
            )
        } ?: throw ModuleFolderNotFoundException()
    }
}