package com.jetbrains.kmpapp.di

import com.jetbrains.kmpapp.auth.createTokenStorage
import org.koin.dsl.module

val platformModule = module {
    single { createTokenStorage(null) }
}
