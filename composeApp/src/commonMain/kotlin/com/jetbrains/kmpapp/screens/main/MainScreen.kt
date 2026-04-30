package com.jetbrains.kmpapp.screens.main

import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jetbrains.kmpapp.data.sync.SyncRepository
import com.jetbrains.kmpapp.screens.family.FamilyContent
import com.jetbrains.kmpapp.screens.home.HomeContent
import com.jetbrains.kmpapp.screens.templates.TemplatesContent
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import com.jetbrains.kmpapp.screens.groups.GroupsContent
import com.jetbrains.kmpapp.screens.groups.GroupsViewModel
import com.jetbrains.kmpapp.screens.groups.GroupsUiEvent
import com.jetbrains.kmpapp.screens.groups.CreateGroupDialog
import com.jetbrains.kmpapp.screens.todo.CreateListDialog
import com.jetbrains.kmpapp.screens.todo.TodoListsContent
import com.jetbrains.kmpapp.screens.todo.TodoListsViewModel
import org.koin.compose.viewmodel.koinViewModel

enum class MainTab { DASHBOARD, HOME, LISTS, TEMPLATES, GROUPS }

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
    pendingInviteToken: String? = null,
) {
    val todoListsViewModel = koinViewModel<TodoListsViewModel>()
    val groupsViewModel = koinViewModel<GroupsViewModel>()
    val syncRepository = koinInject<SyncRepository>()
    val coroutineScope = rememberCoroutineScope()
    val lifecycle = LocalLifecycleOwner.current.lifecycle

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

    var selectedTab by remember { mutableStateOf(MainTab.DASHBOARD) }
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
                is GroupsUiEvent.NavigateToGroup ->
                    navigateToGroupDetail(event.groupId, event.groupName)
            }
        }
    }

    LaunchedEffect(pendingInviteToken) {
        pendingInviteToken?.let {
            selectedTab = MainTab.GROUPS
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (selectedTab) {
                            MainTab.DASHBOARD -> "Главная"
                            MainTab.HOME -> "Мой дом"
                            MainTab.LISTS -> "Мои списки"
                            MainTab.TEMPLATES -> "Шаблоны"
                            MainTab.GROUPS -> "Группы"
                        }
                    )
                },
            )
        },
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
            if (selectedTab == MainTab.LISTS) {
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
                onSpaceClick = navigateToGroupDetail,
                onListClick = navigateToListDetail,
                navigateToGamification = navigateToGamification,
                navigateToShop = navigateToShop,
                navigateToJoinByCode = navigateToJoinByCode,
            )
            MainTab.LISTS -> TodoListsContent(
                lists = lists,
                groups = allGroups,
                contentPadding = paddingValues,
                showCreateDialog = showCreateListDialog,
                onShowCreateDialog = { showCreateListDialog = it },
                onCreateList = { title, type, icon, color, scope, groupId ->
                    todoListsViewModel.createList(title, type, icon, color, scope, groupId)
                },
                onListClick = navigateToListDetail,
                isGuest = isGuest,
                navigateToLinkEmail = navigateToLinkEmail,
            )
            MainTab.TEMPLATES -> TemplatesContent(
                contentPadding = paddingValues,
            )
            MainTab.GROUPS -> GroupsContent(
                groups = groupSpaces,
                isGuest = isGuest,
                contentPadding = paddingValues,
                onGroupClick = { group -> navigateToGroupDetail(group.id, group.name) },
                navigateToLinkEmail = navigateToLinkEmail,
                navigateToJoinByCode = navigateToJoinByCode,
                onCreateGroup = if (!isGuest) ({ showCreateGroupDialog = true }) else null,
            )
        }
    }

    if (showCreateListDialog) {
        CreateListDialog(
            groups = allGroups,
            onDismiss = { showCreateListDialog = false },
            onConfirm = { title, type, icon, color, scope, groupId ->
                todoListsViewModel.createList(title, type, icon, color, scope, groupId)
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
