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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.util.ArrayDeque

@UnstableApi
class AudioPlayer private constructor(context: Context) {
    companion object {
        private const val LOG_TAG = "AR.AudioPlayer"
        private const val POLL_INTERVAL_MS = 8L
        private var INSTANCE: AudioPlayer? = null

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

    private data class Pending(val samplePosUs: Long, val data: FloatArray)

    private val pending = ArrayDeque<Pending>()
    private val pendingLock = Any()

    @Volatile private var teeSampleRateHz = 48000
    @Volatile private var teeChannelCount = 2
    @Volatile private var acceptingBuffers = false
    private var processedFrames = 0L

    private inner class FftBufferSink : TeeAudioProcessor.AudioBufferSink {
        private var isFloatEncoding = false

        override fun flush(sampleRateHz: Int, channelCount: Int, encoding: Int) {
            isFloatEncoding = encoding == ENCODING_PCM_FLOAT
            teeSampleRateHz = sampleRateHz.coerceAtLeast(1)
            teeChannelCount = channelCount.coerceAtLeast(1)
            synchronized(pendingLock) {
                pending.clear()
                processedFrames = 0
            }
        }

        override fun handleBuffer(buffer: ByteBuffer) {
            if (!isFloatEncoding || !buffer.hasRemaining()) return
            if (!acceptingBuffers) return

            val floatBuffer = buffer.asFloatBuffer()
            val interleaved = FloatArray(floatBuffer.remaining())
            floatBuffer.get(interleaved)

            val channels = teeChannelCount
            val frames = interleaved.size / channels
            val mono = if (channels == 1) {
                interleaved
            } else {
                val out = FloatArray(frames)
                val invChannels = 1f / channels
                for (i in 0 until frames) {
                    val base = i * channels
                    var sum = 0f
                    for (c in 0 until channels) sum += interleaved[base + c]
                    out[i] = sum * invChannels
                }
                out
            }

            synchronized(pendingLock) {
                processedFrames += frames
                val samplePosUs = processedFrames * 1_000_000L / teeSampleRateHz
                pending.addLast(Pending(samplePosUs, mono))
            }
        }
    }

    private val teeProcessor = TeeAudioProcessor(FftBufferSink())

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
                if (isPlaying) {
                    acceptingBuffers = true
                } else {
                    acceptingBuffers = false
                    synchronized(pendingLock) {
                        pending.clear()
                    }
                    audioDataListener?.invoke(FloatArray(AudioProcessor.NUM_BINS))
                }
            }
        })
    }

    private val syncScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    init {
        syncScope.launch {
            val drained = ArrayList<FloatArray>(8)
            while (isActive) {
                val playUs = try {
                    audioSink.getCurrentPositionUs(false)
                } catch (_: Throwable) {
                    Long.MIN_VALUE
                }

                drained.clear()
                synchronized(pendingLock) {
                    while (pending.isNotEmpty() && pending.peekFirst().samplePosUs <= playUs) {
                        drained.add(pending.removeFirst().data)
                    }
                }
                val listener = audioDataListener
                if (listener != null) {
                    for (i in drained.indices) listener.invoke(drained[i])
                }
                delay(POLL_INTERVAL_MS)
            }
        }
    }
}