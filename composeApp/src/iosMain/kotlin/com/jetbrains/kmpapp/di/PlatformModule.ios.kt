package com.jetbrains.kmpapp.di

import com.jetbrains.kmpapp.auth.createTokenStorage
import com.jetbrains.kmpapp.data.lists.ListsStorage
import com.jetbrains.kmpapp.data.lists.createListsStorage
import org.koin.dsl.module

val platformModule = module {
    single { createTokenStorage(null) }
    single<ListsStorage> { createListsStorage(null) }
}
