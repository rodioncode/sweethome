package com.jetbrains.kmpapp.di

import com.jetbrains.kmpapp.auth.ApiEnvelope
import com.jetbrains.kmpapp.auth.AuthApi
import com.jetbrains.kmpapp.auth.AuthRepository
import com.jetbrains.kmpapp.auth.AuthTokens
import com.jetbrains.kmpapp.auth.KtorAuthApi
import com.jetbrains.kmpapp.auth.RefreshRequest
import com.jetbrains.kmpapp.auth.TokenStorage
import com.jetbrains.kmpapp.auth.getApiBaseUrl
import com.jetbrains.kmpapp.data.groups.GroupsApi
import com.jetbrains.kmpapp.data.groups.GroupsRepository
import com.jetbrains.kmpapp.data.groups.KtorGroupsApi
import com.jetbrains.kmpapp.data.lists.KtorListsApi
import com.jetbrains.kmpapp.data.lists.ListsApi
import com.jetbrains.kmpapp.data.lists.ListsRepository
import com.jetbrains.kmpapp.data.sync.KtorSyncApi
import com.jetbrains.kmpapp.data.sync.SyncApi
import com.jetbrains.kmpapp.data.sync.SyncRepository
import com.jetbrains.kmpapp.auth.AuthViewModel
import com.jetbrains.kmpapp.screens.groups.GroupDetailViewModel
import com.jetbrains.kmpapp.screens.groups.GroupsViewModel
import com.jetbrains.kmpapp.screens.todo.TodoListDetailViewModel
import com.jetbrains.kmpapp.screens.todo.TodoListsViewModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.plugin
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val dataModule = module {

    // Клиент без авторизации — только для auth-эндпоинтов
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

    // Клиент с Bearer-токеном и авто-обновлением при 401
    single(named("apiClient")) {
        val json = Json { ignoreUnknownKeys = true }
        val tokenStorage = get<TokenStorage>()
        val authClient = get<HttpClient>(named("authClient"))
        val baseUrl = getApiBaseUrl()

        HttpClient {
            install(Logging) {
                logger = createHttpLogger()
                level = LogLevel.ALL
            }
            install(ContentNegotiation) {
                json(json, contentType = ContentType.Any)
            }
            // Добавляем свежий токен к каждому запросу
            defaultRequest {
                tokenStorage.getAccessToken()?.let { token ->
                    header("Authorization", "Bearer $token")
                }
            }
        }.also { client ->
            // Интерцептор 401: авто-обновление токена и повтор запроса
            client.plugin(HttpSend).intercept { request ->
                val originalCall = execute(request)
                if (originalCall.response.status == HttpStatusCode.Unauthorized) {
                    val refreshToken = tokenStorage.getRefreshToken()
                    if (refreshToken != null) {
                        try {
                            val envelope: ApiEnvelope<AuthTokens> = authClient
                                .post("$baseUrl/auth/refresh") {
                                    contentType(ContentType.Application.Json)
                                    setBody(RefreshRequest(refreshToken))
                                }.body()
                            if (envelope.error == null && envelope.data != null) {
                                val isGuest = tokenStorage.getIsGuest() ?: false
                                tokenStorage.saveTokens(envelope.data, isGuest)
                                // Повторяем исходный запрос — defaultRequest подхватит новый токен
                                execute(request)
                            } else {
                                // Refresh-токен невалиден — разлогиниваем
                                tokenStorage.clear()
                                originalCall
                            }
                        } catch (e: Exception) {
                            tokenStorage.clear()
                            originalCall
                        }
                    } else {
                        tokenStorage.clear()
                        originalCall
                    }
                } else {
                    originalCall
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
        )
    }

    single { GroupsRepository(get()) }

    single<SyncApi> {
        KtorSyncApi(
            apiClient = get(named("apiClient")),
            baseUrl = getApiBaseUrl(),
        )
    }

    single {
        val listsStorage = get<com.jetbrains.kmpapp.data.lists.ListsStorage>()
        SyncRepository(
            syncApi = get(),
            listsApi = get(),
            listsStorage = listsStorage,
            pendingDao = listsStorage.pendingOperationDao(),
            tokenStorage = get(),
        )
    }
}

val viewModelModule = module {
    factoryOf(::AuthViewModel)
    factoryOf(::TodoListsViewModel)
    factoryOf(::TodoListDetailViewModel)
    factoryOf(::GroupsViewModel)
    factory { (groupId: String) -> GroupDetailViewModel(groupId, get(), get()) }
}

expect fun platformModules(): List<org.koin.core.module.Module>

expect fun initKoin()
