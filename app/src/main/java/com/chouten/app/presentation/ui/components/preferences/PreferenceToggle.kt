package com.chouten.app.presentation.ui.components.preferences

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.unit.dp

/**
 * A preference that can be toggled on or off.
 * @param headlineContent The content of the headline.
 * @param supportingContent The content of the supporting text.
 * @param icon The icon to display.
 * @param onToggle The callback to invoke when the toggle is toggled.
 * @param initial The initial state of the toggle.
 * @param constraint The constraint to apply to the toggle. The toggle is enabled if the constraint is met.
 * Defaults to always enabled.
 */
@Composable
fun PreferenceToggle(
    headlineContent: @Composable () -> Unit,
    supportingContent: @Composable (() -> Unit) = {},
    icon: @Composable (() -> Unit) = {},
    onToggle: (Boolean) -> Unit,
    initial: Boolean = false,
    constraint: (() -> Boolean) = { true },
) {

    val isToggled = rememberSaveable(initial) {
        mutableStateOf(initial)
    }

    val isConstrained by remember {
        derivedStateOf {
            constraint()
        }
    }

    val surfaceColor = MaterialTheme.colorScheme.surface

    ListItem(
        modifier = Modifier
            .clickable(
                enabled = isConstrained,
            ) {
                isToggled.value = !isToggled.value
                onToggle(isToggled.value)
            }
            .then(
                if (!isConstrained) {
                    Modifier.drawWithContent {
                        // Draw a semi-transparent layer on top of the content
                        drawContent()
                        drawRect(
                            color = surfaceColor.copy(alpha = 0.2f)
                        )
                    }
                } else {
                    Modifier
                }
            ),
        headlineContent = headlineContent,
        supportingContent = supportingContent,
        trailingContent = {
            Row(
                horizontalArrangement = Arrangement.End
            ) {
                Spacer(
                    modifier = Modifier.width(32.dp),
                )
                Switch(
                    checked = isToggled.value,
                    onCheckedChange = {
                        if (isConstrained) {
                            isToggled.value = it
                            onToggle(it)
                        }
                    },
                    enabled = isConstrained,
                )

            }
        },
        leadingContent = icon
    )
}