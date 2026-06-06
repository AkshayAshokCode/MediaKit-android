package com.akshayashokcode.mediapreviewer.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.net.toUri
import androidx.viewpager2.widget.ViewPager2
import com.akshayashokcode.mediapreviewer.R
import com.akshayashokcode.mediapreviewer.adapter.PreviewAdapter
import com.akshayashokcode.mediapreviewer.model.MediaPreviewItem

internal class PreviewActivity : ComponentActivity() {

    companion object {
        const val EXTRA_URIS = "extra_preview_uris"
        const val EXTRA_TYPES = "extra_preview_types"
        const val EXTRA_SHOW_SHARE = "extra_show_share"
        const val EXTRA_ZOOM_ENABLED = "extra_zoom_enabled"
    }

    private lateinit var adapter: PreviewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        val uriStrings = intent.getStringArrayExtra(EXTRA_URIS) ?: emptyArray()
        val typeInts = intent.getIntArrayExtra(EXTRA_TYPES) ?: intArrayOf()
        val showShare = intent.getBooleanExtra(EXTRA_SHOW_SHARE, false)
        val zoomEnabled = intent.getBooleanExtra(EXTRA_ZOOM_ENABLED, true)

        val items = uriStrings.zip(typeInts.toList()).map { (uriStr, typeOrdinal) ->
            val uri = uriStr.toUri()
            when (typeOrdinal) {
                1 -> MediaPreviewItem.Video(uri)
                2 -> MediaPreviewItem.Audio(uri)
                else -> MediaPreviewItem.Image(uri)
            }
        }

        adapter = PreviewAdapter(items, zoomEnabled)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        viewPager.adapter = adapter

        // Page counter
        val counter = findViewById<TextView>(R.id.pageCounter)
        if (items.size > 1) {
            counter.visibility = View.VISIBLE
            counter.text = "1 / ${items.size}"
            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    counter.text = "${position + 1} / ${items.size}"
                }
            })
        }

        // Close
        findViewById<ImageButton>(R.id.closeButton).setOnClickListener { finish() }

        // Share
        val shareBtn = findViewById<ImageButton>(R.id.shareButton)
        if (showShare) {
            shareBtn.visibility = View.VISIBLE
            shareBtn.setOnClickListener {
                val item = items.getOrNull(viewPager.currentItem) ?: return@setOnClickListener
                startActivity(Intent.createChooser(
                    Intent(Intent.ACTION_SEND).apply {
                        putExtra(Intent.EXTRA_STREAM, item.uri)
                        type = contentResolver.getType(item.uri) ?: "*/*"
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    },
                    "Share"
                ))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::adapter.isInitialized) adapter.releaseAll()
    }
}
