package com.chouten.app

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import org.acra.BuildConfig
import org.acra.data.StringFormat
import org.acra.ktx.initAcra

@HiltAndroidApp
class ChoutenApplication : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        initAcra {
            buildConfigClass = BuildConfig::class.java
            reportFormat = StringFormat.JSON
        }
    }
}