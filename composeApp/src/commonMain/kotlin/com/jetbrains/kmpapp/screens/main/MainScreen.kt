package com.jetbrains.kmpapp.screens.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.LocalCozyElevation
import com.jetbrains.kmpapp.ui.LocalCozyShapes
import com.jetbrains.kmpapp.ui.LocalCozySpacing
import com.jetbrains.kmpapp.ui.components.CozyBottomNav
import com.jetbrains.kmpapp.ui.components.CozyTab
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.jetbrains.kmpapp.data.sync.SyncRepository
import com.jetbrains.kmpapp.screens.family.FamilyContent
import com.jetbrains.kmpapp.screens.groups.CreateGroupDialog
import com.jetbrains.kmpapp.screens.groups.GroupDetailScreen
import com.jetbrains.kmpapp.screens.groups.GroupsContent
import com.jetbrains.kmpapp.screens.groups.GroupsUiEvent
import com.jetbrains.kmpapp.screens.groups.GroupsViewModel
import com.jetbrains.kmpapp.screens.home.HomeContent
import com.jetbrains.kmpapp.screens.todo.TodoListDetailScreen
import com.jetbrains.kmpapp.screens.todo.TodoListsContent
import com.jetbrains.kmpapp.screens.todo.TodoListsViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

enum class MainTab { HOME, LISTS, FAMILY, CALENDAR, PROFILE }

private fun CozyTab.toMainTab(): MainTab = when (this) {
    CozyTab.HOME     -> MainTab.HOME
    CozyTab.LISTS    -> MainTab.LISTS
    CozyTab.FAMILY   -> MainTab.FAMILY
    CozyTab.CALENDAR -> MainTab.CALENDAR
    CozyTab.PROFILE  -> MainTab.PROFILE
}

private fun MainTab.toCozyTab(): CozyTab = when (this) {
    MainTab.HOME     -> CozyTab.HOME
    MainTab.LISTS    -> CozyTab.LISTS
    MainTab.FAMILY   -> CozyTab.FAMILY
    MainTab.CALENDAR -> CozyTab.CALENDAR
    MainTab.PROFILE  -> CozyTab.PROFILE
}

@Serializable private object ListsRoot
@Serializable private data class ListsDetail(val listId: String)

