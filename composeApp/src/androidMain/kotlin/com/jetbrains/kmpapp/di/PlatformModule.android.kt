package com.jetbrains.kmpapp.di

import android.content.Context
import com.jetbrains.kmpapp.auth.createTokenStorage
import com.jetbrains.kmpapp.data.lists.ListsStorage
import com.jetbrains.kmpapp.data.lists.createListsStorage
import com.jetbrains.kmpapp.push.PushTokenProvider
import com.jetbrains.kmpapp.push.createPushTokenProvider
import org.koin.dsl.module

val platformModule = module {
    single { createTokenStorage(get<Context>()) }
    single<ListsStorage> { createListsStorage(get<Context>()) }
    single<PushTokenProvider> { createPushTokenProvider(get<Context>()) }
}
