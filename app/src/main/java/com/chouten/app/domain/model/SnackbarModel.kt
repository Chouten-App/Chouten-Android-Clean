package com.chouten.app.domain.model

import android.os.Parcelable
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import kotlinx.parcelize.Parcelize

@Parcelize
data class SnackbarModel(
    val isError: Boolean = false,
    val customButton: SnackbarButton? = null,
    override val actionLabel: String = customButton?.label ?: if (isError) "Error" else "OK",
    override val duration: SnackbarDuration = if (customButton != null) SnackbarDuration.Indefinite else SnackbarDuration.Short,
    override val message: String,
    override val withDismissAction: Boolean = true,
) : SnackbarVisuals, Parcelable {
    @Parcelize
    data class SnackbarButton(
        val label: String,
        val onClick: () -> Unit,
    ) : Parcelable

    companion object {
        val None = SnackbarModel(message = "")
    }
}