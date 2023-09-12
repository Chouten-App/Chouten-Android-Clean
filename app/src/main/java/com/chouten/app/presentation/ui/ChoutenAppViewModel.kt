package com.chouten.app.presentation.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chouten.app.domain.model.ModuleModel
import com.chouten.app.domain.model.SnackbarModel
import com.chouten.app.domain.proto.filepathDatastore
import com.chouten.app.domain.proto.moduleDatastore
import com.chouten.app.domain.use_case.module_use_cases.ModuleUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChoutenAppViewModel @Inject constructor(
    private val application: Application,
    private val moduleUseCases: ModuleUseCases
) : ViewModel() {

    private val _modules = MutableStateFlow<List<ModuleModel>>(emptyList())

    /**
     * The list of modules currently installed in the app.
     * [getModules] must be called before using this flow as other calls
     * may only filter the flow rather than scanning the module dir and emitting
     * all modules again.
     * Calls to [installModule] and [removeModule] will update this list.
     * @see [getModules]
     * @see [installModule]
     * @see [removeModule]
     */
    val modules: StateFlow<List<ModuleModel>> = _modules

    /**
     * Emits the list of modules currently installed in the app.
     * @see [modules]
     */
    suspend fun getModules() {
        _modules.emit(moduleUseCases.getModuleUris())
    }

    /**
     * Set the Module Directory preference to the given [directory].
     * @param context The context to use to access the filepathDatastore.
     * @param directory The content uri of the directory to set as the module directory.
     * expects directory to be a valid document uri.
     */
    suspend fun setModuleDirectory(context: Context, directory: Uri) {
        context.filepathDatastore.updateData { preferences ->
            preferences.copy(
                CHOUTEN_ROOT_DIR = directory, IS_CHOUTEN_MODULE_DIR_SET = true
            )
        }
    }

    /**
     * Add a module to the app.
     * @param uri The content uri of the module to add.
     * expects uri to be a valid document uri.
     */
    suspend fun installModule(uri: Uri, showSnackbar: (SnackbarModel) -> Unit) {
        try {
            moduleUseCases.addModule(uri)
            _modules.emit(moduleUseCases.getModuleUris())
        } catch (e: Exception) {
            showSnackbar(
                SnackbarModel(
                    message = e.message ?: "Unknown error", actionLabel = "Dismiss", isError = true
                )
            )
        }
    }

    /**
     * Remove module from the app and delete the module folder.
     * Completely re-emits the list of modules by re-scanning the module directory.
     * @param uri The content uri of the module to remove.
     * expects uri to be a valid document uri.
     * @param showSnackbar A lambda to show a snackbar with the given [SnackbarModel].
     */
    suspend fun removeModule(uri: Uri, showSnackbar: (SnackbarModel) -> Unit) {
        try {
            moduleUseCases.removeModule(uri)
            _modules.emit(moduleUseCases.getModuleUris())
            // TODO: Update the module preferences to remove the module from the
            // auto-update list (if it is in the list) and make sure it is
            // not the current module.
        } catch (e: Exception) {
            showSnackbar(
                SnackbarModel(
                    message = e.message ?: "Unknown error", actionLabel = "Dismiss", isError = true
                )
            )
        }
    }

    /**
     * Remove module from the app and delete the module folder.
     * Does not re-emit the list of modules; filters the current list of modules to remove
     * the given [ModuleModel].
     * @param model The [ModuleModel] of the module to remove.
     * @param showSnackbar A lambda to show a snackbar with the given [SnackbarModel].
     * @see [removeModule]
     */
    suspend fun removeModule(model: ModuleModel, showSnackbar: (SnackbarModel) -> Unit) {
        try {
            moduleUseCases.removeModule(model)
            _modules.emit(modules.value.filter { it != model })

            // Update the preferences to remove the module from the auto-update list
            // and make sure it is not set as the current module.
            application.baseContext.moduleDatastore.data.first().let {
                if (it.selectedModuleId == model.id) {
                    application.baseContext.moduleDatastore.updateData { preferences ->
                        preferences.copy(selectedModuleId = "")
                    }
                }
                if (it.autoUpdatingModules.contains(model.id)) {
                    application.baseContext.moduleDatastore.updateData { preferences ->
                        preferences.copy(
                            autoUpdatingModules = preferences.autoUpdatingModules - model.id
                        )
                    }
                }
            }
        } catch (e: Exception) {
            showSnackbar(
                SnackbarModel(
                    message = e.message ?: "Unknown error", actionLabel = "Dismiss", isError = true
                )
            )
        }
    }

    /**
     * Asynchronously run the given [block] using the viewModelScope.
     * @param block The block to run asynchronously.
     */
    fun runAsync(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}