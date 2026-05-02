package com.jetbrains.kmpapp.data.wishlist

import com.jetbrains.kmpapp.data.lists.TodoItem
import com.jetbrains.kmpapp.data.lists.TodoList
import kotlinx.serialization.Serializable

@Serializable
data class PublicWishlist(
    val list: TodoList,
    val items: List<TodoItem>,
)

@Serializable
data class ClaimRequest(
    val name: String,
    val isAnonymous: Boolean = false,
    val userId: String? = null,
)

class AlreadyClaimedException : Exception("already_claimed")
class WishlistNotFoundException : Exception("wishlist_not_found")
