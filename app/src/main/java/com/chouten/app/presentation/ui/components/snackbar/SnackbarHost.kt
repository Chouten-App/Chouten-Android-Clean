package com.chouten.app.presentation.ui.components.snackbar

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.chouten.app.domain.model.SnackbarModel

@Composable
fun SnackbarHost(
    snackbarHostState: SnackbarHostState
) {
    androidx.compose.material3.SnackbarHost(hostState = snackbarHostState) { data ->
        val extendedVisuals = data.visuals as? SnackbarModel
        val isError = extendedVisuals?.isError ?: false
        val buttonColor = if (isError) {
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            )
        } else {
            ButtonDefaults.buttonColors(
                containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.primary
            )
        }

        Snackbar(modifier = Modifier.padding(12.dp), containerColor = if (!isError) {
            MaterialTheme.colorScheme.surfaceColorAtElevation(
                10.dp
            )
        } else {
            MaterialTheme.colorScheme.error
        }, contentColor = if (!isError) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onError
        }, action = {
            TextButton(
                onClick = {
                    extendedVisuals?.customButton?.onClick?.invoke().also { data.dismiss() }
                        ?: if (isError) data.dismiss() else data.performAction()
                }, shape = MaterialTheme.shapes.extraSmall, colors = buttonColor
            ) {
                extendedVisuals?.customButton?.label?.let {
                    Text(
                        it
                    )
                } ?: extendedVisuals?.actionLabel?.let {
                    Text(
                        it
                    )
                } ?: Icon(Icons.Default.Close, "Dismiss")
            }
        }) {
            Text(
                text = data.visuals.message, maxLines = 8
            )
        }
    }
}