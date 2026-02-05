package com.audioreactive

import android.Manifest
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Binder
import android.os.IBinder
import android.os.Process
import androidx.annotation.RequiresPermission
import java.util.concurrent.ArrayBlockingQueue

class AudioCaptureService : Service() {

    companion object {
        const val EXTRA_RESULT_CODE = "result_code"
        const val EXTRA_DATA = "data"
        private const val CHANNEL_ID = "media_projection"

        private object CaptureConfig {
            const val SAMPLE_RATE: Int = 48_000
            const val CHANNEL_COUNT: Int = AudioFormat.CHANNEL_IN_MONO
            const val ENCODING_FORMAT: Int = AudioFormat.ENCODING_PCM_FLOAT
        }
    }

    private lateinit var mediaProjection: MediaProjection
    private lateinit var audioRecord: AudioRecord
    private val audioQueue = ArrayBlockingQueue<FloatArray>(3)
    private val processor = AudioProcessor(audioQueue)
    private var captureThread: Thread? = null
    @Volatile private var running = false

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        startForeground(1, createNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)

        val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED)
            ?: return START_NOT_STICKY
        val data = intent.getParcelableExtra(EXTRA_DATA, Intent::class.java)
            ?: return START_NOT_STICKY

        val projectionManager = getSystemService(MediaProjectionManager::class.java)
        mediaProjection = projectionManager.getMediaProjection(resultCode, data) ?: return START_NOT_STICKY

        startCapture()
        processor.start()

        return START_STICKY
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun startCapture() {
        running = true

        val config = AudioPlaybackCaptureConfiguration.Builder(mediaProjection)
            .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
            .addMatchingUsage(AudioAttributes.USAGE_GAME)
            .build()

        val format = AudioFormat.Builder()
            .setSampleRate(CaptureConfig.SAMPLE_RATE)
            .setChannelMask(CaptureConfig.CHANNEL_COUNT)
            .setEncoding(CaptureConfig.ENCODING_FORMAT)
            .build()

        val bufferSize = AudioRecord.getMinBufferSize(
            CaptureConfig.SAMPLE_RATE,
            CaptureConfig.CHANNEL_COUNT,
            CaptureConfig.ENCODING_FORMAT
        )

        audioRecord = AudioRecord.Builder()
            .setAudioFormat(format)
            .setBufferSizeInBytes(bufferSize * 2)
            .setAudioPlaybackCaptureConfig(config)
            .build()

        audioRecord.startRecording()

        captureThread = Thread {
            Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)

            val buffer = FloatArray(bufferSize / 4)

            while (running) {
                val read = audioRecord.read(buffer, 0, buffer.size, AudioRecord.READ_BLOCKING)
                if (read > 0) {
                    audioQueue.offer(buffer.copyOf(read))
                }
            }
        }.apply { start() }
    }

    fun spectrumFlow() = processor.spectrumFlow

    override fun onDestroy() {
        println("SERVICE DESTROYED!!")
        running = false
        captureThread?.interrupt()
        audioRecord.stop()
        audioRecord.release()
        mediaProjection.stop()
        processor.stop()
        super.onDestroy()
    }

    inner class LocalBinder : Binder() {
        fun getService(): AudioCaptureService = this@AudioCaptureService
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder = binder

    private fun createNotification(): Notification {
        val channel = NotificationChannel(CHANNEL_ID, "Audio Capture",
            NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)

        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Capturing system audio")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .build()
    }
}
