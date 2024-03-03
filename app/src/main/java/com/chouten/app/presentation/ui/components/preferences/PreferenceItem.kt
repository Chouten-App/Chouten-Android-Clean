package com.chouten.app.presentation.ui.components.preferences

import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PreferenceItem(
    modifier: Modifier = Modifier,
    headlineContent: @Composable () -> Unit,
    supportingContent: @Composable (() -> Unit) = {},
    icon: @Composable (() -> Unit) = {},
    callback: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .clickable {
                callback()
            }
            .then(modifier),
        headlineContent = headlineContent,
        supportingContent = supportingContent,
        leadingContent = icon
    )
}