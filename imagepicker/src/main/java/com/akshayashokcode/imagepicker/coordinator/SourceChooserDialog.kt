package com.akshayashokcode.imagepicker.coordinator

import android.content.Context
import androidx.appcompat.app.AlertDialog

internal fun showSourceChooserDialog(
    context: Context,
    onGallery: () -> Unit,
    onCamera: () -> Unit
) {
    AlertDialog.Builder(context)
        .setTitle("Select Source")
        .setItems(arrayOf("Gallery", "Camera")) { _, which ->
            when (which) {
                0 -> onGallery()
                1 -> onCamera()
            }
        }
        .setNegativeButton("Cancel", null)
        .show()
}
