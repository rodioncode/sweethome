package com.jetbrains.kmpapp.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module

actual fun platformModules(): List<Module> = listOf(platformModule)

actual fun initKoin() {
    startKoin {
        modules(platformModules() + dataModule + viewModelModule)
    }
}
