package com.jetbrains.kmpapp.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import kotlin.coroutines.resume

@Composable
actual fun rememberImagePicker(): ImagePicker = remember {
    object : ImagePicker {
        override suspend fun pick(): PickedImage? = suspendCancellableCoroutine { cont ->
            val rootVc: UIViewController? = UIApplication.sharedApplication.keyWindow?.rootViewController
            if (rootVc == null) {
                if (cont.isActive) cont.resume(null)
                return@suspendCancellableCoroutine
            }
            val config = PHPickerConfiguration().apply {
                setSelectionLimit(1)
                setFilter(PHPickerFilter.imagesFilter)
            }
            val picker = PHPickerViewController(configuration = config)
            val delegate = PhPickerDelegate { result ->
                if (cont.isActive) cont.resume(result)
            }
            // Удерживаем delegate в objc_setAssociatedObject через свойство контроллера —
            // здесь храним через локальную ссылку: захват в Kotlin closure достаточен,
            // т.к. picker удерживается до dismiss.
            picker.setDelegate(delegate)
            // Чтобы delegate не был собран GC раньше времени, привязываем к picker.
            associatedDelegate = delegate
            rootVc.presentViewController(picker, animated = true, completion = null)
        }
    }
}

// Простой holder, чтобы delegate жил до конца модального показа.
private var associatedDelegate: Any? = null

private class PhPickerDelegate(
    private val onResult: (PickedImage?) -> Unit,
) : NSObject(), PHPickerViewControllerDelegateProtocol {

    @OptIn(ExperimentalForeignApi::class)
    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        picker.dismissViewControllerAnimated(true, completion = null)
        val result = didFinishPicking.firstOrNull() as? PHPickerResult
        if (result == null) {
            associatedDelegate = null
            onResult(null)
            return
        }
        val provider = result.itemProvider
        // Получаем сырые bytes по UTI public.image — для большинства камер/галерей это JPEG/HEIC.
        provider.loadDataRepresentationForTypeIdentifier("public.image") { data: NSData?, _ ->
            val bytes: ByteArray? = data?.let { d ->
                val len = d.length.toInt()
                if (len <= 0) null
                else ByteArray(len).also { arr ->
                    arr.usePinned { pinned ->
                        platform.posix.memcpy(pinned.addressOf(0), d.bytes, d.length)
                    }
                }
            }
            associatedDelegate = null
            onResult(if (bytes != null) PickedImage(bytes, "image/jpeg") else null)
        }
    }
}
