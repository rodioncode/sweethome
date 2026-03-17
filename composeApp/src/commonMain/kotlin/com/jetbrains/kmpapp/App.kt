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
import com.jetbrains.kmpapp.screens.detail.DetailScreen
import com.jetbrains.kmpapp.screens.todo.TodoListDetailScreen
import com.jetbrains.kmpapp.screens.todo.TodoListsScreen
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
data class DetailDestination(val objectId: Int)

@Serializable
data class TodoListDetailDestination(val listId: String)

@Composable
fun App() {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
    ) {
        Surface {
            val navController: NavHostController = rememberNavController()
            val authRepository: AuthRepository = koinInject()
            val authState by authRepository.authState.collectAsState(initial = AuthState.Initial)

            LaunchedEffect(authState) {
                when (authState) {
                    is AuthState.Authenticated -> {
                        navController.navigate(ListDestination) {
                            popUpTo(AuthDestination) { inclusive = true }
                        }
                    }
                    is AuthState.Unauthenticated -> {
                        navController.navigate(AuthDestination) {
                            popUpTo<ListDestination> { inclusive = true }
                        }
                    }
                    else -> {}
                }
            }

            NavHost(
                navController = navController,
                startDestination = AuthDestination,
                modifier = androidx.compose.ui.Modifier
            ) {
                composable<AuthDestination> {
                    AuthScreen(
                        onAuthSuccess = {
                            navController.navigate(ListDestination) {
                                popUpTo(AuthDestination) { inclusive = true }
                            }
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
                        },
                        onNavigateBack = { navController.popBackStack() },
                    )
                }
                composable<ListDestination> {
                    TodoListsScreen(
                        navigateToListDetail = { listId ->
                            navController.navigate(TodoListDetailDestination(listId))
                        },
                    )
                }
                composable<TodoListDetailDestination> { backStackEntry ->
                    TodoListDetailScreen(
                        listId = backStackEntry.toRoute<TodoListDetailDestination>().listId,
                        navigateBack = { navController.popBackStack() },
                    )
                }
                composable<DetailDestination> { backStackEntry ->
                    DetailScreen(
                        objectId = backStackEntry.toRoute<DetailDestination>().objectId,
                        navigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
