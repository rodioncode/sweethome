package com.jetbrains.kmpapp.data.attachments

interface AttachmentsApi {
    suspend fun requestUpload(itemId: String, contentType: String, sizeBytes: Long): Result<UploadUrlResponse>
    suspend fun uploadBytes(uploadUrl: String, contentType: String, bytes: ByteArray): Result<Unit>
    suspend fun confirmUpload(itemId: String, key: String, contentType: String, sizeBytes: Long): Result<Attachment>
}
