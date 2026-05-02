package com.jetbrains.kmpapp.data.wishlist

interface PublicWishlistApi {
    suspend fun getPublic(token: String): Result<PublicWishlist>
    suspend fun claim(token: String, itemId: String, request: ClaimRequest): Result<Unit>
}
