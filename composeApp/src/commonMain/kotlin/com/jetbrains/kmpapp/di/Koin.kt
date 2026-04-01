package com.jetbrains.kmpapp.di

import com.jetbrains.kmpapp.auth.AuthApi
import com.jetbrains.kmpapp.auth.AuthRepository
import com.jetbrains.kmpapp.auth.KtorAuthApi
import com.jetbrains.kmpapp.auth.TokenStorage
import com.jetbrains.kmpapp.auth.getApiBaseUrl
import com.jetbrains.kmpapp.data.InMemoryMuseumStorage
import com.jetbrains.kmpapp.data.KtorMuseumApi
import com.jetbrains.kmpapp.data.MuseumApi
import com.jetbrains.kmpapp.data.MuseumRepository
import com.jetbrains.kmpapp.data.MuseumStorage
import com.jetbrains.kmpapp.data.groups.GroupsApi
import com.jetbrains.kmpapp.data.groups.GroupsRepository
import com.jetbrains.kmpapp.data.groups.KtorGroupsApi
import com.jetbrains.kmpapp.data.lists.KtorListsApi
import com.jetbrains.kmpapp.data.lists.ListsApi
import com.jetbrains.kmpapp.data.lists.ListsRepository
import com.jetbrains.kmpapp.auth.AuthViewModel
import com.jetbrains.kmpapp.screens.detail.DetailViewModel
import com.jetbrains.kmpapp.screens.groups.GroupDetailViewModel
import com.jetbrains.kmpapp.screens.groups.GroupsViewModel
import com.jetbrains.kmpapp.screens.list.ListViewModel
import com.jetbrains.kmpapp.screens.todo.TodoListDetailViewModel
import com.jetbrains.kmpapp.screens.todo.TodoListsViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val dataModule = module {
    single(named("authClient")) {
        val json = Json { ignoreUnknownKeys = true }
        HttpClient {
            install(Logging) {
                logger = createHttpLogger()
                level = LogLevel.ALL
            }
            install(ContentNegotiation) {
                json(json, contentType = ContentType.Any)
            }
        }
    }

    single(named("apiClient")) {
        val json = Json { ignoreUnknownKeys = true }
        val tokenStorage = get<TokenStorage>()
        HttpClient {
            install(Logging) {
                logger = createHttpLogger()
                level = LogLevel.ALL
                //sanitizeHeader { header -> header == HttpHeaders.Authorization }
            }
            install(ContentNegotiation) {
                json(json, contentType = ContentType.Any)
            }
            defaultRequest {
                tokenStorage.getAccessToken()?.let { token ->
                    header("Authorization", "Bearer $token")
                }
            }
        }
    }

    single<AuthApi> {
        KtorAuthApi(
            authClient = get(named("authClient")),
            apiClient = get(named("apiClient")),
            baseUrl = getApiBaseUrl(),
            tokenProvider = { get<TokenStorage>().getAccessToken() },
        )
    }

    single {
        AuthRepository(get(), get())
    }

    single<MuseumApi> { KtorMuseumApi(get(named("authClient"))) }
    single<MuseumStorage> { InMemoryMuseumStorage() }
    single {
        MuseumRepository(get(), get()).apply {
            initialize()
        }
    }

    single<ListsApi> {
        KtorListsApi(
            apiClient = get(named("apiClient")),
            baseUrl = getApiBaseUrl(),
            tokenProvider = { get<TokenStorage>().getAccessToken() },
        )
    }

    single { ListsRepository(get(), get()) }

    single<GroupsApi> {
        KtorGroupsApi(
            apiClient = get(named("apiClient")),
            baseUrl = getApiBaseUrl(),
            tokenProvider = { get<TokenStorage>().getAccessToken() },
        )
    }

    single { GroupsRepository(get()) }
}

val viewModelModule = module {
    factoryOf(::AuthViewModel)
    factoryOf(::ListViewModel)
    factoryOf(::DetailViewModel)
    factoryOf(::TodoListsViewModel)
    factoryOf(::TodoListDetailViewModel)
    factoryOf(::GroupsViewModel)
    factoryOf(::GroupDetailViewModel)
}

expect fun platformModules(): List<org.koin.core.module.Module>

expect fun initKoin()
