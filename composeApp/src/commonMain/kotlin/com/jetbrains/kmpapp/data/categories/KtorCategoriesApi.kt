package com.jetbrains.kmpapp.data.categories

import com.jetbrains.kmpapp.auth.ApiEnvelope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class KtorCategoriesApi(
    private val apiClient: HttpClient,
    private val baseUrl: String,
) : CategoriesApi {

    override suspend fun getCategories(scope: String): Result<List<Category>> = runCatching {
        val envelope: ApiEnvelope<CategoriesWrapper> =
            apiClient.get("$baseUrl/categories?scope=$scope").body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        require(envelope.data != null) { "No data in response" }
        envelope.data.categories
    }

    override suspend fun createCategory(request: CreateCategoryRequest): Result<Category> = runCatching {
        val envelope: ApiEnvelope<Category> = apiClient.post("$baseUrl/categories") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        require(envelope.data != null) { "No data in response" }
        envelope.data
    }
}
