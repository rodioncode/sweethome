package com.jetbrains.kmpapp.di

import com.jetbrains.kmpapp.auth.AuthApi
import com.jetbrains.kmpapp.auth.AuthRepository
import com.jetbrains.kmpapp.auth.KtorAuthApi
import com.jetbrains.kmpapp.auth.TokenStorage
import com.jetbrains.kmpapp.auth.getApiBaseUrl
import com.jetbrains.kmpapp.data.sync.KtorSyncApi
import com.jetbrains.kmpapp.data.sync.SyncApi
import com.jetbrains.kmpapp.data.sync.SyncRepository
import com.jetbrains.kmpapp.data.categories.CategoriesApi
import com.jetbrains.kmpapp.data.categories.CategoriesRepository
import com.jetbrains.kmpapp.data.categories.KtorCategoriesApi
import com.jetbrains.kmpapp.data.groups.GroupsApi
import com.jetbrains.kmpapp.data.groups.GroupsRepository
import com.jetbrains.kmpapp.data.groups.KtorGroupsApi
import com.jetbrains.kmpapp.data.lists.KtorListsApi
import com.jetbrains.kmpapp.data.lists.ListsApi
import com.jetbrains.kmpapp.data.lists.ListsRepository
import com.jetbrains.kmpapp.data.suggestions.KtorSuggestionsApi
import com.jetbrains.kmpapp.data.suggestions.SuggestionsApi
import com.jetbrains.kmpapp.data.suggestions.SuggestionsRepository
import com.jetbrains.kmpapp.data.profile.KtorProfileApi
import com.jetbrains.kmpapp.data.profile.ProfileApi
import com.jetbrains.kmpapp.data.notifications.KtorNotificationsApi
import com.jetbrains.kmpapp.data.notifications.NotificationsApi
import com.jetbrains.kmpapp.screens.notifications.NotificationsViewModel
import com.jetbrains.kmpapp.screens.settings.NotificationPrefsViewModel
import com.jetbrains.kmpapp.screens.calendar.CalendarViewModel
import com.jetbrains.kmpapp.data.chat.ChatApi
import com.jetbrains.kmpapp.data.chat.KtorChatApi
import com.jetbrains.kmpapp.data.devices.DeviceApi
import com.jetbrains.kmpapp.data.devices.KtorDeviceApi
import com.jetbrains.kmpapp.data.attachments.AttachmentsApi
import com.jetbrains.kmpapp.data.attachments.AttachmentsRepository
import com.jetbrains.kmpapp.data.attachments.KtorAttachmentsApi
import com.jetbrains.kmpapp.data.wishlist.KtorPublicWishlistApi
import com.jetbrains.kmpapp.data.wishlist.PublicWishlistApi
import com.jetbrains.kmpapp.data.calendar.CalendarSharingApi
import com.jetbrains.kmpapp.data.calendar.KtorCalendarSharingApi
import com.jetbrains.kmpapp.data.gamification.GamificationApi
import com.jetbrains.kmpapp.data.gamification.GamificationRepository
import com.jetbrains.kmpapp.data.gamification.KtorGamificationApi
import com.jetbrains.kmpapp.data.achievements.AchievementsApi
import com.jetbrains.kmpapp.data.achievements.AchievementsRepository
import com.jetbrains.kmpapp.data.achievements.KtorAchievementsApi
import com.jetbrains.kmpapp.data.goals.GoalsApi
import com.jetbrains.kmpapp.data.goals.GoalsRepository
import com.jetbrains.kmpapp.data.goals.KtorGoalsApi
import com.jetbrains.kmpapp.screens.family.GamificationViewModel
import com.jetbrains.kmpapp.screens.goals.GoalDetailViewModel
import com.jetbrains.kmpapp.screens.goals.GoalsViewModel
import com.jetbrains.kmpapp.screens.wishlist.PublicWishlistViewModel
import com.jetbrains.kmpapp.push.DeviceRegistrar
import com.jetbrains.kmpapp.push.PushTokenProvider
import com.jetbrains.kmpapp.screens.chat.ChatViewModel
import com.jetbrains.kmpapp.auth.AuthViewModel
import com.jetbrains.kmpapp.screens.groups.GroupDetailViewModel
import com.jetbrains.kmpapp.screens.groups.GroupsViewModel
import com.jetbrains.kmpapp.screens.family.FamilyViewModel
import com.jetbrains.kmpapp.screens.home.HomeViewModel
import com.jetbrains.kmpapp.screens.profile.ProfileViewModel
import com.jetbrains.kmpapp.screens.templates.TemplatesViewModel
import com.jetbrains.kmpapp.screens.todo.TodoListDetailViewModel
import com.jetbrains.kmpapp.screens.todo.TodoListsViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.sse.SSE
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.plugin
import io.ktor.client.request.bearerAuth
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val dataModule = module {
    single(named("authClient")) {
        val json = Json { ignoreUnknownKeys = true; encodeDefaults = true; explicitNulls = false }
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
        val json = Json { ignoreUnknownKeys = true; encodeDefaults = true; explicitNulls = false }
        val tokenStorage = get<TokenStorage>()
        val refreshMutex = Mutex()
        val client = HttpClient {
            install(Logging) {
                logger = createHttpLogger()
                level = LogLevel.ALL
            }
            install(ContentNegotiation) {
                json(json, contentType = ContentType.Any)
            }
            install(SSE)
            defaultRequest {
                tokenStorage.getAccessToken()?.let { bearerAuth(it) }
            }
        }
        client.plugin(HttpSend).intercept { request ->
            val originalCall = execute(request)
            if (originalCall.response.status != HttpStatusCode.Unauthorized) {
                return@intercept originalCall
            }
            val refreshed = refreshMutex.withLock {
                // If another coroutine already refreshed while we waited,
                // check if the current token differs from the one used in the failed request
                val authApi = get<AuthApi>()
                val refreshToken = tokenStorage.getRefreshToken() ?: return@withLock false
                authApi.refresh(refreshToken)
                    .onSuccess { tokens -> tokenStorage.saveTokens(tokens, isGuest = false) }
                    .onFailure { get<AuthRepository>().forceUnauthenticated() }
                    .isSuccess
            }
            if (refreshed) {
                // Retry with new token
                tokenStorage.getAccessToken()?.let { request.bearerAuth(it) }
                execute(request)
            } else {
                originalCall
            }
        }
        client
    }

    single<AuthApi> {
        KtorAuthApi(
            authClient = get(named("authClient")),
            apiClient = get(named("apiClient")),
            baseUrl = getApiBaseUrl(),
        )
    }

    single {
        AuthRepository(
            authApi = get(),
            tokenStorage = get(),
            listsStorage = get(),
            onLogout = {
                get<ListsRepository>().clearAll()
                get<GroupsRepository>().clearAll()
                get<CategoriesRepository>().clear()
                get<SuggestionsRepository>().clear()
                get<SyncRepository>().clear()
                get<GamificationRepository>().clearAll()
                get<AchievementsRepository>().clearAll()
                get<GoalsRepository>().clearAll()
            },
            onAuthenticated = { get<DeviceRegistrar>().registerInBackground() },
        )
    }

    single<ListsApi> {
        KtorListsApi(
            apiClient = get(named("apiClient")),
            baseUrl = getApiBaseUrl(),
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

    single<CategoriesApi> { KtorCategoriesApi(get(named("apiClient")), getApiBaseUrl()) }
    single { CategoriesRepository(get()) }

    single<SuggestionsApi> { KtorSuggestionsApi(get(named("apiClient")), getApiBaseUrl()) }
    single { SuggestionsRepository(get()) }

    single<SyncApi> { KtorSyncApi(get(named("apiClient")), getApiBaseUrl()) }
    single { SyncRepository(get(), get(), get(), get()) }

    single<ProfileApi> { KtorProfileApi(get(named("apiClient")), getApiBaseUrl()) }
    single<NotificationsApi> { KtorNotificationsApi(get(named("apiClient")), getApiBaseUrl()) }
    single<ChatApi> { KtorChatApi(get(named("apiClient")), getApiBaseUrl()) }
    single<DeviceApi> { KtorDeviceApi(get(named("apiClient")), getApiBaseUrl()) }
    single { DeviceRegistrar(get(), get(), get()) }
    single<AttachmentsApi> {
        KtorAttachmentsApi(
            apiClient = get(named("apiClient")),
            uploadClient = get(named("authClient")),
            baseUrl = getApiBaseUrl(),
        )
    }
    single { AttachmentsRepository(get()) }
    single<PublicWishlistApi> { KtorPublicWishlistApi(get(named("authClient")), getApiBaseUrl()) }
    single<CalendarSharingApi> { KtorCalendarSharingApi(get(named("apiClient")), getApiBaseUrl()) }
    single<GamificationApi> { KtorGamificationApi(get(named("apiClient")), getApiBaseUrl()) }
    single { GamificationRepository(get()) }
    single<AchievementsApi> { KtorAchievementsApi(get(named("apiClient")), getApiBaseUrl()) }
    single { AchievementsRepository(get()) }
    single<GoalsApi> { KtorGoalsApi(get(named("apiClient")), getApiBaseUrl()) }
    single { GoalsRepository(get()) }
}

val viewModelModule = module {
    factoryOf(::AuthViewModel)
    factoryOf(::TodoListsViewModel)
    factoryOf(::TodoListDetailViewModel)
    factoryOf(::GroupsViewModel)
    factoryOf(::GroupDetailViewModel)
    factoryOf(::ProfileViewModel)
    factoryOf(::FamilyViewModel)
    factoryOf(::HomeViewModel)
    factoryOf(::TemplatesViewModel)
    factoryOf(::NotificationsViewModel)
    factoryOf(::ChatViewModel)
    factoryOf(::NotificationPrefsViewModel)
    factoryOf(::PublicWishlistViewModel)
    factoryOf(::CalendarViewModel)
    factoryOf(::GamificationViewModel)
    factoryOf(::GoalsViewModel)
    factoryOf(::GoalDetailViewModel)
}

expect fun platformModules(): List<org.koin.core.module.Module>

expect fun initKoin()
