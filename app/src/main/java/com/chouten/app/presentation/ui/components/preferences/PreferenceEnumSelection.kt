package com.chouten.app.presentation.ui.components.preferences

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A preference that allows the user to select an enum value.
 * @param initial The initial value of the preference (can change over time).
 * @param onSelectionChange The callback when the selection changes.
 * @param transformLabel The label transformer. Default is the enum value's [toString].
 * * For a popup version of this preference, see [PreferenceEnumPopup].
 * @see PreferenceEnumPopup
 */
@Composable
inline fun <reified T : Enum<T>> PreferenceEnumSelection(
    initial: T,
    crossinline onSelectionChange: (T) -> Unit,
    crossinline transformLabel: @Composable (T) -> Unit = { Text(it.toString()) }
) {
    var selected = initial
    Column {
        enumValues<T>().forEach {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable {
                        selected = it
                        onSelectionChange(it)
                    }, verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = it == selected, onClick = {
                    selected = it
                    onSelectionChange(it)
                })
                Spacer(Modifier.width(8.dp))
                transformLabel(it)
            }
        }
    }
}