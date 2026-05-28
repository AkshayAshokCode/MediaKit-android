package com.akshayashokcode.imagecropper

import android.os.Parcel
import android.os.Parcelable

class CropperOptions(
    val aspectRatios: List<AspectRatio> = listOf(AspectRatio.Free),
    val lockAspectRatio: Boolean = false,
    val cropShape: CropShape = CropShape.Rectangle,
    val showRotateButtons: Boolean = false,
    val showFlipButtons: Boolean = false,
    val outputFormat: OutputFormat = OutputFormat.JPEG(),
    val maxOutputWidth: Int = 0,
    val maxOutputHeight: Int = 0,
    val minOutputWidth: Int = 100,
    val minOutputHeight: Int = 100
) : Parcelable {

    constructor(parcel: Parcel) : this(
        aspectRatios = parcel.readAspectRatioList(),
        lockAspectRatio = parcel.readInt() != 0,
        cropShape = parcel.readCropShape(),
        showRotateButtons = parcel.readInt() != 0,
        showFlipButtons = parcel.readInt() != 0,
        outputFormat = parcel.readOutputFormat(),
        maxOutputWidth = parcel.readInt(),
        maxOutputHeight = parcel.readInt(),
        minOutputWidth = parcel.readInt(),
        minOutputHeight = parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeAspectRatioList(aspectRatios)
        parcel.writeInt(if (lockAspectRatio) 1 else 0)
        parcel.writeCropShape(cropShape)
        parcel.writeInt(if (showRotateButtons) 1 else 0)
        parcel.writeInt(if (showFlipButtons) 1 else 0)
        parcel.writeOutputFormat(outputFormat)
        parcel.writeInt(maxOutputWidth)
        parcel.writeInt(maxOutputHeight)
        parcel.writeInt(minOutputWidth)
        parcel.writeInt(minOutputHeight)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<CropperOptions> {
        override fun createFromParcel(parcel: Parcel) = CropperOptions(parcel)
        override fun newArray(size: Int): Array<CropperOptions?> = arrayOfNulls(size)
    }
}

private fun Parcel.writeAspectRatioList(list: List<AspectRatio>) {
    writeInt(list.size)
    list.forEach { writeAspectRatio(it) }
}

private fun Parcel.readAspectRatioList(): List<AspectRatio> {
    val size = readInt()
    return (0 until size).map { readAspectRatio() }
}

private fun Parcel.writeAspectRatio(ratio: AspectRatio) {
    when (ratio) {
        is AspectRatio.Free -> writeInt(0)
        is AspectRatio.Square -> writeInt(1)
        is AspectRatio.Ratio -> {
            writeInt(2)
            writeInt(ratio.width)
            writeInt(ratio.height)
        }
    }
}

private fun Parcel.readAspectRatio(): AspectRatio = when (readInt()) {
    1 -> AspectRatio.Square
    2 -> AspectRatio.Ratio(readInt(), readInt())
    else -> AspectRatio.Free
}

private fun Parcel.writeCropShape(shape: CropShape) {
    writeInt(if (shape is CropShape.Circle) 1 else 0)
}

private fun Parcel.readCropShape(): CropShape =
    if (readInt() == 1) CropShape.Circle else CropShape.Rectangle

private fun Parcel.writeOutputFormat(format: OutputFormat) {
    when (format) {
        is OutputFormat.JPEG -> {
            writeInt(0)
            writeInt(format.quality)
        }
        is OutputFormat.PNG -> writeInt(1)
        is OutputFormat.WebP -> {
            writeInt(2)
            writeInt(format.quality)
        }
    }
}

private fun Parcel.readOutputFormat(): OutputFormat = when (readInt()) {
    1 -> OutputFormat.PNG
    2 -> OutputFormat.WebP(readInt())
    else -> OutputFormat.JPEG(readInt())
}
