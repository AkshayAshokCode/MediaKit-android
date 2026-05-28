package com.akshayashokcode.imagecropper

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.io.File
import java.io.FileOutputStream

class CropperActivity : ComponentActivity() {

    companion object {
        const val EXTRA_INPUT_URI = "extra_input_uri"
        const val EXTRA_OUTPUT_URI = "extra_output_uri"
        const val EXTRA_OPTIONS = "extra_options"
    }

    private lateinit var cropperView: CropperView
    private var options: CropperOptions = CropperOptions()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableImmersiveMode()
        setContentView(R.layout.activity_cropper)

        options = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_OPTIONS, CropperOptions::class.java) ?: CropperOptions()
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_OPTIONS) ?: CropperOptions()
        }

        cropperView = findViewById(R.id.cropperView)

        findViewById<ImageButton>(R.id.buttonCancel).setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
        findViewById<ImageButton>(R.id.buttonConfirm).setOnClickListener {
            saveCroppedImage()
        }

        val uriString = intent.getStringExtra(EXTRA_INPUT_URI)
        if (uriString == null) { finish(); return }

        val bitmap = contentResolver.openInputStream(uriString.toUri())?.use {
            BitmapFactory.decodeStream(it)
        }
        if (bitmap == null) { setResult(RESULT_CANCELED); finish(); return }

        cropperView.setImageBitmap(bitmap)
        applyOptions()
    }

    private fun applyOptions() {
        cropperView.setCropShape(options.cropShape)
        setupAspectRatioChips()
        setupTransformToolbar()
    }

    private fun setupAspectRatioChips() {
        val container = findViewById<HorizontalScrollView>(R.id.aspectRatioContainer)
        val row = findViewById<LinearLayout>(R.id.aspectRatioRow)

        if (options.aspectRatios.size <= 1) {
            container.visibility = View.GONE
            val single = options.aspectRatios.firstOrNull() ?: AspectRatio.Free
            cropperView.setAspectRatio(single, options.lockAspectRatio)
            return
        }

        container.visibility = View.VISIBLE

        options.aspectRatios.forEachIndexed { index, ratio ->
            val chip = makeChip(ratio.label, selected = index == 0)
            chip.setOnClickListener {
                if (options.lockAspectRatio && chip.isSelected) return@setOnClickListener
                for (i in 0 until row.childCount) {
                    (row.getChildAt(i) as? TextView)?.let { styleChip(it, false) }
                }
                styleChip(chip, true)
                cropperView.setAspectRatio(ratio, true)
            }
            row.addView(chip)
        }

        cropperView.setAspectRatio(options.aspectRatios.first(), true)
    }

    private fun makeChip(label: String, selected: Boolean): TextView {
        val dp = resources.displayMetrics.density
        return TextView(this).apply {
            text = label
            textSize = 13f
            isSelected = selected
            setPadding((14 * dp).toInt(), (6 * dp).toInt(), (14 * dp).toInt(), (6 * dp).toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins((5 * dp).toInt(), 0, (5 * dp).toInt(), 0) }
            styleChip(this, selected)
        }
    }

    private fun styleChip(chip: TextView, selected: Boolean) {
        val dp = resources.displayMetrics.density
        chip.isSelected = selected
        chip.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 100f * dp
            if (selected) {
                setColor(Color.WHITE)
                setStroke(0, Color.TRANSPARENT)
            } else {
                setColor(Color.TRANSPARENT)
                setStroke((1.5f * dp).toInt(), Color.parseColor("#80FFFFFF"))
            }
        }
        chip.setTextColor(if (selected) Color.BLACK else Color.WHITE)
    }

    private fun setupTransformToolbar() {
        val bottomToolbar = findViewById<LinearLayout>(R.id.bottomToolbar)
        val btnRotateCCW = findViewById<ImageButton>(R.id.buttonRotateCCW)
        val btnRotateCW = findViewById<ImageButton>(R.id.buttonRotateCW)
        val divider = findViewById<View>(R.id.dividerRotateFlip)
        val btnFlipH = findViewById<ImageButton>(R.id.buttonFlipH)
        val btnFlipV = findViewById<ImageButton>(R.id.buttonFlipV)

        if (!options.showRotateButtons && !options.showFlipButtons) return

        bottomToolbar.visibility = View.VISIBLE

        if (options.showRotateButtons) {
            btnRotateCCW.visibility = View.VISIBLE
            btnRotateCW.visibility = View.VISIBLE
            btnRotateCCW.setOnClickListener { cropperView.rotate90CCW() }
            btnRotateCW.setOnClickListener { cropperView.rotate90CW() }
        }

        if (options.showFlipButtons) {
            btnFlipH.visibility = View.VISIBLE
            btnFlipV.visibility = View.VISIBLE
            btnFlipH.setOnClickListener { cropperView.flipHorizontal() }
            btnFlipV.setOnClickListener { cropperView.flipVertical() }
        }

        if (options.showRotateButtons && options.showFlipButtons) {
            divider.visibility = View.VISIBLE
        }
    }

    private fun saveCroppedImage() {
        var cropped = cropperView.getCroppedImage() ?: run {
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        // Apply max output size cap
        val maxW = options.maxOutputWidth
        val maxH = options.maxOutputHeight
        if ((maxW > 0 && cropped.width > maxW) || (maxH > 0 && cropped.height > maxH)) {
            val scaleW = if (maxW > 0) maxW.toFloat() / cropped.width else Float.MAX_VALUE
            val scaleH = if (maxH > 0) maxH.toFloat() / cropped.height else Float.MAX_VALUE
            val scale = minOf(scaleW, scaleH)
            cropped = Bitmap.createScaledBitmap(
                cropped,
                (cropped.width * scale).toInt().coerceAtLeast(1),
                (cropped.height * scale).toInt().coerceAtLeast(1),
                true
            )
        }

        val outputFile = compressToFile(cropped)

        val outputUri = FileProvider.getUriForFile(
            this,
            "${packageName}.imagecropper.provider",
            outputFile
        )

        setResult(
            RESULT_OK,
            Intent().apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(EXTRA_OUTPUT_URI, outputUri.toString())
            }
        )
        finish()
    }

    private fun compressToFile(bitmap: Bitmap): File {
        return when (val fmt = options.outputFormat) {
            is OutputFormat.JPEG -> {
                File(cacheDir, "cropped_${System.currentTimeMillis()}.jpg").also { file ->
                    FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, fmt.quality, it) }
                }
            }
            is OutputFormat.PNG -> {
                File(cacheDir, "cropped_${System.currentTimeMillis()}.png").also { file ->
                    FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                }
            }
            is OutputFormat.WebP -> {
                val webpFormat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSY
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
                File(cacheDir, "cropped_${System.currentTimeMillis()}.webp").also { file ->
                    FileOutputStream(file).use { bitmap.compress(webpFormat, fmt.quality, it) }
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            enableImmersiveMode()
            applyInsetsDirectly()
        }
    }

    /**
     * Applies bottom clearance for the gesture bar and rounded screen corners.
     * The top is handled automatically by fitsSystemWindows on the root layout,
     * which keeps the status bar visible and lets the system manage the cutout safe area.
     */
    private fun applyInsetsDirectly() {
        val dp = resources.displayMetrics.density
        val cornerSafety = (12 * dp).toInt()

        val rootInsets = WindowInsetsCompat.toWindowInsetsCompat(
            window.decorView.rootWindowInsets ?: return
        )
        val gestureBottom = rootInsets
            .getInsets(WindowInsetsCompat.Type.mandatorySystemGestures())
            .bottom
            .coerceAtLeast((20 * dp).toInt())

        // Cropper container: bottom clearance for gesture bar + rounded corners
        val cropperContainer = findViewById<android.widget.FrameLayout>(R.id.cropperContainer)
        cropperContainer.setPadding(cornerSafety, 0, cornerSafety, gestureBottom + cornerSafety)

        // Bottom toolbar: above gesture bar
        val bottomToolbar = findViewById<LinearLayout>(R.id.bottomToolbar)
        bottomToolbar.setPadding(0, 0, 0, gestureBottom)
    }

    private fun enableImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            // Hide only the navigation bar — keeping the status bar visible lets the system
            // handle the camera cutout safe area automatically via fitsSystemWindows.
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
