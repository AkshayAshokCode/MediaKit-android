package com.akshayashokcode.mediapreviewer.builder

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultCaller
import com.akshayashokcode.mediapreviewer.activity.PreviewActivity
import com.akshayashokcode.mediapreviewer.model.MediaPreviewItem
import com.akshayashokcode.mediapreviewer.model.PreviewOptions

class MediaPreviewerBuilder internal constructor(
    private val context: Context,
    @Suppress("UNUSED_PARAMETER") caller: ActivityResultCaller
) {
    private val items = mutableListOf<MediaPreviewItem>()
    private var options = PreviewOptions()

    fun items(list: List<MediaPreviewItem>): MediaPreviewerBuilder = apply {
        items.clear(); items.addAll(list)
    }

    fun options(opts: PreviewOptions): MediaPreviewerBuilder = apply { options = opts }

    fun launch() {
        require(items.isNotEmpty()) { "Provide at least one item using items()" }
        context.startActivity(
            Intent(context, PreviewActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(PreviewActivity.EXTRA_URIS, items.map { it.uri.toString() }.toTypedArray())
                putExtra(PreviewActivity.EXTRA_TYPES, items.map { it.typeOrdinal() }.toIntArray())
                putExtra(PreviewActivity.EXTRA_SHOW_SHARE, options.showShareButton)
                putExtra(PreviewActivity.EXTRA_ZOOM_ENABLED, options.zoomEnabled)
            }
        )
    }

    private fun MediaPreviewItem.typeOrdinal() = when (this) {
        is MediaPreviewItem.Image -> 0
        is MediaPreviewItem.Video -> 1
        is MediaPreviewItem.Audio -> 2
    }
}
