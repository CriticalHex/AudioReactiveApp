package com.audioreactive.player

import android.content.Context
import androidx.media3.common.C.ENCODING_PCM_FLOAT
import androidx.media3.common.Player
import androidx.media3.common.audio.ToInt16PcmAudioProcessor
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.audio.TeeAudioProcessor
import androidx.media3.exoplayer.audio.ToFloatPcmAudioProcessor
import com.audioreactive.AudioProcessor
import java.nio.ByteBuffer
import java.util.ArrayDeque

@UnstableApi
class AudioPlayer private constructor(context: Context) {
    companion object {
        private const val LOG_TAG = "AR.AudioPlayer"
        private var INSTANCE: AudioPlayer? = null

        private const val FILE_MODE_DELAY_MS = 150L

        fun getInstance(context: Context) =
            INSTANCE ?: AudioPlayer(context).also { INSTANCE = it }
    }

    private var audioDataListener: ((FloatArray) -> Unit)? = null

    fun registerAudioDataListener(callback: (FloatArray) -> Unit) {
        audioDataListener = callback
    }

    fun unregisterAudioDataListener() {
        audioDataListener = null
    }

    private class DelayingDispatcher(
        private val delayMs: Long,
        private val onReady: (FloatArray) -> Unit
    ) {
        private data class Pending(val readyAt: Long, val data: FloatArray)

        private val queue = ArrayDeque<Pending>()
        private var sampleRate: Int = 48_000
        private var channelCount: Int = 2

        fun configure(sampleRate: Int, channelCount: Int) {
            this.sampleRate = sampleRate.coerceAtLeast(1)
            this.channelCount = channelCount.coerceAtLeast(1)
            queue.clear()
        }

        fun submit(samples: FloatArray) {
            val now = System.nanoTime() / 1_000_000L
            queue.addLast(Pending(now + delayMs, samples))
            while (true) {
                val head = queue.peekFirst() ?: break
                if (head.readyAt > now) break
                queue.pollFirst()
                onReady(head.data)
            }
        }

        fun flush() {
            queue.clear()
        }
    }

    private val delayingDispatcher = DelayingDispatcher(FILE_MODE_DELAY_MS) { data ->
        audioDataListener?.invoke(data)
    }

    private class FftBufferSink(
        private val onConfigure: (Int, Int) -> Unit,
        private val onFloatArrayReady: (FloatArray) -> Unit
    ) : TeeAudioProcessor.AudioBufferSink {
        private var isFloatEncoding = false

        override fun flush(sampleRateHz: Int, channelCount: Int, encoding: Int) {
            isFloatEncoding = encoding == ENCODING_PCM_FLOAT
            onConfigure(sampleRateHz, channelCount)
        }

        override fun handleBuffer(buffer: ByteBuffer) {
            if (!isFloatEncoding || !buffer.hasRemaining()) return

            // Create a copy so we don't block the audio thread's buffer
            val floatBuffer = buffer.asFloatBuffer()
            val floatArray = FloatArray(floatBuffer.remaining())
            floatBuffer.get(floatArray)

            onFloatArrayReady(floatArray)
        }
    }

    private val teeProcessor = TeeAudioProcessor(
        FftBufferSink(
            onConfigure = { sr, ch -> delayingDispatcher.configure(sr, ch) },
            onFloatArrayReady = { audioData -> delayingDispatcher.submit(audioData) }
        )
    )

    private val audioSink: DefaultAudioSink = DefaultAudioSink.Builder(context)
        .setAudioProcessors(arrayOf(
            ToFloatPcmAudioProcessor(),
            teeProcessor,
            ToInt16PcmAudioProcessor()
        ))
        .build()

    private val renderersFactory = object : DefaultRenderersFactory(context) {
        override fun buildAudioSink(
            context: Context,
            enableFloatOutput: Boolean,
            enableAudioTrackPlaybackParams: Boolean
        ): AudioSink = audioSink
    }

    val player: ExoPlayer = ExoPlayer.Builder(context, renderersFactory).build().also {
        it.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (!isPlaying) {
                    delayingDispatcher.flush()
                    audioDataListener?.invoke(FloatArray(AudioProcessor.NUM_BINS))
                }
            }
        })
    }
}
