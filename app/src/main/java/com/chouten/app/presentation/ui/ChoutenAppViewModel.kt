package com.chouten.app.presentation.ui

import androidx.lifecycle.ViewModel
import com.chouten.app.domain.model.SnackbarModel
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
}