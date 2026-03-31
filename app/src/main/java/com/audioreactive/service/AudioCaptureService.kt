package com.audioreactive.service

import android.Manifest
import android.R.drawable.ic_media_play
import android.app.Activity.RESULT_OK
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
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Process
import android.util.Log
import androidx.annotation.RequiresPermission
import com.audioreactive.AudioProcessor
import kotlinx.coroutines.channels.Channel

class AudioCaptureService : Service() {

    companion object {
        private const val LOG_TAG = "AR.AudioCaptureService"
        const val EXTRA_DATA = "com.audioreactive.service.data"
        private const val CHANNEL_ID = "media_projection"

        private object CaptureConfig {
            const val SAMPLE_RATE: Int = 48_000
            const val CHANNEL_COUNT: Int = AudioFormat.CHANNEL_IN_MONO
            const val ENCODING_FORMAT: Int = AudioFormat.ENCODING_PCM_FLOAT
        }
    }

    private lateinit var mediaProjection: MediaProjection
    private lateinit var audioRecord: AudioRecord
    private val audioChannel = Channel<FloatArray>(3)
    private var processor = AudioProcessor(audioChannel)
    private var captureThread: Thread? = null
    @Volatile private var running = false

    private val projectionCallback = object : MediaProjection.Callback() {
        override fun onStop() {
            Log.d(LOG_TAG, "Media projection stopped")
            stopCaptureAndSelf()
        }
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(LOG_TAG, "Service started")

        startForeground(1, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)

        val data = intent?.getParcelableExtra(EXTRA_DATA, Intent::class.java)
            ?: return START_NOT_STICKY

        val projectionManager = getSystemService(MediaProjectionManager::class.java)
        mediaProjection = projectionManager.getMediaProjection(RESULT_OK, data) ?: return START_NOT_STICKY

        mediaProjection.registerCallback(projectionCallback, Handler(Looper.getMainLooper()))

        if (!running) {
            Log.d(LOG_TAG, "Starting capture")
            startCapture()
            processor.start()
        }

        return START_NOT_STICKY
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

            val buffer = FloatArray(2048)

            while (running) {
                val read = audioRecord.read(
                    buffer,
                    0,
                    buffer.size,
                    AudioRecord.READ_BLOCKING
                )
                if (read > 0) {
                    audioChannel.trySend(buffer.copyOf(read))
                }
            }
            Log.d(LOG_TAG, "Capture thread stopped")
        }.apply { start() }
    }

    fun spectrumFlow() = processor.spectrumFlow
    fun volumeFlow() = processor.volumeFlow

    private fun stopCaptureAndSelf() {
        Log.d(LOG_TAG, "Stopping self. Running was: $running")
        if (!running) return

        running = false

        captureThread?.interrupt()
        captureThread = null

        runCatching {
            audioRecord.stop()
            audioRecord.release()
        }

        processor.stop()

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d(LOG_TAG, "App swiped away, shutting down service")
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        Log.d(LOG_TAG, "SERVICE DESTROYED!!")
        running = false
        captureThread?.interrupt()
        captureThread = null
        runCatching {
            audioRecord.stop()
            audioRecord.release()
        }
        processor.stop()
        runCatching {
            mediaProjection.unregisterCallback(projectionCallback)
            mediaProjection.stop()
        }
        super.onDestroy()
    }

    inner class LocalBinder : Binder() {
        fun getService(): AudioCaptureService = this@AudioCaptureService
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(LOG_TAG, "Unbound service!")
        return super.onUnbind(intent)
    }

    private fun createNotification(): Notification {
        val channel = NotificationChannel(
            CHANNEL_ID, "Audio Capture",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)

        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Capturing system audio")
            .setSmallIcon(ic_media_play)
            .build()
    }
}