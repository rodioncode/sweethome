package com.jetbrains.kmpapp.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module

actual fun platformModules(): List<Module> = listOf(platformModule)

actual fun initKoin() {
    startKoin {
        androidContext(com.jetbrains.kmpapp.SweetHomeApp.appContext)
        modules(platformModules() + dataModule + viewModelModule)
    }
}
