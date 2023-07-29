package com.chouten.app.data.repository

import com.chouten.app.domain.model.SnackbarModel
import com.chouten.app.domain.repository.SnackbarRepository
import kotlinx.coroutines.channels.Channel

class SnackbarRepositoryImpl : SnackbarRepository {

    private val snackbarChannel = Channel<SnackbarModel>(Channel.BUFFERED)

    override suspend fun showSnackbar(message: SnackbarModel) {
        snackbarChannel.send(message)
    }

    override fun getSnackbar(): Channel<SnackbarModel> = snackbarChannel
}