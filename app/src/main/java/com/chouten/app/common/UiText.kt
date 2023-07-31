package com.chouten.app.common

import android.content.Context
import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class UiText : Parcelable {
    class Literal(val string: String) : UiText()
    class StringRes(@androidx.annotation.StringRes val stringRes: Int) :
        UiText()

    @Composable
    fun string(): String {
        return when (this) {
            is Literal -> this.string
            is StringRes -> stringResource(this.stringRes)
        }
    }

    fun string(context: Context): String {
        return when (this) {
            is Literal -> this.string
            is StringRes -> context.getString(this.stringRes)
        }
    }
}