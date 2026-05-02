package com.jetbrains.kmpapp.data.wishlist

import com.jetbrains.kmpapp.auth.ApiEnvelope
import com.jetbrains.kmpapp.auth.EmptyResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

class KtorPublicWishlistApi(
    private val httpClient: HttpClient,   // unauth (no bearer)
    private val baseUrl: String,
) : PublicWishlistApi {

    override suspend fun getPublic(token: String): Result<PublicWishlist> = runCatching {
        try {
            val envelope: ApiEnvelope<PublicWishlist> = httpClient.get("$baseUrl/public/wishlists/$token").body()
            require(envelope.error == null) { envelope.error?.message ?: "wishlist_failed" }
            require(envelope.data != null) { "no_data" }
            envelope.data
        } catch (e: ResponseException) {
            if (e.response.status == HttpStatusCode.NotFound) throw WishlistNotFoundException()
            throw e
        }
    }

    override suspend fun claim(token: String, itemId: String, request: ClaimRequest): Result<Unit> = runCatching {
        try {
            val envelope: ApiEnvelope<EmptyResponse> = httpClient.post("$baseUrl/public/wishlists/$token/items/$itemId/claim") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
            when (envelope.error?.code) {
                "already_claimed" -> throw AlreadyClaimedException()
            }
            require(envelope.error == null) { envelope.error?.message ?: "claim_failed" }
            Unit
        } catch (e: ResponseException) {
            when (e.response.status) {
                HttpStatusCode.Conflict -> throw AlreadyClaimedException()
                HttpStatusCode.NotFound -> throw WishlistNotFoundException()
                else -> throw e
            }
        }
    }
}
