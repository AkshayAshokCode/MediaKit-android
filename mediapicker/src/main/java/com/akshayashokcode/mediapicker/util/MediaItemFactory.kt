package com.akshayashokcode.mediapicker.util

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import com.akshayashokcode.mediapicker.model.MediaItem

internal object MediaItemFactory {

    fun build(resolver: ContentResolver, uri: Uri): MediaItem? {
        val mimeType = resolver.getType(uri) ?: return MediaItem.Unknown(uri, "")
        return when {
            mimeType.startsWith("image/") -> MediaItem.Image(uri, mimeType)
            mimeType.startsWith("video/") -> MediaItem.Video(uri, mimeType, queryDuration(resolver, uri))
            mimeType.startsWith("audio/") -> MediaItem.Audio(
                uri, mimeType,
                queryDuration(resolver, uri),
                queryDisplayName(resolver, uri)
            )
            else -> MediaItem.Document(
                uri, mimeType,
                queryDisplayName(resolver, uri),
                querySize(resolver, uri)
            )
        }
    }

    private fun queryDuration(resolver: ContentResolver, uri: Uri): Long {
        return try {
            resolver.query(uri, arrayOf(MediaStore.MediaColumns.DURATION), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) cursor.getLong(0) else 0L
                } ?: 0L
        } catch (_: Exception) { 0L }
    }

    private fun queryDisplayName(resolver: ContentResolver, uri: Uri): String {
        return try {
            resolver.query(uri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) cursor.getString(0) ?: "" else ""
                } ?: ""
        } catch (_: Exception) { "" }
    }

    private fun querySize(resolver: ContentResolver, uri: Uri): Long {
        return try {
            resolver.query(uri, arrayOf(MediaStore.MediaColumns.SIZE), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) cursor.getLong(0) else 0L
                } ?: 0L
        } catch (_: Exception) { 0L }
    }
}
