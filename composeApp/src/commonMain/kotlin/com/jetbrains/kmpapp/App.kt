package com.jetbrains.kmpapp

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
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
import com.jetbrains.kmpapp.auth.LinkEmailScreen
import com.jetbrains.kmpapp.auth.RegisterScreen
import com.jetbrains.kmpapp.data.groups.GroupsRepository
import com.jetbrains.kmpapp.screens.groups.GroupDetailScreen
import com.jetbrains.kmpapp.screens.main.MainScreen
import com.jetbrains.kmpapp.screens.todo.TodoListDetailScreen
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

@Serializable
object AuthDestination

@Serializable
object RegisterDestination

@Serializable
object LinkEmailDestination

@Serializable
object ListDestination

@Serializable
data class TodoListDetailDestination(val listId: String)

@Serializable
data class GroupDetailDestination(val groupId: String, val groupName: String)

@Serializable
data class InviteDestination(val token: String)

@Composable
fun App() {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
    ) {
        Surface {
            val navController: NavHostController = rememberNavController()
            val authRepository: AuthRepository = koinInject()
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

            // Case 2: deep link was pending while user was logging in — call this after navigating to ListDestination
            fun navigatePendingInvite() {
                val url = DeepLinkHandler.pendingDeepLink.value ?: return
                DeepLinkHandler.pendingDeepLink.value = null
                val token = url.removePrefix("familytodo://invite/")
                if (token.isNotEmpty() && token != url) {
                    navController.navigate(InviteDestination(token))
                }
            }

            val startDestination = when (authState) {
                is AuthState.Authenticated -> {
                    ListDestination
                }

                is AuthState.Unauthenticated -> {
                    AuthDestination
                }
                else -> {
                    AuthDestination
                }
            }

            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier,
            ) {
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
            }
        }
    }
}
