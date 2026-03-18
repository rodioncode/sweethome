package com.jetbrains.kmpapp.di

import android.content.Context
import com.jetbrains.kmpapp.auth.createTokenStorage
import com.jetbrains.kmpapp.data.lists.ListsStorage
import com.jetbrains.kmpapp.data.lists.createListsStorage
import org.koin.dsl.module

val platformModule = module {
    single { createTokenStorage(get<Context>()) }
    single<ListsStorage> { createListsStorage(get<Context>()) }
}
