package com.jetbrains.kmpapp.data.goals

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

class KtorGoalsApi(
    private val apiClient: HttpClient,
    private val baseUrl: String,
) : GoalsApi {

    override suspend fun listGoals(workspaceId: String): Result<List<Goal>> = runCatching {
        val env: ApiEnvelope<GoalsWrapper> = apiClient.get("$baseUrl/workspaces/$workspaceId/goals").body()
        require(env.error == null) { env.error?.message ?: "goals_failed" }
        env.data?.goals ?: emptyList()
    }

    override suspend fun createGoal(workspaceId: String, request: CreateGoalRequest): Result<Goal> = runCatching {
        val env: ApiEnvelope<Goal> = apiClient.post("$baseUrl/workspaces/$workspaceId/goals") {
            contentType(ContentType.Application.Json); setBody(request)
        }.body()
        require(env.error == null) { env.error?.message ?: "create_goal_failed" }
        env.data ?: throw IllegalStateException("no_goal")
    }

    override suspend fun getGoal(goalId: String): Result<Goal> = runCatching {
        val env: ApiEnvelope<Goal> = apiClient.get("$baseUrl/goals/$goalId").body()
        require(env.error == null) { env.error?.message ?: "goal_failed" }
        env.data ?: throw IllegalStateException("no_goal")
    }

    override suspend fun patchGoal(goalId: String, request: PatchGoalRequest): Result<Goal> = runCatching {
        val env: ApiEnvelope<Goal> = apiClient.patch("$baseUrl/goals/$goalId") {
            contentType(ContentType.Application.Json); setBody(request)
        }.body()
        require(env.error == null) { env.error?.message ?: "patch_goal_failed" }
        env.data ?: throw IllegalStateException("no_goal")
    }

    override suspend fun archiveGoal(goalId: String): Result<Unit> = runCatching {
        val env: ApiEnvelope<EmptyResponse> = apiClient.delete("$baseUrl/goals/$goalId").body()
        require(env.error == null) { env.error?.message ?: "archive_goal_failed" }
        Unit
    }

    override suspend fun createStep(goalId: String, request: CreateStepRequest): Result<GoalStep> = runCatching {
        val env: ApiEnvelope<GoalStep> = apiClient.post("$baseUrl/goals/$goalId/steps") {
            contentType(ContentType.Application.Json); setBody(request)
        }.body()
        require(env.error == null) { env.error?.message ?: "create_step_failed" }
        env.data ?: throw IllegalStateException("no_step")
    }

    override suspend fun patchStep(goalId: String, stepId: String, request: PatchStepRequest): Result<GoalStep> = runCatching {
        val env: ApiEnvelope<GoalStep> = apiClient.patch("$baseUrl/goals/$goalId/steps/$stepId") {
            contentType(ContentType.Application.Json); setBody(request)
        }.body()
        require(env.error == null) { env.error?.message ?: "patch_step_failed" }
        env.data ?: throw IllegalStateException("no_step")
    }

    override suspend fun deleteStep(goalId: String, stepId: String): Result<Unit> = runCatching {
        val env: ApiEnvelope<EmptyResponse> = apiClient.delete("$baseUrl/goals/$goalId/steps/$stepId").body()
        require(env.error == null) { env.error?.message ?: "delete_step_failed" }
        Unit
    }

    override suspend fun completeStep(goalId: String, stepId: String): Result<GoalStep> = runCatching {
        val env: ApiEnvelope<GoalStep> = apiClient.post("$baseUrl/goals/$goalId/steps/$stepId/complete").body()
        require(env.error == null) { env.error?.message ?: "complete_step_failed" }
        env.data ?: throw IllegalStateException("no_step")
    }
}
