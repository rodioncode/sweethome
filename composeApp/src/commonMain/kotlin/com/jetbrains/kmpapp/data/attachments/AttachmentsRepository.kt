package com.jetbrains.kmpapp.data.attachments

class AttachmentsRepository(
    private val api: AttachmentsApi,
) {
    suspend fun upload(itemId: String, contentType: String, bytes: ByteArray): Result<Attachment> {
        val sizeBytes = bytes.size.toLong()
        val urlResp = api.requestUpload(itemId, contentType, sizeBytes).getOrElse { return Result.failure(it) }
        api.uploadBytes(urlResp.uploadUrl, contentType, bytes).getOrElse { return Result.failure(it) }
        return api.confirmUpload(itemId, urlResp.key, contentType, sizeBytes)
    }
}
