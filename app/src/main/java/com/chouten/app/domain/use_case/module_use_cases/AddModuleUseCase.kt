package com.chouten.app.domain.use_case.module_use_cases

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.core.content.FileProvider
import com.chouten.app.common.OutOfDateAppException
import com.chouten.app.common.OutOfDateModuleException
import com.chouten.app.common.findDocument
import com.chouten.app.domain.model.ModuleModel
import com.chouten.app.domain.proto.filepathDatastore
import com.chouten.app.domain.repository.ModuleRepository
import com.lagradost.nicehttp.Requests
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import okio.buffer
import okio.sink
import okio.source
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.UUID
import java.util.zip.ZipInputStream
import javax.inject.Inject

sealed class ModuleInstallEvent() {
    data object DOWNLOADING : ModuleInstallEvent()
    data class DOWNLOADED(val moduleFile: File) : ModuleInstallEvent()
    data class CACHING(val moduleFile: Uri) : ModuleInstallEvent()
    data class CACHED(val moduleFile: Uri) : ModuleInstallEvent()
    data class PARSING(val moduleFile: Uri) : ModuleInstallEvent()
    data class PARSED(val module: ModuleModel) : ModuleInstallEvent()
    data class INSTALLED(val module: ModuleModel) : ModuleInstallEvent()
}

