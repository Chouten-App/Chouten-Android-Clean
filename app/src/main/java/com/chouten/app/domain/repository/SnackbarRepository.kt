package com.chouten.app.domain.repository

import com.chouten.app.domain.model.SnackbarModel
import kotlinx.coroutines.channels.Channel

interface SnackbarRepository {
    suspend fun showSnackbar(message: SnackbarModel)
    fun getSnackbar(): Channel<SnackbarModel>
}