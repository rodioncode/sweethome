package com.jetbrains.kmpapp.data.groups

import com.jetbrains.kmpapp.auth.ApiEnvelope
import com.jetbrains.kmpapp.auth.EmptyResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class KtorGroupsApi(
    private val apiClient: HttpClient,
    private val baseUrl: String,
    private val tokenProvider: () -> String?,
) : GroupsApi {

    private fun requireToken(): String =
        tokenProvider() ?: throw IllegalStateException("Требуется авторизация. Войдите в аккаунт.")

    override suspend fun getGroups(): Result<List<Group>> = runCatching {
        val envelope: ApiEnvelope<GroupsWrapper> = apiClient.get("$baseUrl/groups") {
            header("Authorization", "Bearer ${requireToken()}")
        }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        require(envelope.data != null) { "No data in response" }
        envelope.data.groups
    }

    override suspend fun createGroup(request: CreateGroupRequest): Result<Group> = runCatching {
        val envelope: ApiEnvelope<Group> = apiClient.post("$baseUrl/groups") {
            header("Authorization", "Bearer ${requireToken()}")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        require(envelope.data != null) { "No data in response" }
        envelope.data
    }

    override suspend fun createInvite(groupId: String): Result<Invite> = runCatching {
        val envelope: ApiEnvelope<Invite> = apiClient.post("$baseUrl/groups/$groupId/invites") {
            header("Authorization", "Bearer ${requireToken()}")
        }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        require(envelope.data != null) { "No data in response" }
        envelope.data
    }

    override suspend fun acceptInvite(token: String): Result<AcceptInviteResponse> = runCatching {
        val envelope: ApiEnvelope<AcceptInviteResponse> =
            apiClient.post("$baseUrl/invites/$token/accept") {
                header("Authorization", "Bearer ${requireToken()}")
            }.body()
        if (envelope.error?.code == "email_required") throw EmailRequiredException()
        if (envelope.error?.code == "invalid_invite") throw InvalidInviteException()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        require(envelope.data != null) { "No data in response" }
        envelope.data
    }

    override suspend fun deleteGroup(groupId: String): Result<Unit> = runCatching {
        val envelope: ApiEnvelope<EmptyResponse> = apiClient.delete("$baseUrl/groups/$groupId") {
            header("Authorization", "Bearer ${requireToken()}")
        }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        Unit
    }

    override suspend fun removeMember(groupId: String, userId: String): Result<Unit> = runCatching {
        val envelope: ApiEnvelope<EmptyResponse> =
            apiClient.delete("$baseUrl/groups/$groupId/members/$userId") {
                header("Authorization", "Bearer ${requireToken()}")
            }.body()
        if (envelope.error?.code == "owner_cannot_leave") throw OwnerCannotLeaveException()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        Unit
    }

    override suspend fun transferOwnership(
        groupId: String,
        request: TransferOwnershipRequest,
    ): Result<Unit> = runCatching {
        val envelope: ApiEnvelope<EmptyResponse> =
            apiClient.post("$baseUrl/groups/$groupId/transfer-ownership") {
                header("Authorization", "Bearer ${requireToken()}")
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        Unit
    }
}
