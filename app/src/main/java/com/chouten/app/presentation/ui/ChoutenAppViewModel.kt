package com.chouten.app.presentation.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chouten.app.domain.model.SnackbarModel
import com.chouten.app.domain.proto.filepathDatastore
import com.chouten.app.domain.use_case.module_use_cases.ModuleUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChoutenAppViewModel @Inject constructor(
    private val moduleUseCases: ModuleUseCases
) : ViewModel() {

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