@Serializable private object GroupsRoot
@Serializable private data class GroupsDetail(val groupId: String, val groupName: String)
@Serializable private data class GroupsChat(val workspaceId: String, val title: String, val memberCount: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navigateToListDetail: (String) -> Unit,
    navigateToGroupDetail: (groupId: String, groupName: String) -> Unit,
    navigateToLinkEmail: () -> Unit,
    navigateToJoinByCode: () -> Unit,
    navigateToProfile: () -> Unit,
    navigateToGamification: () -> Unit = {},
    navigateToShop: () -> Unit = {},
    navigateToGoals: () -> Unit = {},
    navigateToChat: (workspaceId: String, title: String, memberCount: Int) -> Unit = { _, _, _ -> },
    navigateToCreateList: (workspaceId: String?) -> Unit = {},
    navigateToTemplates: () -> Unit = {},
    pendingInviteToken: String? = null,
) {
    val todoListsViewModel = koinViewModel<TodoListsViewModel>()
    val groupsViewModel = koinViewModel<GroupsViewModel>()
    val syncRepository = koinInject<SyncRepository>()
    val coroutineScope = rememberCoroutineScope()
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    val listsNavController = rememberNavController()
    val groupsNavController = rememberNavController()

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                coroutineScope.launch { syncRepository.sync() }
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    val lists by todoListsViewModel.lists.collectAsStateWithLifecycle()
    val listsError by todoListsViewModel.error.collectAsStateWithLifecycle()
    val allGroups by todoListsViewModel.groups.collectAsStateWithLifecycle()
    val groupSpaces by groupsViewModel.groupSpaces.collectAsStateWithLifecycle()
    val groupsError by groupsViewModel.error.collectAsStateWithLifecycle()
    val isGuest by groupsViewModel.isGuest.collectAsStateWithLifecycle()

    var selectedTab by rememberSaveable { mutableStateOf(MainTab.HOME) }
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var fabExpanded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Pending cross-tab navigation. NavController.navigate() requires the target tab's NavHost
    // to be in composition (so setGraph() has been called). When the user triggers a cross-tab
    // jump from a tab that hasn't been visited yet, we stash the route here and let a
    // LaunchedEffect dispatch it once the target NavHost is ready.
    var pendingListsRoute by remember { mutableStateOf<Any?>(null) }
    var pendingGroupsRoute by remember { mutableStateOf<Any?>(null) }

    val listsBackStackEntry by listsNavController.currentBackStackEntryAsState()
    val groupsBackStackEntry by groupsNavController.currentBackStackEntryAsState()

    LaunchedEffect(listsBackStackEntry, pendingListsRoute) {
        val route = pendingListsRoute ?: return@LaunchedEffect
        if (listsBackStackEntry == null) return@LaunchedEffect
        pendingListsRoute = null
        listsNavController.navigate(route) { launchSingleTop = true }
    }

    LaunchedEffect(groupsBackStackEntry, pendingGroupsRoute) {
        val route = pendingGroupsRoute ?: return@LaunchedEffect
        if (groupsBackStackEntry == null) return@LaunchedEffect
        pendingGroupsRoute = null
        groupsNavController.navigate(route) { launchSingleTop = true }
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == MainTab.LISTS) todoListsViewModel.refresh()
    }

    LaunchedEffect(listsError) {
        listsError?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            todoListsViewModel.clearError()
        }
    }

    LaunchedEffect(groupsError) {
        groupsError?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            groupsViewModel.clearError()
        }
    }

    LaunchedEffect(Unit) {
        groupsViewModel.uiEvent.collect { event ->
            when (event) {
                is GroupsUiEvent.NavigateToGroup -> {
                    pendingGroupsRoute = GroupsDetail(event.groupId, event.groupName)
                    selectedTab = MainTab.FAMILY
                }
            }
        }
    }

    LaunchedEffect(pendingInviteToken) {
        pendingInviteToken?.let {
            selectedTab = MainTab.FAMILY
        }
    }

    Scaffold(
        bottomBar = {
            CozyBottomNav(
                active = selectedTab.toCozyTab(),
                onTabSelected = { selectedTab = it.toMainTab() },
            )
        },
        floatingActionButton = {
            val isInListDetail = listsBackStackEntry?.destination?.hasRoute(ListsDetail::class) == true
            when {
                selectedTab == MainTab.LISTS && !isInListDetail -> {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .shadow(LocalCozyElevation.current.fab, CircleShape)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { navigateToCreateList(null) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("+", color = MaterialTheme.colorScheme.onPrimary, fontSize = 28.sp, fontWeight = FontWeight.Light)
                    }
                }
                selectedTab == MainTab.HOME -> {
                    DashboardFabSpeedDial(
                        expanded = fabExpanded,
                        onToggle = { fabExpanded = !fabExpanded },
                        onAddTask = {
                            fabExpanded = false
                            navigateToCreateList(null)
                        },
                        onAddList = {
                            fabExpanded = false
                            navigateToCreateList(null)
                        },
                        onAddGoal = {
                            fabExpanded = false
                            navigateToGoals()
                        },
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        when (selectedTab) {
            MainTab.HOME -> HomeContent(
                contentPadding = paddingValues,
                navigateToListDetail = navigateToListDetail,
                onCreateList = { navigateToCreateList(null) },
                onNavigateToHome = { selectedTab = MainTab.FAMILY },
                onNavigateToGroups = { selectedTab = MainTab.FAMILY },
                navigateToProfile = navigateToProfile,
                navigateToGoals = navigateToGoals,
                onNavigateToLists = { selectedTab = MainTab.LISTS },
            )
            MainTab.FAMILY -> FamilyContent(
                contentPadding = paddingValues,
                onSpaceClick = { groupId, name ->
                    pendingGroupsRoute = GroupsDetail(groupId, name)
                    selectedTab = MainTab.FAMILY
                },
                onListClick = { listId ->
                    pendingListsRoute = ListsDetail(listId)
                    selectedTab = MainTab.LISTS
                },
                navigateToGamification = navigateToGamification,
                navigateToShop = navigateToShop,
                navigateToGoals = navigateToGoals,
                navigateToJoinByCode = navigateToJoinByCode,
            )
            MainTab.LISTS -> ListsTabNavHost(
                navController = listsNavController,
                paddingValues = paddingValues,
                lists = lists,
                groups = allGroups,
                isGuest = isGuest,
                navigateToLinkEmail = navigateToLinkEmail,
                navigateToCreateList = navigateToCreateList,
                navigateToTemplates = navigateToTemplates,
            )
            MainTab.CALENDAR -> com.jetbrains.kmpapp.screens.calendar.CalendarContent(
                contentPadding = paddingValues,
                onItemOpen = { listId, _ ->
                    pendingListsRoute = ListsDetail(listId)
                    selectedTab = MainTab.LISTS
                },
            )
            MainTab.PROFILE -> {
                com.jetbrains.kmpapp.screens.profile.ProfileContent(
                    navigateToLinkEmail = navigateToLinkEmail,
                    navigateToTemplates = navigateToTemplates,
                )
            }
        }
    }

    if (showCreateGroupDialog) {
        CreateGroupDialog(
            onDismiss = { showCreateGroupDialog = false },
            onConfirm = { name, type ->
                groupsViewModel.createGroup(name, type)
                showCreateGroupDialog = false
            },
        )
    }
}

@Composable
private fun DashboardFabSpeedDial(
    expanded: Boolean,
    onToggle: () -> Unit,
    onAddTask: () -> Unit,
    onAddList: () -> Unit,
    onAddGoal: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(LocalCozySpacing.current.sm),
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(LocalCozySpacing.current.sm),
            ) {
                SpeedDialAction("Цель", Icons.Default.Flag, onAddGoal)
                SpeedDialAction("Список", Icons.Default.PlaylistAdd, onAddList)
                SpeedDialAction("Задача", Icons.Default.Task, onAddTask)
            }
        }
        FloatingActionButton(
            onClick = onToggle,
            shape = CircleShape,
        ) {
            Icon(
                if (expanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = if (expanded) "Закрыть" else "Создать",
            )
        }
    }
}

@Composable
private fun SpeedDialAction(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {
        androidx.compose.material3.Surface(
            shape = LocalCozyShapes.current.pill,
            color = androidx.compose.material3.MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = LocalCozySpacing.current.sm, vertical = LocalCozySpacing.current.xs),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(Modifier.width(LocalCozySpacing.current.sm))
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier.size(36.dp + 8.dp),
            shape = CircleShape,
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
            contentColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun ListsTabNavHost(
    navController: NavHostController,
    paddingValues: PaddingValues,
    lists: List<com.jetbrains.kmpapp.data.lists.TodoList>,
    groups: List<com.jetbrains.kmpapp.data.groups.Group>,
    isGuest: Boolean,
    navigateToLinkEmail: () -> Unit,
    navigateToCreateList: (workspaceId: String?) -> Unit,
    navigateToTemplates: () -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = ListsRoot,
        modifier = Modifier.fillMaxSize(),
    ) {
        composable<ListsRoot> {
            TodoListsContent(
                lists = lists,
                groups = groups,
                contentPadding = paddingValues,
                onListClick = { listId -> navController.navigate(ListsDetail(listId)) },
                onCreateList = { navigateToCreateList(null) },
                onNavigateToTemplates = navigateToTemplates,
                isGuest = isGuest,
                navigateToLinkEmail = navigateToLinkEmail,
            )
        }
        composable<ListsDetail> { backStack ->
            val dest = backStack.toRoute<ListsDetail>()
            TodoListDetailScreen(
                listId = dest.listId,
                navigateBack = { navController.popBackStack() },
                contentWindowInsets = WindowInsets(bottom = paddingValues.calculateBottomPadding()),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupsTabNavHost(
    navController: NavHostController,
    paddingValues: PaddingValues,
    groupSpaces: List<com.jetbrains.kmpapp.data.groups.Group>,
    isGuest: Boolean,
    navigateToLinkEmail: () -> Unit,
    navigateToJoinByCode: () -> Unit,
    onCreateGroup: (() -> Unit)?,
    navigateToListDetail: (String) -> Unit,
    navigateToChat: (workspaceId: String, title: String, memberCount: Int) -> Unit,
    navigateToCreateList: (workspaceId: String?) -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = GroupsRoot,
        modifier = Modifier.fillMaxSize(),
    ) {
        composable<GroupsRoot> {
            GroupsContent(
                groups = groupSpaces,
                isGuest = isGuest,
                contentPadding = paddingValues,
                onGroupClick = { group ->
                    navController.navigate(GroupsDetail(group.id, group.title))
                },
                navigateToLinkEmail = navigateToLinkEmail,
                navigateToJoinByCode = navigateToJoinByCode,
                onCreateGroup = onCreateGroup,
            )
        }
        composable<GroupsDetail> { backStack ->
            val dest = backStack.toRoute<GroupsDetail>()
            GroupDetailScreen(
                groupId = dest.groupId,
                groupName = dest.groupName,
                navigateBack = { navController.popBackStack() },
                navigateToListDetail = navigateToListDetail,
                navigateToLinkEmail = navigateToLinkEmail,
                navigateToChat = navigateToChat,
                navigateToCreateList = { ws -> navigateToCreateList(ws) },
            )
        }
    }
}
