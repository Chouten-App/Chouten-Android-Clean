package com.chouten.app.domain.model

import androidx.compose.ui.graphics.vector.ImageVector

data class AlertDialogModel(
    val icon: ImageVector? = null,
    val title: String,
    val message: String,
    val positiveButton: Pair<String, () -> Unit>,
    val negativeButton: Pair<String, () -> Unit>? = null,
)
