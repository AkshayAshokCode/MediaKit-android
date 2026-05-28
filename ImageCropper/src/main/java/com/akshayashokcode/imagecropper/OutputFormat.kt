package com.akshayashokcode.imagecropper

sealed class OutputFormat {
    data class JPEG(val quality: Int = 90) : OutputFormat()
    object PNG : OutputFormat()
    data class WebP(val quality: Int = 90) : OutputFormat()
}
