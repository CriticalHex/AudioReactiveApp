package com.audioreactive

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.jtransforms.fft.FloatFFT_1D
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.ln1p
import kotlin.math.log10
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

class AudioProcessor(
    private val audioChannel: Channel<FloatArray>
) {
    companion object {
        private const val LOG_TAG = "AR.AudioProcessor"
        private const val SILENCE_DB = -120f
        private const val FFT_SIZE = 2048
        const val NUM_BINS = 96
        private const val MIN_FREQ = 20f
        private const val MAX_FREQ = 20000f
        private const val SAMPLE_RATE = 48000f
        private const val FREQ_PER_BIN = SAMPLE_RATE / FFT_SIZE
    }
    private var jobScope = SupervisorJob()
    private var scope = CoroutineScope(Dispatchers.Default + jobScope)

    private val fft = FloatFFT_1D(FFT_SIZE.toLong())
    private val window = FloatArray(FFT_SIZE) {
        (0.5f * (1 - cos(2 * Math.PI * it / (FFT_SIZE - 1)))).toFloat()
    }
    private val fftBuffer = FloatArray(FFT_SIZE * 2)

    private val _spectrumFlow = MutableStateFlow(FloatArray(NUM_BINS) { SILENCE_DB })
    private val _volumeFlow = MutableStateFlow(0f)

    val spectrumFlow = _spectrumFlow.asStateFlow()
    val volumeFlow = _volumeFlow.asStateFlow()

    private fun createScope() {
        jobScope = SupervisorJob()
        scope = CoroutineScope(Dispatchers.Default + jobScope)
    }

    fun start() {
        jobScope.cancel()
        createScope()
        scope.launch {
            Log.d(LOG_TAG, "Starting the audio processor")
            try {
                while (isActive) {
                    val samples = audioChannel.receive()
                    process(samples)
                    processVolume(samples)
                }
            } catch (e: CancellationException) {
                Log.d(LOG_TAG, "Audio processor cancelled")
            } finally {
                Log.d(LOG_TAG, "Audio processor stopped")
            }
        }
    }

    fun stop() {
        Log.d(LOG_TAG, "Cancelling job")
        jobScope.cancel()
    }

    private fun mapToLogBins(mags: FloatArray): FloatArray {
        val result = FloatArray(NUM_BINS)
        val ratio = MAX_FREQ / MIN_FREQ
        for (i in 0 until NUM_BINS) {
            val freq = MIN_FREQ * 10.0.pow((i.toDouble() / (NUM_BINS - 1)) * log10(ratio.toDouble()))
                .toFloat()
            val fftIndex = freq / FREQ_PER_BIN
            val indexLow = fftIndex.toInt().coerceAtMost(mags.size - 1)
            val indexHigh = (indexLow + 1).coerceAtMost(mags.size - 1)
            val fraction = fftIndex - indexLow
            result[i] = (1f - fraction) * mags[indexLow] + fraction * mags[indexHigh]
        }
        return result
    }

    private var maxVolume = 1f
    private val alpha = 0.02f

    private fun normalizeVolume(volumes: FloatArray) {
        val currentMax = volumes.maxOrNull() ?: 0f
        if (currentMax > maxVolume) {
            maxVolume = currentMax
        } else {
            maxVolume *= 0.999f
        }
        if (maxVolume > 0f) {
            val denominator = ln1p((alpha * maxVolume).toDouble()).toFloat()
            for (i in volumes.indices) {
                volumes[i] = ln1p((alpha * volumes[i]).toDouble()).toFloat() / denominator
            }
        }
    }

    private fun smoothedPeakVolume(samples: FloatArray): Float {
        // this is what my c++ program was doing
        // not sure if it needs to anything other than return samples.maxOf { abs(it) }
        // since this is magnitude, not whatever windows gives you
        val peakVolume = samples.maxOf { abs(it) }.toDouble()
        if (peakVolume <= 1e-6f) return 0f
        return atan((peakVolume + 0.2f).toFloat())
    }

    private fun processVolume(samples: FloatArray) {
        _volumeFlow.value = smoothedPeakVolume(samples)
//        _volumeFlow.value = normalizedRmsVolume(samples)
    }

    private fun process(samples: FloatArray) {
        val n = min(samples.size, FFT_SIZE)
        for (i in 0 until n) fftBuffer[i] = samples[i] * window[i]
        for (i in n until FFT_SIZE * 2) fftBuffer[i] = 0f // imaginary numbers issue

        fft.realForwardFull(fftBuffer)

        val mags = FloatArray(FFT_SIZE / 2)
        for (i in mags.indices) {
            val re = fftBuffer[2 * i]
            val im = fftBuffer[2 * i + 1]
            mags[i] = sqrt(re * re + im * im)
        }

        val bins = mapToLogBins(mags)
        normalizeVolume(bins)
        _spectrumFlow.value = bins
    }
}
