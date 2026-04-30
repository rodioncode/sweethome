package com.jetbrains.kmpapp.data.groups

import com.jetbrains.kmpapp.auth.ApiEnvelope
import com.jetbrains.kmpapp.auth.EmptyResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class KtorGroupsApi(
    private val apiClient: HttpClient,
    private val baseUrl: String,
) : GroupsApi {

    override suspend fun getWorkspaces(): Result<List<Group>> = runCatching {
        val envelope: ApiEnvelope<WorkspacesWrapper> = apiClient.get("$baseUrl/workspaces").body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        require(envelope.data != null) { "No data in response" }
        envelope.data.workspaces
    }

    override suspend fun createWorkspace(request: CreateWorkspaceRequest): Result<Group> = runCatching {
        val envelope: ApiEnvelope<Group> = apiClient.post("$baseUrl/workspaces") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        require(envelope.data != null) { "No data in response" }
        envelope.data
    }

    override suspend fun getWorkspace(workspaceId: String): Result<Group> = runCatching {
        val envelope: ApiEnvelope<Group> = apiClient.get("$baseUrl/workspaces/$workspaceId").body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        require(envelope.data != null) { "No data in response" }
        envelope.data
    }

    override suspend fun patchWorkspace(workspaceId: String, request: PatchWorkspaceRequest): Result<Group> = runCatching {
        val envelope: ApiEnvelope<Group> = apiClient.patch("$baseUrl/workspaces/$workspaceId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        require(envelope.data != null) { "No data in response" }
        envelope.data
    }

    override suspend fun deleteWorkspace(workspaceId: String): Result<Unit> = runCatching {
        val envelope: ApiEnvelope<EmptyResponse> = apiClient.delete("$baseUrl/workspaces/$workspaceId").body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        Unit
    }

    override suspend fun getWorkspaceMembers(workspaceId: String): Result<List<GroupMember>> = runCatching {
        val envelope: ApiEnvelope<WorkspaceMembersWrapper> =
            apiClient.get("$baseUrl/workspaces/$workspaceId/members-info").body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        require(envelope.data != null) { "No data in response" }
        envelope.data.members
    }

    override suspend fun removeMember(workspaceId: String, userId: String): Result<Unit> = runCatching {
        val envelope: ApiEnvelope<EmptyResponse> =
            apiClient.delete("$baseUrl/workspaces/$workspaceId/members/$userId").body()
        if (envelope.error?.code == "owner_cannot_leave") throw OwnerCannotLeaveException()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        Unit
    }

    override suspend fun transferOwnership(
        workspaceId: String,
        request: TransferOwnershipRequest,
    ): Result<Unit> = runCatching {
        val envelope: ApiEnvelope<EmptyResponse> =
            apiClient.post("$baseUrl/workspaces/$workspaceId/transfer-ownership") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        Unit
    }

    override suspend fun createInviteCode(workspaceId: String): Result<Invite> = runCatching {
        val envelope: ApiEnvelope<Invite> =
            apiClient.post("$baseUrl/workspaces/$workspaceId/invite-code").body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        require(envelope.data != null) { "No data in response" }
        envelope.data
    }

    override suspend fun joinByCode(token: String): Result<Group> = runCatching {
        val envelope: ApiEnvelope<Group> =
            apiClient.post("$baseUrl/workspaces/join-by-code") {
                contentType(ContentType.Application.Json)
                setBody(JoinByCodeRequest(token))
            }.body()
        when (envelope.error?.code) {
            "email_required" -> throw EmailRequiredException()
            "invalid_token" -> throw InvalidInviteException()
            "invite_expired" -> throw InviteExpiredException()
            "invite_used" -> throw InvalidInviteException()
        }
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        require(envelope.data != null) { "No data in response" }
        envelope.data
    }
}
