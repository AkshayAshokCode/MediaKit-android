package com.akshayashokcode.mediapreviewer.adapter

import android.graphics.BitmapFactory
import android.widget.MediaController
import android.media.MediaPlayer
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.akshayashokcode.mediapreviewer.R
import com.akshayashokcode.mediapreviewer.model.MediaPreviewItem
import com.akshayashokcode.mediapreviewer.view.ZoomableImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class PreviewAdapter(
    private val items: List<MediaPreviewItem>,
    private val zoomEnabled: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val activePlayers = mutableMapOf<Int, MediaPlayer>()

    override fun getItemViewType(position: Int) = when (items[position]) {
        is MediaPreviewItem.Image -> 0
        is MediaPreviewItem.Video -> 1
        is MediaPreviewItem.Audio -> 2
    }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            0 -> ImageViewHolder(inflater.inflate(R.layout.page_image, parent, false))
            1 -> VideoViewHolder(inflater.inflate(R.layout.page_video, parent, false))
            else -> AudioViewHolder(inflater.inflate(R.layout.page_audio, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is MediaPreviewItem.Image -> (holder as ImageViewHolder).bind(item.uri, zoomEnabled)
            is MediaPreviewItem.Video -> (holder as VideoViewHolder).bind(item.uri)
            is MediaPreviewItem.Audio -> (holder as AudioViewHolder).bind(item.uri, position)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if (holder is VideoViewHolder) holder.release()
        if (holder is AudioViewHolder) {
            activePlayers[holder.bindingAdapterPosition]?.runCatching { stop(); release() }
            activePlayers.remove(holder.bindingAdapterPosition)
        }
    }

    fun releaseAll() {
        activePlayers.values.forEach { runCatching { it.stop(); it.release() } }
        activePlayers.clear()
    }

    inner class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imageView: ZoomableImageView = view.findViewById(R.id.zoomableImageView)
        private val loading: ProgressBar = view.findViewById(R.id.loadingIndicator)

        fun bind(uri: Uri, zoomEnabled: Boolean) {
            loading.visibility = View.VISIBLE
            imageView.isEnabled = zoomEnabled
            CoroutineScope(Dispatchers.Main).launch {
                val bmp = withContext(Dispatchers.IO) {
                    runCatching {
                        itemView.context.contentResolver.openInputStream(uri)
                            ?.use { BitmapFactory.decodeStream(it) }
                    }.getOrNull()
                }
                loading.visibility = View.GONE
                bmp?.let { imageView.fitBitmap(it) }
            }
        }
    }

    inner class VideoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val videoView: VideoView = view.findViewById(R.id.videoView)

        fun bind(uri: Uri) {
            val controller = MediaController(itemView.context)
            controller.setAnchorView(videoView)
            videoView.setMediaController(controller)
            videoView.setVideoURI(uri)
            videoView.requestFocus()
            videoView.setOnPreparedListener { it.start() }
        }

        fun release() {
            videoView.stopPlayback()
        }
    }

    inner class AudioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val label: TextView = view.findViewById(R.id.audioLabel)
        private val playPause: Button = view.findViewById(R.id.playPauseButton)

        fun bind(uri: Uri, position: Int) {
            label.text = uri.lastPathSegment ?: "Audio"
            activePlayers[position]?.runCatching { stop(); release() }

            val player = MediaPlayer()
            activePlayers[position] = player
            player.setDataSource(itemView.context, uri)
            player.prepareAsync()
            player.setOnPreparedListener { mp ->
                playPause.text = "Play"
                playPause.setOnClickListener {
                    if (mp.isPlaying) { mp.pause(); playPause.text = "Play" }
                    else { mp.start(); playPause.text = "Pause" }
                }
            }
            player.setOnCompletionListener { playPause.text = "Play" }
        }
    }
}
