package com.jetbrains.kmpapp.data.attachments

import com.jetbrains.kmpapp.auth.ApiEnvelope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class KtorAttachmentsApi(
    private val apiClient: HttpClient,
    private val uploadClient: HttpClient,   // без bearer; для прямого PUT в S3
    private val baseUrl: String,
) : AttachmentsApi {

    override suspend fun requestUpload(itemId: String, contentType: String, sizeBytes: Long): Result<UploadUrlResponse> = runCatching {
        val envelope: ApiEnvelope<UploadUrlResponse> = apiClient.post("$baseUrl/items/$itemId/upload-url") {
            contentType(ContentType.Application.Json)
            setBody(UploadUrlRequest(contentType = contentType, sizeBytes = sizeBytes))
        }.body()
        require(envelope.error == null) { envelope.error?.message ?: "upload_url_failed" }
        require(envelope.data != null) { "no_upload_url" }
        envelope.data
    }

    override suspend fun uploadBytes(uploadUrl: String, contentType: String, bytes: ByteArray): Result<Unit> = runCatching {
        val response = uploadClient.put(uploadUrl) {
            contentType(ContentType.parse(contentType))
            setBody(bytes)
        }
        require(response.status.isSuccess()) { "s3_put_failed_${response.status.value}" }
        Unit
    }

    override suspend fun confirmUpload(itemId: String, key: String, contentType: String, sizeBytes: Long): Result<Attachment> = runCatching {
        val envelope: ApiEnvelope<Attachment> = apiClient.post("$baseUrl/items/$itemId/confirm-upload") {
            contentType(ContentType.Application.Json)
            setBody(ConfirmUploadRequest(key = key, contentType = contentType, sizeBytes = sizeBytes))
        }.body()
        require(envelope.error == null) { envelope.error?.message ?: "confirm_failed" }
        require(envelope.data != null) { "no_attachment" }
        envelope.data
    }
}
