package com.jetbrains.kmpapp.data.groups

import com.jetbrains.kmpapp.auth.ApiEnvelope
import com.jetbrains.kmpapp.auth.EmptyResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class KtorGroupsApi(
    private val apiClient: HttpClient,
    private val baseUrl: String,
) : GroupsApi {

    override suspend fun getGroups(): Result<List<GroupDTO>> = runCatching {
        val envelope: ApiEnvelope<GroupsWrapper> = apiClient.get("$baseUrl/groups").body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        envelope.data?.groups ?: emptyList()
    }

    override suspend fun createGroup(request: CreateGroupRequest): Result<GroupDTO> = runCatching {
        val envelope: ApiEnvelope<GroupDTO> = apiClient.post("$baseUrl/groups") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        requireNotNull(envelope.data) { "No data in response" }
    }

    override suspend fun createInvite(groupId: String): Result<InviteDTO> = runCatching {
        val envelope: ApiEnvelope<InviteDTO> = apiClient.post("$baseUrl/groups/$groupId/invites").body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        requireNotNull(envelope.data) { "No data in response" }
    }

    override suspend fun acceptInvite(token: String): Result<AcceptInviteResponse> = runCatching {
        val envelope: ApiEnvelope<AcceptInviteResponse> = apiClient.post("$baseUrl/invites/$token/accept").body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        requireNotNull(envelope.data) { "No data in response" }
    }

    override suspend fun transferOwnership(
        groupId: String,
        request: TransferOwnershipRequest,
    ): Result<Unit> = runCatching {
        val envelope: ApiEnvelope<EmptyResponse> =
            apiClient.post("$baseUrl/groups/$groupId/transfer-ownership") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
    }

    override suspend fun deleteGroup(groupId: String): Result<Unit> = runCatching {
        val envelope: ApiEnvelope<EmptyResponse> = apiClient.delete("$baseUrl/groups/$groupId").body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
    }

    override suspend fun removeMember(groupId: String, userId: String): Result<Unit> = runCatching {
        val envelope: ApiEnvelope<EmptyResponse> =
            apiClient.delete("$baseUrl/groups/$groupId/members/$userId").body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
    }
}
