package com.akshayashokcode.mediapicker.launcher

import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.akshayashokcode.mediapicker.model.MediaItem
import com.akshayashokcode.mediapicker.model.MediaPickerException
import com.akshayashokcode.mediapicker.model.MediaPickerOptions
import com.akshayashokcode.mediapicker.model.MediaPickerResult
import com.akshayashokcode.mediapicker.util.MediaItemFactory
import com.akshayashokcode.mediapicker.util.MimeTypeUtils

/**
 * Registers all required activity result contracts eagerly so each is live before
 * the activity reaches STARTED. At launch time, the correct contract is selected
 * based on the current options.
 */
internal class MediaPickerLauncher(
    context: Context,
    caller: ActivityResultCaller,
    private val getOptions: () -> MediaPickerOptions,
    private val onResult: (MediaPickerResult) -> Unit,
    private val onError: ((MediaPickerException) -> Unit)? = null
) {
    private val appContext = context.applicationContext

    // Single visual item (Image / Video / Image+Video)
    private val visualSingleLauncher = caller.registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) handleSingleResult(uri) else onResult(MediaPickerResult.Cancelled)
    }

    // Multiple visual items
    private val visualMultipleLauncher = caller.registerForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) handleMultipleResults(uris) else onResult(MediaPickerResult.Cancelled)
    }

    // Single document / audio item
    private val documentSingleLauncher = caller.registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) handleSingleResult(uri) else onResult(MediaPickerResult.Cancelled)
    }

    // Multiple documents / audio items
    private val documentMultipleLauncher = caller.registerForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) handleMultipleResults(uris) else onResult(MediaPickerResult.Cancelled)
    }

    fun launch() {
        val options = getOptions()
        val types = options.mediaTypes
        val allowMultiple = options.allowMultiple

        try {
            if (MimeTypeUtils.isVisualOnly(types)) {
                val input = PickVisualMediaRequest(MimeTypeUtils.resolveVisualMediaInput(types))
                if (allowMultiple) visualMultipleLauncher.launch(input)
                else visualSingleLauncher.launch(input)
            } else {
                val mimeTypes = MimeTypeUtils.resolveInputMimeTypes(types)
                if (allowMultiple) documentMultipleLauncher.launch(mimeTypes)
                else documentSingleLauncher.launch(mimeTypes)
            }
        } catch (e: ActivityNotFoundException) {
            onError?.invoke(MediaPickerException.AppNotFound)
            onResult(MediaPickerResult.Error("No app available to handle media selection"))
        } catch (e: Exception) {
            onError?.invoke(MediaPickerException.Unknown("Unexpected error: ${e.message}", e))
            onResult(MediaPickerResult.Error("Unexpected error during media selection"))
        }
    }

    private fun handleSingleResult(uri: Uri) {
        val options = getOptions()
        val item = MediaItemFactory.build(appContext.contentResolver, uri)
        if (item == null) {
            onError?.invoke(MediaPickerException.InvalidUri)
            onResult(MediaPickerResult.Error("Unable to read selected file"))
            return
        }
        if (isRestricted(item, options)) {
            onError?.invoke(MediaPickerException.RestrictedFile)
            onResult(MediaPickerResult.Error("Selected file is restricted by configured filters"))
        } else {
            onResult(MediaPickerResult.Success(item))
        }
    }

    private fun handleMultipleResults(uris: List<Uri>) {
        val options = getOptions()
        val items = uris.mapNotNull { uri ->
            MediaItemFactory.build(appContext.contentResolver, uri)
                ?.takeUnless { isRestricted(it, options) }
        }
        if (items.isEmpty()) {
            onError?.invoke(MediaPickerException.RestrictedFile)
            onResult(MediaPickerResult.Error("All selected files are restricted by configured filters"))
        } else {
            onResult(MediaPickerResult.MultipleSuccess(items))
        }
    }

    private fun isRestricted(item: MediaItem, options: MediaPickerOptions): Boolean {
        if (options.restrictMimeTypes.isNotEmpty() &&
            options.restrictMimeTypes.any { it.equals(item.mimeType, ignoreCase = true) }
        ) return true

        if (options.restrictExtensions.isNotEmpty()) {
            val ext = item.mimeType.substringAfterLast('/', "")
            if (options.restrictExtensions.any { it.equals(ext, ignoreCase = true) }) return true
        }

        return false
    }
}
