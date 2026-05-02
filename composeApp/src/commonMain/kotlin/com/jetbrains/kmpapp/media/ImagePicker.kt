package com.jetbrains.kmpapp.media

data class PickedImage(
    val bytes: ByteArray,
    val mimeType: String,
)

interface ImagePicker {
    suspend fun pick(): PickedImage?
}

expect fun createImagePicker(platformContext: Any?): ImagePicker
