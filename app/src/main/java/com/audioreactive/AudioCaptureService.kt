package com.audioreactive

import android.annotation.SuppressLint
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
import android.os.IBinder

class AudioCaptureService : Service() {

    companion object {
        const val EXTRA_RESULT_CODE = "result_code"
        const val EXTRA_DATA = "data"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "media_projection"
    }

    private lateinit var mediaProjection: MediaProjection
    private lateinit var audioRecord: AudioRecord
    @Volatile private var running = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        println("starting...")
        startForeground(
            NOTIFICATION_ID,
            createNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
        )
        println("notification created")

        val resultCode =
            intent?.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED)
                ?: return stopSelfAndAbort()
        println("result code: $resultCode")

        val data: Intent =
            intent.getParcelableExtra(EXTRA_DATA, Intent::class.java)
                ?: return stopSelfAndAbort()
        println("data: $data")

        val projectionManager =
            getSystemService(MediaProjectionManager::class.java)
        println("got projection manager")

        mediaProjection =
            projectionManager.getMediaProjection(resultCode, data)
                ?: return stopSelfAndAbort()
        println("got media projection")

        startPlaybackCapture()

        return START_STICKY
    }

    private fun startPlaybackCapture() {
        running = true
        println("starting playback capture")

        val config =
            AudioPlaybackCaptureConfiguration.Builder(mediaProjection)
                .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                .addMatchingUsage(AudioAttributes.USAGE_GAME)
                .build()
        println("configured")

        val format =
            AudioFormat.Builder()
                .setSampleRate(48_000)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_IN_STEREO)
                .build()
        println("formatted")

        val bufferSize =
            AudioRecord.getMinBufferSize(
                48_000,
                AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT
            )
        println("buffer size defined")

        try {
            audioRecord =
                AudioRecord.Builder()
                    .setAudioFormat(format)
                    .setBufferSizeInBytes(bufferSize)
                    .setAudioPlaybackCaptureConfig(config)
                    .build()

            audioRecord.startRecording()
            println("started recording")

        } catch (e: SecurityException) {
            // MediaProjection revoked or invalid
            println("error when starting to record, security exception")
            stopSelf()
            return
        }

        Thread {
            val buffer = ByteArray(bufferSize)
            while (running) {
                val read = audioRecord.read(buffer, 0, buffer.size)
                if (read > 0) {

                }
            }
        }.start()
    }

    private fun createNotification(): Notification {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "System Audio Capture",
            NotificationManager.IMPORTANCE_LOW
        )

        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)

        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Capturing system audio")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .build()
    }

    private fun stopSelfAndAbort(): Int {
        stopSelf()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        running = false

        if (::audioRecord.isInitialized) {
            audioRecord.stop()
            audioRecord.release()
        }

        if (::mediaProjection.isInitialized) {
            mediaProjection.stop()
        }

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
