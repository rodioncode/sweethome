package com.jetbrains.kmpapp.media

// TODO: реализовать через PHPickerViewController.
actual fun createImagePicker(platformContext: Any?): ImagePicker =
    object : ImagePicker {
        override suspend fun pick(): PickedImage? = null
    }
