package com.jetbrains.kmpapp

import android.app.Application
import com.jetbrains.kmpapp.di.initKoin

class SweetHomeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        initKoin()
    }

    companion object {
        lateinit var appContext: android.content.Context
            private set
    }
}
