package com.akshayashokcode.imagepicker

import androidx.core.content.FileProvider

/**
 * Distinct FileProvider subclass for the imagepicker module.
 *
 * A subclass is required so the manifest provider entry has a unique
 * android:name and does not conflict with other FileProvider declarations
 * (e.g. the imagecropper module's own FileProvider).
 */
internal class ImagePickerFileProvider : FileProvider()
