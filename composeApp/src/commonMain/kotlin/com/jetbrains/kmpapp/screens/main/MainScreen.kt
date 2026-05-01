package com.jetbrains.kmpapp.screens.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import com.jetbrains.kmpapp.screens.templates.TemplatesContent
import com.jetbrains.kmpapp.screens.todo.CreateListDialog
import com.jetbrains.kmpapp.screens.todo.TodoListDetailScreen
import com.jetbrains.kmpapp.screens.todo.TodoListsContent
import com.jetbrains.kmpapp.screens.todo.TodoListsViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

enum class MainTab { DASHBOARD, HOME, LISTS, TEMPLATES, GROUPS }

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
    navigateToChat: (workspaceId: String, title: String, memberCount: Int) -> Unit = { _, _, _ -> },
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

    var selectedTab by rememberSaveable { mutableStateOf(MainTab.DASHBOARD) }
    var showCreateListDialog by remember { mutableStateOf(false) }
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

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
                    selectedTab = MainTab.GROUPS
                    groupsNavController.navigate(GroupsDetail(event.groupId, event.groupName)) {
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    LaunchedEffect(pendingInviteToken) {
        pendingInviteToken?.let {
            selectedTab = MainTab.GROUPS
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == MainTab.DASHBOARD,
                    onClick = { selectedTab = MainTab.DASHBOARD },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Главная") },
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.HOME,
                    onClick = { selectedTab = MainTab.HOME },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Мой дом") },
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.LISTS,
                    onClick = { selectedTab = MainTab.LISTS },
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text("Списки") },
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.TEMPLATES,
                    onClick = { selectedTab = MainTab.TEMPLATES },
                    icon = { Icon(Icons.Default.Star, contentDescription = null) },
                    label = { Text("Шаблоны") },
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.GROUPS,
                    onClick = { selectedTab = MainTab.GROUPS },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Группы") },
                )
            }
        },
        floatingActionButton = {
            val listsBackStack by listsNavController.currentBackStackEntryAsState()
            val isInListDetail = listsBackStack?.destination?.hasRoute(ListsDetail::class) == true
            if (selectedTab == MainTab.LISTS && !isInListDetail) {
                FloatingActionButton(onClick = { showCreateListDialog = true }) {
                    Icon(Icons.Default.Add, "Добавить список")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        when (selectedTab) {
            MainTab.DASHBOARD -> HomeContent(
                contentPadding = paddingValues,
                navigateToListDetail = navigateToListDetail,
                onCreateList = { showCreateListDialog = true },
                onNavigateToHome = { selectedTab = MainTab.HOME },
                onNavigateToGroups = { selectedTab = MainTab.GROUPS },
                navigateToProfile = navigateToProfile,
            )
            MainTab.HOME -> FamilyContent(
                contentPadding = paddingValues,
                onSpaceClick = { groupId, name ->
                    selectedTab = MainTab.GROUPS
                    groupsNavController.navigate(GroupsDetail(groupId, name)) { launchSingleTop = true }
                },
                onListClick = { listId ->
                    selectedTab = MainTab.LISTS
                    listsNavController.navigate(ListsDetail(listId)) { launchSingleTop = true }
                },
                navigateToGamification = navigateToGamification,
                navigateToShop = navigateToShop,
            )
            MainTab.LISTS -> ListsTabNavHost(
                navController = listsNavController,
                paddingValues = paddingValues,
                lists = lists,
                groups = allGroups,
                showCreateDialog = showCreateListDialog,
                onShowCreateDialog = { showCreateListDialog = it },
                onCreateList = { title, type, icon, color, workspaceId ->
                    todoListsViewModel.createList(title, type, workspaceId, icon, color)
                },
                isGuest = isGuest,
                navigateToLinkEmail = navigateToLinkEmail,
            )
            MainTab.TEMPLATES -> TemplatesContent(
                contentPadding = paddingValues,
            )
            MainTab.GROUPS -> GroupsTabNavHost(
                navController = groupsNavController,
                paddingValues = paddingValues,
                groupSpaces = groupSpaces,
                isGuest = isGuest,
                navigateToLinkEmail = navigateToLinkEmail,
                navigateToJoinByCode = navigateToJoinByCode,
                onCreateGroup = if (!isGuest) ({ showCreateGroupDialog = true }) else null,
                navigateToListDetail = { listId ->
                    selectedTab = MainTab.LISTS
                    listsNavController.navigate(ListsDetail(listId)) { launchSingleTop = true }
                },
                navigateToChat = navigateToChat,
            )
        }
    }

    if (showCreateListDialog) {
        CreateListDialog(
            groups = allGroups,
            onDismiss = { showCreateListDialog = false },
            onConfirm = { title, type, icon, color, workspaceId ->
                todoListsViewModel.createList(title, type, workspaceId, icon, color)
                showCreateListDialog = false
            },
        )
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
private fun ListsTabNavHost(
    navController: NavHostController,
    paddingValues: PaddingValues,
    lists: List<com.jetbrains.kmpapp.data.lists.TodoList>,
    groups: List<com.jetbrains.kmpapp.data.groups.Group>,
    showCreateDialog: Boolean,
    onShowCreateDialog: (Boolean) -> Unit,
    onCreateList: (String, String, String?, String?, String) -> Unit,
    isGuest: Boolean,
    navigateToLinkEmail: () -> Unit,
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
                showCreateDialog = showCreateDialog,
                onShowCreateDialog = onShowCreateDialog,
                onCreateList = onCreateList,
                onListClick = { listId -> navController.navigate(ListsDetail(listId)) },
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
            )
        }
    }
}
