package com.akshayashokcode.audiorecorder.activity

import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.akshayashokcode.audiorecorder.R
import com.akshayashokcode.audiorecorder.model.AudioRecorderOptions
import com.akshayashokcode.audiorecorder.view.WaveformView
import java.io.File

internal class RecorderActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_OPTIONS = "extra_recorder_options"
        const val EXTRA_RESULT_URI = "extra_result_uri"
        const val EXTRA_RESULT_DURATION_MS = "extra_result_duration_ms"
        private const val WAVEFORM_INTERVAL_MS = 80L
        private const val TIMER_INTERVAL_MS = 200L
    }

    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var startTimeMs = 0L
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var options: AudioRecorderOptions
    private lateinit var timerText: TextView
    private lateinit var waveformView: WaveformView

    private val timerRunnable = object : Runnable {
        override fun run() {
            val elapsed = System.currentTimeMillis() - startTimeMs
            val totalSec = (elapsed / 1000L).toInt()
            timerText.text = "%d:%02d".format(totalSec / 60, totalSec % 60)

            if (options.maxDurationSeconds > 0 && totalSec >= options.maxDurationSeconds) {
                stopAndReturn()
                return
            }
            handler.postDelayed(this, TIMER_INTERVAL_MS)
        }
    }

    private val waveformRunnable = object : Runnable {
        override fun run() {
            val amplitude = recorder?.maxAmplitude?.toFloat() ?: 0f
            val normalised = (amplitude / 32767f).coerceIn(0f, 1f)
            waveformView.addAmplitude(normalised)
            handler.postDelayed(this, WAVEFORM_INTERVAL_MS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recorder)

        @Suppress("DEPRECATION")
        options = (intent.getSerializableExtra(EXTRA_OPTIONS) as? AudioRecorderOptions)
            ?: AudioRecorderOptions()

        timerText = findViewById(R.id.timerText)
        waveformView = findViewById(R.id.waveformView)

        if (!options.showWaveform) waveformView.visibility = View.GONE

        findViewById<Button>(R.id.cancelButton).setOnClickListener {
            recorder?.runCatching { stop(); release() }
            recorder = null
            outputFile?.delete()
            setResult(RESULT_CANCELED)
            finish()
        }

        findViewById<Button>(R.id.stopButton).setOnClickListener { stopAndReturn() }

        startRecording()
    }

    private fun startRecording() {
        val dir = File(cacheDir, "mediakit-recorded").also { it.mkdirs() }
        val ext = "m4a"
        val file = File(dir, "recording_${System.currentTimeMillis()}.$ext")
        outputFile = file

        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        recorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            // AAC in MPEG-4 container works on all supported API levels.
            // WAV (LPCM) is reserved for a future release once stable across the full API range.
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(file.absolutePath)
            runCatching { prepare(); start() }.onFailure {
                release()
                recorder = null
                setResult(RESULT_CANCELED)
                finish()
                return
            }
        }

        startTimeMs = System.currentTimeMillis()
        handler.post(timerRunnable)
        if (options.showWaveform) handler.post(waveformRunnable)
    }

    private fun stopAndReturn() {
        handler.removeCallbacks(timerRunnable)
        handler.removeCallbacks(waveformRunnable)

        val durationMs = System.currentTimeMillis() - startTimeMs
        recorder?.runCatching { stop(); release() }
        recorder = null

        val file = outputFile ?: run { setResult(RESULT_CANCELED); finish(); return }
        if (!file.exists() || file.length() == 0L) { setResult(RESULT_CANCELED); finish(); return }

        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.mediakit.audiorecorder.fileprovider",
            file
        )

        setResult(RESULT_OK, Intent().apply {
            putExtra(EXTRA_RESULT_URI, uri.toString())
            putExtra(EXTRA_RESULT_DURATION_MS, durationMs)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        })
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        recorder?.runCatching { stop(); release() }
        recorder = null
    }
}
