package com.jetbrains.kmpapp.media

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

@Composable
actual fun rememberImagePicker(): ImagePicker {
    val context = LocalContext.current
    val pending = remember { mutableStateOf<((PickedImage?) -> Unit)?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        val cb = pending.value ?: return@rememberLauncherForActivityResult
        pending.value = null
        if (uri == null) {
            cb(null)
            return@rememberLauncherForActivityResult
        }
        // Считываем bytes/mime в callback (короткое блокирование на main; для больших фото
        // разумнее декодировать в фоне — здесь polish-функция пути загрузки делает это сама).
        try {
            val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            cb(if (bytes != null) PickedImage(bytes, mime) else null)
        } catch (_: Throwable) {
            cb(null)
        }
    }
    return remember(launcher) {
        object : ImagePicker {
            override suspend fun pick(): PickedImage? = withContext(Dispatchers.Main) {
                suspendCancellableCoroutine { cont ->
                    pending.value = { result -> if (cont.isActive) cont.resume(result) }
                    launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            }
        }
    }
}
