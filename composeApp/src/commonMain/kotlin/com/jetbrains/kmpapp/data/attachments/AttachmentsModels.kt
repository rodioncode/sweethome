package com.jetbrains.kmpapp.data.attachments

import kotlinx.serialization.Serializable

@Serializable
data class UploadUrlRequest(
    val contentType: String,
    val sizeBytes: Long,
)

@Serializable
data class UploadUrlResponse(
    val uploadUrl: String,
    val key: String,
    val publicUrl: String,
    val expiresIn: Int,
)

@Serializable
data class ConfirmUploadRequest(
    val key: String,
    val contentType: String,
    val sizeBytes: Long,
)

@Serializable
data class Attachment(
    val id: String,
    val url: String,
)
