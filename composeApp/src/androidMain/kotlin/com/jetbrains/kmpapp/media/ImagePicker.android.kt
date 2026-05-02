package com.jetbrains.kmpapp.media

// TODO: реализовать через ActivityResultContracts.PickVisualMedia.
// Сейчас стаб — возвращает null, чтобы UI-кнопка была видна, но без эффекта.
actual fun createImagePicker(platformContext: Any?): ImagePicker =
    object : ImagePicker {
        override suspend fun pick(): PickedImage? = null
    }
