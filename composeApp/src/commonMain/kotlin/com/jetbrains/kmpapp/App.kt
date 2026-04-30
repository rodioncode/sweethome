package com.jetbrains.kmpapp

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.jetbrains.kmpapp.auth.AuthScreen
import com.jetbrains.kmpapp.auth.AuthState
import com.jetbrains.kmpapp.auth.AuthRepository
import com.jetbrains.kmpapp.auth.AuthViewModel
import com.jetbrains.kmpapp.auth.LinkEmailScreen
import com.jetbrains.kmpapp.auth.RegisterScreen
import com.jetbrains.kmpapp.data.groups.GroupsRepository
import com.jetbrains.kmpapp.screens.family.FamilyMembersScreen
import com.jetbrains.kmpapp.screens.family.FamilyShopScreen
import com.jetbrains.kmpapp.screens.family.GamificationScreen
import com.jetbrains.kmpapp.screens.groups.GroupDetailScreen
import com.jetbrains.kmpapp.screens.chat.ChatScreen
import com.jetbrains.kmpapp.screens.notifications.NotificationsScreen
import com.jetbrains.kmpapp.screens.templates.TemplateDetailScreen
import com.jetbrains.kmpapp.screens.main.MainScreen
import com.jetbrains.kmpapp.screens.profile.ProfileContent
import com.jetbrains.kmpapp.screens.splash.SplashScreen
import com.jetbrains.kmpapp.screens.settings.SettingsScreen
import com.jetbrains.kmpapp.screens.spaces.JoinByCodeScreen
import com.jetbrains.kmpapp.screens.todo.TodoListDetailScreen
import com.jetbrains.kmpapp.ui.SweetHomeTheme
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Serializable
object SplashDestination

@Serializable
object AuthDestination

@Serializable
object RegisterDestination

@Serializable
object LinkEmailDestination

@Serializable
object ListDestination

@Serializable
object ProfileDestination

@Serializable
data class TodoListDetailDestination(val listId: String)

@Serializable
data class GroupDetailDestination(val groupId: String, val groupName: String)

@Serializable
data class InviteDestination(val token: String)

@Serializable
object SettingsDestination

@Serializable
data class JoinByCodeDestination(val prefillCode: String = "")

@Serializable
object GamificationDestination

@Serializable
object FamilyShopDestination

@Serializable
object FamilyMembersDestination

@Serializable
data class TemplateDetailDestination(val title: String = "Список покупок", val icon: String = "🛒")

@Serializable
object NotificationsDestination

@Serializable
data class ChatDestination(val workspaceId: String = "", val title: String = "Чат", val memberCount: Int = 0)

@Serializable
object PasswordResetRequestDestination

