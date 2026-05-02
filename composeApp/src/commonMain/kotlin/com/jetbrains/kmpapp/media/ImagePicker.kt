package com.jetbrains.kmpapp.media

import androidx.compose.runtime.Composable

data class PickedImage(
    val bytes: ByteArray,
    val mimeType: String,
)

interface ImagePicker {
    suspend fun pick(): PickedImage?
}

/** Возвращает picker, привязанный к текущему Composable scope. На Android регистрирует ActivityResult-launcher. */
@Composable
expect fun rememberImagePicker(): ImagePicker
