package com.jetbrains.kmpapp

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.jetbrains.kmpapp.auth.AuthRepository
import com.jetbrains.kmpapp.auth.AuthScreen
import com.jetbrains.kmpapp.auth.AuthState
import com.jetbrains.kmpapp.auth.LinkEmailScreen
import com.jetbrains.kmpapp.auth.RegisterScreen
import com.jetbrains.kmpapp.data.sync.SyncRepository
import com.jetbrains.kmpapp.data.sync.SyncStatus
import com.jetbrains.kmpapp.screens.groups.CreateGroupScreen
import com.jetbrains.kmpapp.screens.groups.GroupDetailScreen
import com.jetbrains.kmpapp.screens.groups.GroupsScreen
import com.jetbrains.kmpapp.screens.todo.TodoListDetailScreen
import com.jetbrains.kmpapp.screens.todo.TodoListsScreen
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

// ── Навигационные точки ───────────────────────────────────────────────────────

@Serializable object AuthDestination
@Serializable object RegisterDestination
@Serializable object LinkEmailDestination
@Serializable object MainDestination
@Serializable object MyListsDestination
@Serializable object GroupsDestination
@Serializable data class TodoListDetailDestination(val listId: String)
@Serializable data class GroupDetailDestination(val groupId: String)
@Serializable object CreateGroupDestination

// ── CompositionLocal для root-навигатора ─────────────────────────────────────

val LocalRootNavController = staticCompositionLocalOf<NavHostController> {
    error("RootNavController not provided")
}

// ── App ───────────────────────────────────────────────────────────────────────

@Composable
fun App() {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme(),
    ) {
        Surface {
            val rootNavController = rememberNavController()
            val authRepository: AuthRepository = koinInject()
            val authState by authRepository.authState.collectAsState(initial = AuthState.Initial)

            LaunchedEffect(authState) {
                when (authState) {
                    is AuthState.Authenticated -> rootNavController.navigate(MainDestination) {
                        popUpTo(AuthDestination) { inclusive = true }
                    }
                    is AuthState.Unauthenticated -> rootNavController.navigate(AuthDestination) {
                        popUpTo(MainDestination) { inclusive = true }
                    }
                    else -> {}
                }
            }

            CompositionLocalProvider(LocalRootNavController provides rootNavController) {
                NavHost(
                    navController = rootNavController,
                    startDestination = AuthDestination,
                ) {
                    // ── Auth ──────────────────────────────────────────────────
                    composable<AuthDestination> {
                        AuthScreen(
                            onAuthSuccess = {
                                rootNavController.navigate(MainDestination) {
                                    popUpTo(AuthDestination) { inclusive = true }
                                }
                            },
                            onNavigateToRegister = { rootNavController.navigate(RegisterDestination) },
                            onNavigateToLinkEmail = { rootNavController.navigate(LinkEmailDestination) },
                        )
                    }
                    composable<RegisterDestination> {
                        RegisterScreen(
                            onRegisterSuccess = {
                                rootNavController.navigate(MainDestination) {
                                    popUpTo(AuthDestination) { inclusive = true }
                                }
                            },
                            onNavigateBack = { rootNavController.popBackStack() },
                        )
                    }
                    composable<LinkEmailDestination> {
                        LinkEmailScreen(
                            onLinkSuccess = {
                                rootNavController.navigate(MainDestination) {
                                    popUpTo(AuthDestination) { inclusive = true }
                                }
                            },
                            onNavigateBack = { rootNavController.popBackStack() },
                        )
                    }

                    // ── Main (с Bottom Bar) ───────────────────────────────────
                    composable<MainDestination> {
                        val syncRepository: SyncRepository = koinInject()
                        // Запускаем синхронизацию при каждом входе на главный экран
                        LaunchedEffect(Unit) { syncRepository.sync() }
                        MainScreen(
                            currentUserId = (authState as? AuthState.Authenticated)?.userId,
                            syncRepository = syncRepository,
                        )
                    }

                    // ── Детали поверх Bottom Bar ──────────────────────────────
                    composable<TodoListDetailDestination> { entry ->
                        TodoListDetailScreen(
                            listId = entry.toRoute<TodoListDetailDestination>().listId,
                            navigateBack = { rootNavController.popBackStack() },
                        )
                    }
                    composable<GroupDetailDestination> { entry ->
                        GroupDetailScreen(
                            groupId = entry.toRoute<GroupDetailDestination>().groupId,
                            currentUserId = (authState as? AuthState.Authenticated)?.userId,
                            navigateBack = { rootNavController.popBackStack() },
                            navigateToListDetail = { listId ->
                                rootNavController.navigate(TodoListDetailDestination(listId))
                            },
                        )
                    }
                    composable<CreateGroupDestination> {
                        CreateGroupScreen(
                            navigateBack = { rootNavController.popBackStack() },
                            onGroupCreated = { groupId ->
                                rootNavController.navigate(GroupDetailDestination(groupId)) {
                                    popUpTo(CreateGroupDestination) { inclusive = true }
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

// ── Главный экран с Bottom Navigation Bar ────────────────────────────────────

@Composable
private fun MainScreen(
    currentUserId: String?,
    syncRepository: SyncRepository,
) {
    val tabNavController = rememberNavController()
    val rootNavController = LocalRootNavController.current
    val backStack by tabNavController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val syncStatus by syncRepository.syncStatus.collectAsState()

    data class TabItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val destination: Any, val routeKey: String)

    val tabs = listOf(
        TabItem("Мои списки", Icons.Default.CheckCircle, MyListsDestination, "MyListsDestination"),
        TabItem("Группы",     Icons.Default.Group,        GroupsDestination,   "GroupsDestination"),
    )

    Scaffold(
        topBar = {
            // Тонкий индикатор синхронизации поверх экрана
            if (syncStatus is SyncStatus.Syncing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        },
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    val selected = currentRoute?.contains(tab.routeKey) == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            tabNavController.navigate(tab.destination) {
                                popUpTo(tabNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { paddingValues ->
        NavHost(
            navController = tabNavController,
            startDestination = MyListsDestination,
            modifier = Modifier.padding(paddingValues),
        ) {
            composable<MyListsDestination> {
                TodoListsScreen(
                    navigateToListDetail = { listId ->
                        rootNavController.navigate(TodoListDetailDestination(listId))
                    },
                )
            }
            composable<GroupsDestination> {
                GroupsScreen(
                    navigateToGroupDetail = { groupId ->
                        rootNavController.navigate(GroupDetailDestination(groupId))
                    },
                    navigateToCreateGroup = {
                        rootNavController.navigate(CreateGroupDestination)
                    },
                )
            }
        }
    }
}
