package com.akshayashokcode.imagecropper

sealed class AspectRatio {
    abstract val label: String
    abstract val ratioWidth: Float
    abstract val ratioHeight: Float

    object Free : AspectRatio() {
        override val label = "Free"
        override val ratioWidth = 0f
        override val ratioHeight = 0f
    }

    object Square : AspectRatio() {
        override val label = "1:1"
        override val ratioWidth = 1f
        override val ratioHeight = 1f
    }

    data class Ratio(val width: Int, val height: Int) : AspectRatio() {
        override val label = "$width:$height"
        override val ratioWidth = width.toFloat()
        override val ratioHeight = height.toFloat()
    }

    companion object {
        val FourThree = Ratio(4, 3)
        val SixteenNine = Ratio(16, 9)
        val ThreeTwo = Ratio(3, 2)
        val FiveFour = Ratio(5, 4)
    }
}
