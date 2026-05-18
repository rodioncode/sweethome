package com.jetbrains.kmpapp.di

import com.jetbrains.kmpapp.auth.createTokenStorage
import com.jetbrains.kmpapp.data.lists.ListsStorage
import com.jetbrains.kmpapp.data.lists.createListsStorage
import com.jetbrains.kmpapp.data.preferences.LocalPreferences
import com.jetbrains.kmpapp.data.preferences.createLocalPreferences
import com.jetbrains.kmpapp.push.PushTokenProvider
import com.jetbrains.kmpapp.push.createPushTokenProvider
import org.koin.dsl.module

val platformModule = module {
    single { createTokenStorage(null) }
    single<ListsStorage> { createListsStorage(null) }
    single<LocalPreferences> { createLocalPreferences(null) }
    single<PushTokenProvider> { createPushTokenProvider(null) }
}
