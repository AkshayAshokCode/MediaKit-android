package com.akshayashokcode.mediakitcore.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object TempFileManager {

    fun createTempImageUri(context: Context, authority: String): Uri? = try {
        val file = File.createTempFile("capture_", ".jpg", context.cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        FileProvider.getUriForFile(context, authority, file)
    } catch (_: Exception) { null }

    fun deleteTempFile(context: Context, uri: Uri) {
        try { context.contentResolver.delete(uri, null, null) } catch (_: Exception) { }
    }
}
