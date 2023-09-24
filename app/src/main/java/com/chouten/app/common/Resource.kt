package com.chouten.app.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
sealed class Resource<T>(open val data: @RawValue T? = null, open val message: String? = null) :
    Parcelable {
    class Success<T>(override val data: @RawValue T) : Resource<T>(data)
    class Error<T>(override val message: String, override val data: @RawValue T? = null) :
        Resource<T>(data, message)

    class Loading<T>(override val data: @RawValue T? = null) : Resource<T>(data)
    class Uninitialized<T>(override val data: @RawValue T? = null) : Resource<T>(data)
}