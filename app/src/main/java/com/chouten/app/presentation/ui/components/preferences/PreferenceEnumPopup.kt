package com.chouten.app.presentation.ui.components.preferences

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.chouten.app.R
import com.chouten.app.common.UiText

/**
 * A preference item that displays a popup dialog with a list of enum values.
 * @param headlineContent The headline content of the preference item.
 * @param supportingContent The supporting text of the preference item.
 * @param leadingContent The leading content of the preference item.
 * @param trailingContent The trailing content of the preference item.
 * @param title The title of the popup dialog.
 * @param icon The icon of the popup dialog.
 * @param initial The initial value of the enum.
 * @param onSelectionChange The callback when the selection changes.
 * @param onSelectedConfirm The callback when the selection is confirmed.
 * @param transformLabel The label transformer (default is the enum value's [toString]).
 * @param T The enum type.
 * @see PreferenceEnumSelection
 */
@Composable
inline fun <reified T : Enum<T>> PreferenceEnumPopup(
    noinline headlineContent: @Composable () -> Unit,
    noinline supportingContent: @Composable (() -> Unit) = {},
    noinline leadingContent: @Composable (() -> Unit) = {},
    noinline trailingContent: @Composable (() -> Unit) = {},
    noinline title: @Composable (() -> Unit) = {},
    noinline icon: @Composable (() -> Unit) = {},
    initial: T,
    crossinline onSelectionChange: (T) -> Unit,
    crossinline onSelectedConfirm: (T) -> Unit,
    crossinline transformLabel: @Composable (T) -> Unit = { Text(it.toString()) }
) {
    val isPopupVisible = rememberSaveable {
        mutableStateOf(false)
    }

    var selected by rememberSaveable(initial) {
        mutableStateOf(initial)
    }

    ListItem(
        headlineContent = headlineContent,
        supportingContent = supportingContent,
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        modifier = Modifier.clickable {
            isPopupVisible.value = true
        }
    )

    AnimatedVisibility(visible = isPopupVisible.value, modifier = Modifier.fillMaxSize()) {
        AlertDialog(
            onDismissRequest = {
                isPopupVisible.value = false
            },
            confirmButton = {
                TextButton(onClick = {
                    isPopupVisible.value = false
                    onSelectedConfirm(selected)
                }) {
                    Text(UiText.StringRes(R.string.confirm).string())
                }
            }, title = title, icon = icon, text = {
                PreferenceEnumSelection(
                    initial = selected,
                    onSelectionChange = {
                        selected = it
                        onSelectionChange(it)
                    },
                    transformLabel = transformLabel
                )
            }
        )
    }
}