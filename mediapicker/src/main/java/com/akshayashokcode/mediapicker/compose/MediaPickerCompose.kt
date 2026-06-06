package com.akshayashokcode.mediapicker.compose

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.activity.result.contract.ActivityResultContracts.OpenMultipleDocuments
import androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.akshayashokcode.mediapicker.model.MediaItem
import com.akshayashokcode.mediapicker.model.MediaPickerResult
import com.akshayashokcode.mediapicker.model.MediaType
import com.akshayashokcode.mediapicker.util.MediaItemFactory
import com.akshayashokcode.mediakitcore.ExperimentalMediaKitApi
import com.akshayashokcode.mediapicker.util.MimeTypeUtils

/**
 * Compose-native media picker. Can be called anywhere inside a `@Composable` — no
 * `onCreate`-before-`setContent` restriction.
 *
 * All four contracts (visual single/multiple and document single/multiple) are registered
 * eagerly; the correct one is selected at [MediaPickerHandle.launch] time based on the
 * current [mediaTypes] and [allowMultiple] values.
 *
 * @param mediaTypes One or more [MediaType] values. Defaults to [MediaType.All].
 * @param allowMultiple Whether to allow selecting multiple items.
 * @param onResult Invoked on the main thread with the picker result.
 */
@ExperimentalMediaKitApi
@Composable
fun rememberMediaPicker(
    vararg mediaTypes: MediaType = arrayOf(MediaType.All),
    allowMultiple: Boolean = false,
    onResult: (MediaPickerResult) -> Unit
): MediaPickerHandle {
    val context = LocalContext.current
    val currentOnResult = rememberUpdatedState(onResult)
    val currentTypes = rememberUpdatedState(mediaTypes.toList())
    val currentAllowMultiple = rememberUpdatedState(allowMultiple)

    fun buildItem(uri: Uri): MediaItem? = MediaItemFactory.build(context.contentResolver, uri)

    fun deliverSingle(uri: Uri?) {
        if (uri == null) { currentOnResult.value(MediaPickerResult.Cancelled); return }
        val item = buildItem(uri)
        if (item != null) currentOnResult.value(MediaPickerResult.Success(item))
        else currentOnResult.value(MediaPickerResult.Error("Unable to read selected file"))
    }

    fun deliverMultiple(uris: List<Uri>) {
        if (uris.isEmpty()) { currentOnResult.value(MediaPickerResult.Cancelled); return }
        val items = uris.mapNotNull { buildItem(it) }
        if (items.isNotEmpty()) currentOnResult.value(MediaPickerResult.MultipleSuccess(items))
        else currentOnResult.value(MediaPickerResult.Cancelled)
    }

    val visualSingle = rememberLauncherForActivityResult(PickVisualMedia()) { deliverSingle(it) }
    val visualMultiple = rememberLauncherForActivityResult(PickMultipleVisualMedia()) { deliverMultiple(it) }
    val docSingle = rememberLauncherForActivityResult(OpenDocument()) { deliverSingle(it) }
    val docMultiple = rememberLauncherForActivityResult(OpenMultipleDocuments()) { deliverMultiple(it) }

    return remember {
        MediaPickerHandle {
            val types = currentTypes.value
            val multiple = currentAllowMultiple.value
            if (MimeTypeUtils.isVisualOnly(types)) {
                val req = PickVisualMediaRequest(MimeTypeUtils.resolveVisualMediaInput(types))
                if (multiple) visualMultiple.launch(req) else visualSingle.launch(req)
            } else {
                val mimes = MimeTypeUtils.resolveInputMimeTypes(types)
                if (multiple) docMultiple.launch(mimes) else docSingle.launch(mimes)
            }
        }
    }
}
