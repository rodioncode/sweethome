package com.jetbrains.kmpapp.screens.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetbrains.kmpapp.data.categories.CategoriesRepository
import com.jetbrains.kmpapp.data.categories.Category
import com.jetbrains.kmpapp.data.groups.GroupMember
import com.jetbrains.kmpapp.data.groups.GroupsRepository
import com.jetbrains.kmpapp.data.lists.ChoreSchedule
import com.jetbrains.kmpapp.data.lists.ListsRepository
import com.jetbrains.kmpapp.data.lists.ShoppingItemFields
import com.jetbrains.kmpapp.data.lists.TodoItem
import com.jetbrains.kmpapp.data.lists.TodoList
import com.jetbrains.kmpapp.data.suggestions.ChoreTemplate
import com.jetbrains.kmpapp.data.suggestions.SuggestionsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TodoListDetailViewModel(
    private val listsRepository: ListsRepository,
    private val categoriesRepository: CategoriesRepository,
    private val suggestionsRepository: SuggestionsRepository,
    private val groupsRepository: GroupsRepository,
) : ViewModel() {

    val listWithItems: StateFlow<Pair<TodoList, List<TodoItem>>?> =
        listsRepository.currentListWithItems
    val error: StateFlow<String?> = listsRepository.error

    val memberNames: StateFlow<Map<String, String>> = groupsRepository.members
        .map { members -> members.associate { it.userId to (it.displayName ?: it.userId) } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val groupMembers: StateFlow<List<GroupMember>> = groupsRepository.members

    val categories: StateFlow<List<Category>> = categoriesRepository.categories
    val choreTemplates: StateFlow<List<ChoreTemplate>> = suggestionsRepository.choreTemplates
    val frequentItems: StateFlow<List<TodoItem>> = suggestionsRepository.frequentItems

    fun loadList(listId: String) {
        viewModelScope.launch {
            listsRepository.loadListWithItems(listId)
            listsRepository.currentListWithItems.value?.first?.let { list ->
                when (list.type) {
                    "shopping" -> {
                        launch { categoriesRepository.loadCategories("shopping") }
                        launch { suggestionsRepository.loadFrequentItems(listId) }
                    }
                    "home_chores" -> {
                        launch { categoriesRepository.loadCategories("chore") }
                        launch { suggestionsRepository.loadChoreTemplates() }
                    }
                }
            }
        }
    }

    fun addItem(
        listId: String,
        title: String,
        note: String? = null,
        dueAt: String? = null,
        isFavorite: Boolean = false,
        assignedTo: String? = null,
        shopping: ShoppingItemFields? = null,
        choreSchedule: ChoreSchedule? = null,
    ) {
        viewModelScope.launch {
            listsRepository.createItem(
                listId = listId,
                title = title,
                note = note?.takeIf { it.isNotBlank() },
                assignedTo = assignedTo?.takeIf { it.isNotBlank() },
                dueAt = dueAt?.takeIf { it.isNotBlank() },
                isFavorite = isFavorite.takeIf { it },
                shopping = shopping,
                choreSchedule = choreSchedule,
            )
        }
    }

    fun updateItem(
        item: TodoItem,
        title: String,
        note: String,
        dueAt: String,
        isFavorite: Boolean,
        assignedTo: String? = null,
        shopping: ShoppingItemFields? = null,
        choreSchedule: ChoreSchedule? = null,
    ) {
        viewModelScope.launch {
            listsRepository.updateItem(
                itemId = item.id,
                title = title.takeIf { it.isNotBlank() && it != item.title },
                note = note.takeIf { it.isNotBlank() },
                assignedTo = assignedTo,
                dueAt = dueAt.takeIf { it.isNotBlank() },
                isFavorite = isFavorite.takeIf { it != item.isFavorite },
                shopping = shopping,
                choreSchedule = choreSchedule,
            )
        }
    }

    fun createCategory(scope: String, name: String) {
        viewModelScope.launch {
            categoriesRepository.createCategory(scope, name)
        }
    }

    fun toggleItem(item: TodoItem) {
        viewModelScope.launch {
            listsRepository.toggleItemDone(item)
        }
    }

    fun deleteItem(item: TodoItem) {
        viewModelScope.launch {
            listsRepository.deleteItem(item)
        }
    }

    fun clearError() {
        listsRepository.clearError()
    }

    override fun onCleared() {
        super.onCleared()
        listsRepository.clearCurrentList()
        categoriesRepository.clear()
        suggestionsRepository.clear()
    }
}