class AddModuleUseCase @Inject constructor(
    private val mContext: Context,
    private val moduleRepository: ModuleRepository,
    private val httpClient: Requests,
    private val log: suspend (String) -> Unit,
    private val jsonParser: suspend (String) -> ModuleModel
) {

    enum class ModuleDirectories {
        HOME, SEARCH, INFO, MEDIA,
    }

    /**
     * Adds a module to the module folder
     * @param uri The URI of the module (either a local file or a remote resource)
     * @param callback The callback that is called during each stage of the module installation.
     * True: Cancellation (with the exception of the INSTALLED event)
     * False: Continuation
     * @throws IOException if the module cannot be downloaded or added (e.g duplicate/unsupported version)
     * @throws FileNotFoundException If the module is missing files (no metadata.json)
     * @throws IllegalArgumentException if the URI is invalid. Not a valid module (e.g not a zip)
     */
    suspend operator fun invoke(uri: Uri, callback: (ModuleInstallEvent) -> Boolean) =
        withContext(Dispatchers.IO) {
            val contentResolver = mContext.contentResolver

            /**
             * Throw an exception safely, deleting the file if it exists
             */
            val safeException: suspend (Exception, File?) -> Nothing = { it, toDelete ->
                log("Error adding module: ${it.message}")
                toDelete?.delete()
                throw it
            }

            // If the URI points to a remote resource, we must first download it
            val isRemote = uri.scheme in setOf("http", "https")

            /**
             * The cached .module file and the URI of the .module file which we will pass on to the module repository
             * The cached module is null unless the module was downloaded from a remote location.
             */
            val (cachedModule, newUri) = if (isRemote) {
                if (callback(ModuleInstallEvent.DOWNLOADING)) return@withContext
                log("Downloading remote module $uri")

                val byteStream = httpClient.get(
                    uri.toString()
                ).body.byteStream()

                // Save the module to the module folder
                val moduleFile = mContext.cacheDir?.resolve(
                    uri.lastPathSegment ?:
                    // If the module does not have a name, we will generate a random one
                    UUID.randomUUID().toString()
                ) ?: throw IOException("Could not create module directory")

                // Write the bytes from the remote resource to the module file
                moduleFile.outputStream().use { outputStream ->
                    byteStream.use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                if (callback(ModuleInstallEvent.DOWNLOADED(moduleFile))) return@withContext
                log("Downloaded module to $moduleFile")
                (moduleFile to FileProvider.getUriForFile(
                    mContext, "${mContext.packageName}.provider", moduleFile
                ))
            } else null to uri

            // Parse the module and test if it is valid
            if (callback(ModuleInstallEvent.PARSING(newUri))) safeException(
                InterruptedException("Cancelled via Callback"), cachedModule
            )
            log("Parsing module $newUri")

            // Unzip the module uri into the module directory
            val inputStream = contentResolver.openInputStream(newUri)
                ?: throw IllegalArgumentException("Invalid module uri")
            val zipInputStream = ZipInputStream(inputStream.buffered())

            val destinationDir =
                mContext.cacheDir.resolve(UUID.randomUUID().toString() + "-module/").also {
                    it.mkdirs()
                }

            // Unzip the module and place it in the cache directory
            zipInputStream.use { zipInputStream ->
                while (true) {
                    val entry = zipInputStream.nextEntry ?: break

                    // If the entry is not a directory, create the file
                    // and the parent directories
                    if (!entry.isDirectory) {
                        // Create the file
                        val document = File(
                            destinationDir, entry.name
                        ).also {
                            it.parentFile?.mkdirs()
                        }

                        // Write the file
                        val outputStream = document.outputStream()
                        outputStream.buffered().use { outputStream ->
                            zipInputStream.copyTo(outputStream)
                        }
                        outputStream.close()
                    } else {
                        // Create the directory
                        destinationDir.resolve(entry.name)
                    }
                }
            }

            // Get the metadata file
            val metadataInputStream = destinationDir.resolve("metadata.json").also {
                if (!it.exists()) safeException(
                    FileNotFoundException("metadata.json does not exist!"), destinationDir
                )
            }.inputStream()

            /**
             * The parsed module
             */
            var module = try {
                metadataInputStream.use {
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
            } catch (e: Exception) {
                e.printStackTrace()
                safeException(
                    IllegalArgumentException("Could not parse module", e), destinationDir
                )
            }

            if (callback(ModuleInstallEvent.PARSED(module))) {
                Log.d("Chouten", "Cancelled via Callback")
                safeException(
                    InterruptedException("Cancelled via Callback"), destinationDir
                )
            } else {
                Log.d("Chouten", "Allowed to continue")
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
                    destinationDir
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
                    destinationDir
                )
            }

            // Compare the module with the existing modules
            // If the module already exists, we must check for updates
            // Compare the module with the existing modules
            moduleRepository.getModules().firstOrNull()?.forEach { installed ->
                log("Comparing module ${module.id} (${module.version}) with ${installed.id} (${installed.version})")
                // Check if the module already exists
                if (module.id == installed.id) {
                    if (module.version > installed.version) { // new module version > old module version
                        moduleRepository.updateModule(module)
                        log("Updated module ${module.name} (${module.id})")
                    }
                    if (module.version == installed.version) { // new module version == old module version
                        safeException(
                            IllegalArgumentException("Module ${module.name} (${module.id}) already exists"),
                            destinationDir
                        )
                    } else if (module.version < installed.version) { // new module version < old module version
                        safeException(
                            IllegalArgumentException("Module ${module.name} (${module.id}) is older than the existing module"),
                            destinationDir
                        )
                    }
                }
            }

            val dirFiles = destinationDir.listFiles()
                ?: safeException(IOException("Could not list destination files."), destinationDir)

            ModuleDirectories.entries.forEach { dir ->
                dirFiles.find { it.name.toUpperCase(Locale.current) == dir.name }?.let {
                    return@let it.resolve("code.js").let code@{ code ->
                        log("Adding code for ${code.parentFile?.name}.")
                        val moduleCode = module.code ?: ModuleModel.ModuleCode()
                        when (dir) {
                            ModuleDirectories.HOME -> {
                                module = module.copy(
                                    code = moduleCode.copy(
                                        home = listOf(
                                            ModuleModel.ModuleCode.ModuleCodeblock(
                                                code = code.readLines().joinToString("\n")
                                            )
                                        )
                                    )
                                )
                            }

                            ModuleDirectories.SEARCH -> {
                                module = module.copy(
                                    code = moduleCode.copy(
                                        search = listOf(
                                            ModuleModel.ModuleCode.ModuleCodeblock(
                                                code = code.readLines().joinToString("\n")
                                            )
                                        )
                                    )
                                )
                            }

                            ModuleDirectories.INFO -> {
                                module = module.copy(
                                    code = moduleCode.copy(
                                        info = listOf(
                                            ModuleModel.ModuleCode.ModuleCodeblock(
                                                code = code.readLines().joinToString("\n")
                                            )
                                        )
                                    )
                                )
                            }

                            ModuleDirectories.MEDIA -> {
                                module = module.copy(
                                    code = module.code?.copy(
                                        mediaConsume = listOf(
                                            ModuleModel.ModuleCode.ModuleCodeblock(
                                                code = code.readLines().joinToString("\n")
                                            )
                                        )
                                    )
                                )
                            }
                        }
                    }
                } ?: log("${module.name} does not contain code for $dir")
            }

            destinationDir.resolve("icon.png").apply {
                if (!exists()) {
                    log("${module.name} does not contain an icon in PNG format")
                    return@apply
                }

                val os = ByteArrayOutputStream()
                BitmapFactory.decodeFile(this@apply.absolutePath)?.apply {
                    compress(Bitmap.CompressFormat.PNG, 80, os)
                } ?: log("Could not parse icon.png")
                module = module.copy(metadata = module.metadata.copy(icon = os.toByteArray()))
            }

            val preferences = mContext.filepathDatastore.data.firstOrNull()
            preferences?.CHOUTEN_ROOT_DIR?.let {
                if (it == Uri.EMPTY) {
                    log("CHOUTEN_ROOT_DIR is empty. Cannot copy module artifact")
                    return@let
                } else if (!preferences.SAVE_MODULE_ARTIFACTS) {
                    log("SAVE_MODULE_ARTIFACTS is false. Not saving module artifact")
                    return@let
                }

                val childDocumentsUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                    it, DocumentsContract.getTreeDocumentId(
                        DocumentsContract.buildChildDocumentsUriUsingTree(
                            it, DocumentsContract.getTreeDocumentId(it)
                        ).findDocument(contentResolver, "Modules") ?: safeException(
                            IOException(
                                "Could not find Module Dir at $it"
                            ), destinationDir
                        )
                    )
                )

                val displayName = "${module.name}_v${module.version}_${module.id}.module"
                log("Adding artifact $displayName to $childDocumentsUri")
                childDocumentsUri.findDocument(contentResolver, displayName)?.let { _ ->
                    log("Existing artifact exists within $childDocumentsUri")
                } ?: run {
                    val moduleUri = DocumentsContract.createDocument(
                        contentResolver, childDocumentsUri, "application/octet-stream", displayName
                    ) ?: throw IOException("Could not save module artifact")
                    val stream = if (isRemote) {
                        cachedModule?.inputStream()
                    } else contentResolver.openInputStream(uri)
                    stream?.apply {
                        contentResolver.openOutputStream(moduleUri)?.sink()?.buffer()
                            ?.use { buffer ->
                                buffer.writeAll(source())
                            }
                        close()
                    }
                }
            }

            log("Adding Module $module")
            moduleRepository.addModule(module).also {
                callback(ModuleInstallEvent.INSTALLED(module))
            }
            destinationDir.delete()
        }
}