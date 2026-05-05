package com.audioreactive.player

import android.content.Context
import android.util.Log
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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.nio.ByteBuffer

@UnstableApi
class AudioPlayer private constructor(context: Context) {
    companion object {
        private const val LOG_TAG = "AR.AudioPlayer"
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

    private class FftBufferSink(private val onFloatArrayReady: (FloatArray) -> Unit) : TeeAudioProcessor.AudioBufferSink {
        private var isFloatEncoding = false

        override fun flush(sampleRateHz: Int, channelCount: Int, encoding: Int) {
            isFloatEncoding = encoding == ENCODING_PCM_FLOAT
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

    private val teeProcessor = TeeAudioProcessor(FftBufferSink { audioData ->
        audioDataListener?.invoke(audioData)
    })

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
                    audioDataListener?.invoke(FloatArray(AudioProcessor.NUM_BINS))
                }
            }
        })
    }
}