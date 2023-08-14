package com.chouten.app.presentation.ui

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import com.chouten.app.domain.model.SnackbarModel
import com.chouten.app.domain.proto.FilePreferences
import com.chouten.app.domain.proto.filepathDatastore
import com.chouten.app.domain.repository.SnackbarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class ChoutenAppViewModel @Inject constructor(
    private val snackbarRepository: SnackbarRepository
) : ViewModel() {
    val snackbarChannel = snackbarRepository.getSnackbar().receiveAsFlow()

    /**
     * Show a snackbar with the given [message].
     * The snackbar will NOT take precedence over any other snackbar currently being shown.
     * @param message The message to be shown in the snackbar.
     */
    suspend fun showSnackbar(message: SnackbarModel) {
        snackbarRepository.showSnackbar(message)
    }

    suspend fun setModuleDirectory(context: Context, directory: Uri? = null) {
        context.filepathDatastore.updateData { preferences ->
            preferences.copy(
                CHOUTEN_ROOT_DIR = directory ?: if (Build.VERSION.SDK_INT >= 29) {
                    context.getExternalFilesDir(null)?.toUri()
                        ?: FilePreferences.DEFAULT.CHOUTEN_ROOT_DIR
                } else {
                    FilePreferences.DEFAULT.CHOUTEN_ROOT_DIR
                }, IS_CHOUTEN_MODULE_DIR_SET = true
            )
        }
    }
}