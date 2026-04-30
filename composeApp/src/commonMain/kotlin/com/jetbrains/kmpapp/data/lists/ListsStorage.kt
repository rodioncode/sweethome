package com.jetbrains.kmpapp.data.lists

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface ListsStorage {
    suspend fun saveLists(lists: List<TodoList>)
    suspend fun getLists(): List<TodoList>
    suspend fun saveListWithItems(list: TodoList, items: List<TodoItem>)
    suspend fun getListWithItems(listId: String): Pair<TodoList, List<TodoItem>>?
    suspend fun clear()
    suspend fun applySync(updatedItems: List<TodoItem>, deletedIds: List<String>)
}

fun createListsStorage(platformContext: Any?): ListsStorage {
    val db = getListsDatabaseBuilder(platformContext)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.Default)
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
    return RoomListsStorage(db)
}

private class RoomListsStorage(
    private val db: ListsDatabase
) : ListsStorage {

    private val listDao = db.todoListDao()
    private val itemDao = db.todoItemDao()
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun saveLists(lists: List<TodoList>) = withContext(Dispatchers.Default) {
        listDao.insertAll(lists.map { it.toEntity() })
    }

    override suspend fun getLists(): List<TodoList> = withContext(Dispatchers.Default) {
        listDao.getAllLists().first().map { it.toDomain() }
    }

    override suspend fun saveListWithItems(list: TodoList, items: List<TodoItem>) = withContext(Dispatchers.Default) {
        listDao.insert(list.toEntity())
        itemDao.deleteByListId(list.id)
        if (items.isNotEmpty()) {
            itemDao.insertAll(items.map { it.toEntity() })
        }
    }

    override suspend fun getListWithItems(listId: String): Pair<TodoList, List<TodoItem>>? = withContext(Dispatchers.Default) {
        val listEntity = listDao.getListById(listId) ?: return@withContext null
        val itemEntities = itemDao.getItemsByListIdSync(listId)
        listEntity.toDomain() to itemEntities.map { it.toDomain() }
    }

    override suspend fun clear() = withContext(Dispatchers.Default) {
        listDao.deleteAll()
        itemDao.deleteAll()
    }

    override suspend fun applySync(updatedItems: List<TodoItem>, deletedIds: List<String>) = withContext(Dispatchers.Default) {
        for (id in deletedIds) {
            itemDao.deleteById(id)
        }
        for (item in updatedItems) {
            val existing = itemDao.getById(item.id)
            if (existing == null || item.version >= existing.version) {
                itemDao.insert(item.toEntity())
            }
        }
    }

    private fun TodoList.toEntity() = TodoListEntity(
        id = id,
        workspaceId = workspaceId,
        type = type,
        title = title,
        icon = icon,
        color = color,
        description = description,
        customTypeLabel = customTypeLabel,
        createdBy = createdBy,
        createdAt = createdAt,
        archivedAt = archivedAt,
        isPublic = isPublic,
        publicToken = publicToken,
    )

    private fun TodoListEntity.toDomain() = TodoList(
        id = id,
        workspaceId = workspaceId,
        type = type,
        title = title,
        icon = icon,
        color = color,
        description = description,
        customTypeLabel = customTypeLabel,
        createdBy = createdBy,
        createdAt = createdAt,
        archivedAt = archivedAt,
        isPublic = isPublic,
        publicToken = publicToken,
    )

    private fun TodoItem.toEntity() = TodoItemEntity(
        id = id,
        listId = listId,
        title = title,
        note = note,
        sortOrder = sortOrder,
        isDone = isDone,
        doneAt = doneAt,
        createdBy = createdBy,
        createdAt = createdAt,
        updatedAt = updatedAt,
        shoppingJson = shopping?.let { json.encodeToString(it) },
        choreScheduleJson = choreSchedule?.let { json.encodeToString(it) },
        mediaJson = media?.let { json.encodeToString(it) },
        wishlistJson = wishlist?.let { json.encodeToString(it) },
        assignedTo = assignedTo,
        dueAt = dueAt,
        isFavorite = isFavorite,
        priority = priority,
        reward = reward,
        version = version,
    )

    private fun TodoItemEntity.toDomain() = TodoItem(
        id = id,
        listId = listId,
        title = title,
        note = note,
        sortOrder = sortOrder,
        isDone = isDone,
        doneAt = doneAt,
        createdBy = createdBy,
        createdAt = createdAt,
        updatedAt = updatedAt,
        shopping = shoppingJson?.let { json.decodeFromString<ShoppingItemFields>(it) },
        choreSchedule = choreScheduleJson?.let { json.decodeFromString<ChoreSchedule>(it) },
        media = mediaJson?.let { json.decodeFromString<MediaItemFields>(it) },
        wishlist = wishlistJson?.let { json.decodeFromString<WishlistItemFields>(it) },
        assignedTo = assignedTo,
        dueAt = dueAt,
        isFavorite = isFavorite,
        priority = priority,
        reward = reward,
        version = version,
    )
}