@Serializable
data class PasswordResetConfirmDestination(val token: String = "")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    SweetHomeTheme {
        Surface {
            val navController: NavHostController = rememberNavController()
            val authRepository: AuthRepository = koinInject()
            val authViewModel = koinViewModel<AuthViewModel>()
            val authState by authRepository.authState.collectAsState(initial = AuthState.Initial)

            // Case 1: deep link arrives when user is already authenticated
            val pendingLink by DeepLinkHandler.pendingDeepLink.collectAsState()
            LaunchedEffect(pendingLink) {
                val url = pendingLink ?: return@LaunchedEffect
                if (authState !is AuthState.Authenticated) return@LaunchedEffect
                DeepLinkHandler.pendingDeepLink.value = null
                val token = url.removePrefix("familytodo://invite/")
                if (token.isNotEmpty() && token != url) {
                    navController.navigate(InviteDestination(token))
                }
            }

            // Case 2: deep link was pending while user was logging in
            fun navigatePendingInvite() {
                val url = DeepLinkHandler.pendingDeepLink.value ?: return
                DeepLinkHandler.pendingDeepLink.value = null
                val token = url.removePrefix("familytodo://invite/")
                if (token.isNotEmpty() && token != url) {
                    navController.navigate(InviteDestination(token))
                }
            }

            NavHost(
                navController = navController,
                startDestination = SplashDestination,
                modifier = Modifier,
            ) {
                composable<SplashDestination> {
                    SplashScreen(
                        isAuthenticated = authState is AuthState.Authenticated,
                        onNavigateToMain = {
                            navController.navigate(ListDestination) {
                                popUpTo(SplashDestination) { inclusive = true }
                            }
                        },
                        onNavigateToLogin = {
                            navController.navigate(AuthDestination) {
                                popUpTo(SplashDestination) { inclusive = true }
                            }
                        },
                        onNavigateToRegister = {
                            navController.navigate(RegisterDestination) {
                                popUpTo(SplashDestination) { inclusive = true }
                            }
                        },
                        onGuestLogin = {
                            authViewModel.loginAsGuest()
                        },
                    )
                }
                composable<AuthDestination> {
                    AuthScreen(
                        onAuthSuccess = {
                            navController.navigate(ListDestination) {
                                popUpTo(AuthDestination) { inclusive = true }
                            }
                            navigatePendingInvite()
                        },
                        onNavigateToRegister = { navController.navigate(RegisterDestination) },
                        onNavigateToLinkEmail = { navController.navigate(LinkEmailDestination) },
                        onNavigateToPasswordReset = { navController.navigate(PasswordResetRequestDestination) },
                    )
                }
                composable<PasswordResetRequestDestination> {
                    com.jetbrains.kmpapp.auth.PasswordResetRequestScreen(
                        onBack = { navController.popBackStack() },
                        onSent = {
                            navController.navigate(PasswordResetConfirmDestination()) {
                                popUpTo(PasswordResetRequestDestination) { inclusive = true }
                            }
                        },
                    )
                }
                composable<PasswordResetConfirmDestination> { backStackEntry ->
                    val dest = backStackEntry.toRoute<PasswordResetConfirmDestination>()
                    com.jetbrains.kmpapp.auth.PasswordResetConfirmScreen(
                        token = dest.token,
                        onBack = { navController.popBackStack() },
                        onSuccess = {
                            navController.navigate(AuthDestination) {
                                popUpTo(PasswordResetConfirmDestination()) { inclusive = true }
                            }
                        },
                    )
                }
                composable<RegisterDestination> {
                    RegisterScreen(
                        onRegisterSuccess = {
                            navController.navigate(ListDestination) {
                                popUpTo(AuthDestination) { inclusive = true }
                            }
                            navigatePendingInvite()
                        },
                        onNavigateBack = { navController.popBackStack() },
                    )
                }
                composable<LinkEmailDestination> {
                    LinkEmailScreen(
                        onLinkSuccess = {
                            navController.navigate(ListDestination) {
                                popUpTo(AuthDestination) { inclusive = true }
                            }
                            navigatePendingInvite()
                        },
                        onNavigateBack = { navController.popBackStack() },
                    )
                }
                composable<ListDestination> {
                    MainScreen(
                        navigateToListDetail = { listId ->
                            navController.navigate(TodoListDetailDestination(listId))
                        },
                        navigateToGroupDetail = { groupId, groupName ->
                            navController.navigate(GroupDetailDestination(groupId, groupName))
                        },
                        navigateToLinkEmail = { navController.navigate(LinkEmailDestination) },
                        navigateToJoinByCode = { navController.navigate(JoinByCodeDestination()) },
                        navigateToProfile = { navController.navigate(ProfileDestination) },
                        navigateToGamification = { navController.navigate(GamificationDestination) },
                        navigateToShop = { navController.navigate(FamilyShopDestination) },
                    )
                }
                composable<ProfileDestination> {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = { Text("Профиль") },
                                navigationIcon = {
                                    IconButton(onClick = { navController.popBackStack() }) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                                    }
                                }
                            )
                        },
                        modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars),
                    ) { paddingValues ->
                        ProfileContent(
                            navigateToLinkEmail = { navController.navigate(LinkEmailDestination) },
                            navigateToSettings = { navController.navigate(SettingsDestination) },
                            modifier = Modifier,
                        )
                    }
                }
                composable<SettingsDestination> {
                    SettingsScreen(
                        onNavigateBack = { navController.popBackStack() },
                    )
                }
                composable<TodoListDetailDestination> { backStackEntry ->
                    TodoListDetailScreen(
                        listId = backStackEntry.toRoute<TodoListDetailDestination>().listId,
                        navigateBack = { navController.popBackStack() },
                    )
                }
                composable<GroupDetailDestination> { backStackEntry ->
                    val dest = backStackEntry.toRoute<GroupDetailDestination>()
                    GroupDetailScreen(
                        groupId = dest.groupId,
                        groupName = dest.groupName,
                        navigateBack = { navController.popBackStack() },
                        navigateToListDetail = { listId ->
                            navController.navigate(TodoListDetailDestination(listId))
                        },
                        navigateToLinkEmail = { navController.navigate(LinkEmailDestination) },
                    )
                }
                composable<JoinByCodeDestination> { backStackEntry ->
                    val dest = backStackEntry.toRoute<JoinByCodeDestination>()
                    JoinByCodeScreen(
                        prefillCode = dest.prefillCode,
                        onSuccess = { groupId, groupName ->
                            navController.navigate(GroupDetailDestination(groupId, groupName)) {
                                popUpTo(JoinByCodeDestination()) { inclusive = true }
                            }
                        },
                        onEmailRequired = {
                            navController.navigate(LinkEmailDestination) {
                                popUpTo(JoinByCodeDestination()) { inclusive = true }
                            }
                        },
                        onNavigateBack = { navController.popBackStack() },
                    )
                }
                composable<InviteDestination> { backStackEntry ->
                    val token = backStackEntry.toRoute<InviteDestination>().token
                    InviteScreen(
                        token = token,
                        onSuccess = { groupId, groupName ->
                            navController.navigate(GroupDetailDestination(groupId, groupName)) {
                                popUpTo(InviteDestination(token)) { inclusive = true }
                            }
                        },
                        onEmailRequired = {
                            navController.navigate(LinkEmailDestination) {
                                popUpTo(InviteDestination(token)) { inclusive = true }
                            }
                        },
                        onError = { navController.popBackStack() },
                    )
                }
                composable<GamificationDestination> {
                    GamificationScreen(
                        navigateBack = { navController.popBackStack() },
                        navigateToShop = { navController.navigate(FamilyShopDestination) },
                    )
                }
                composable<FamilyShopDestination> {
                    FamilyShopScreen(
                        navigateBack = { navController.popBackStack() },
                    )
                }
                composable<FamilyMembersDestination> {
                    FamilyMembersScreen(
                        navigateBack = { navController.popBackStack() },
                    )
                }
                composable<TemplateDetailDestination> { backStackEntry ->
                    val dest = backStackEntry.toRoute<TemplateDetailDestination>()
                    TemplateDetailScreen(
                        templateTitle = dest.title,
                        templateIcon = dest.icon,
                        navigateBack = { navController.popBackStack() },
                        navigateToLists = {
                            navController.navigate(ListDestination) {
                                popUpTo(ListDestination) { inclusive = false }
                            }
                        },
                    )
                }
                composable<NotificationsDestination> {
                    NotificationsScreen()
                }
                composable<ChatDestination> { backStackEntry ->
                    val dest = backStackEntry.toRoute<ChatDestination>()
                    ChatScreen(
                        workspaceId = dest.workspaceId,
                        chatTitle = dest.title,
                        memberCount = dest.memberCount,
                        navigateBack = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}
