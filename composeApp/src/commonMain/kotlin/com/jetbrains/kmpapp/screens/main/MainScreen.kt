package com.jetbrains.kmpapp.screens.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.jetbrains.kmpapp.auth.AuthViewModel
import com.jetbrains.kmpapp.data.sync.SyncRepository
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

enum class MainTab { MY_LISTS, GROUPS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navigateToListDetail: (String) -> Unit,
    navigateToGroupDetail: (groupId: String, groupName: String) -> Unit,
    navigateToLinkEmail: () -> Unit,
) {
    val todoListsViewModel = koinViewModel<TodoListsViewModel>()
    val groupsViewModel = koinViewModel<GroupsViewModel>()
    val authViewModel = koinViewModel<AuthViewModel>()
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
    val groups by groupsViewModel.groups.collectAsStateWithLifecycle()
    val groupsError by groupsViewModel.error.collectAsStateWithLifecycle()
    val isGuest by groupsViewModel.isGuest.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(MainTab.MY_LISTS) }
    var showCreateListDialog by remember { mutableStateOf(false) }
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Reload personal lists when switching back to MY_LISTS tab
    LaunchedEffect(selectedTab) {
        if (selectedTab == MainTab.MY_LISTS) todoListsViewModel.refresh()
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (selectedTab == MainTab.MY_LISTS) "Мои списки" else "Группы")
                },
                actions = {
                    IconButton(onClick = { authViewModel.logout() }) {
                        Icon(Icons.Default.ExitToApp, "Выйти")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == MainTab.MY_LISTS,
                    onClick = { selectedTab = MainTab.MY_LISTS },
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text("Мои списки") },
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.GROUPS,
                    onClick = { selectedTab = MainTab.GROUPS },
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                    label = { Text("Группы") },
                )
            }
        },
        floatingActionButton = {
            when {
                selectedTab == MainTab.MY_LISTS ->
                    FloatingActionButton(onClick = { showCreateListDialog = true }) {
                        Icon(Icons.Default.Add, "Добавить список")
                    }
                selectedTab == MainTab.GROUPS && !isGuest ->
                    FloatingActionButton(onClick = { showCreateGroupDialog = true }) {
                        Icon(Icons.Default.Add, "Создать группу")
                    }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        when (selectedTab) {
            MainTab.MY_LISTS -> TodoListsContent(
                lists = lists,
                contentPadding = paddingValues,
                showCreateDialog = showCreateListDialog,
                onShowCreateDialog = { showCreateListDialog = it },
                onCreateList = { title -> todoListsViewModel.createList(title) },
                onListClick = navigateToListDetail,
            )
            MainTab.GROUPS -> GroupsContent(
                groups = groups,
                isGuest = isGuest,
                contentPadding = paddingValues,
                onGroupClick = { group -> navigateToGroupDetail(group.id, group.name) },
                navigateToLinkEmail = navigateToLinkEmail,
            )
        }
    }

    if (showCreateListDialog) {
        CreateListDialog(
            onDismiss = { showCreateListDialog = false },
            onConfirm = { title ->
                todoListsViewModel.createList(title)
                showCreateListDialog = false
            },
        )
    }

    if (showCreateGroupDialog) {
        CreateGroupDialog(
            onDismiss = { showCreateGroupDialog = false },
            onConfirm = { name ->
                groupsViewModel.createGroup(name)
                showCreateGroupDialog = false
            },
        )
    }
}
