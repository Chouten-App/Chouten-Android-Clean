package com.chouten.app.common

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed class UiText {
    class Literal(val string: String) : UiText()
    class StringRes(@androidx.annotation.StringRes val stringRes: Int, vararg val args: Any) :
        UiText()

    @Composable
    fun string(): String {
        return when (this) {
            is Literal -> this.string
            is StringRes -> stringResource(this.stringRes, *this.args)
        }
    }

    fun string(context: Context): String {
        return when (this) {
            is Literal -> this.string
            is StringRes -> context.getString(this.stringRes, *this.args)
        }
    }
}