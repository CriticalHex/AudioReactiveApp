package com.audioreactive

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.jtransforms.fft.FloatFFT_1D
import java.util.concurrent.BlockingQueue
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.ln1p
import kotlin.math.log10
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

class AudioProcessor(
    private val inputQueue: BlockingQueue<FloatArray>
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val SILENCE_DB = -120f

    private val fftSize = 2048
    private val fft = FloatFFT_1D(fftSize.toLong())

    private val window = FloatArray(fftSize) {
        (0.5f * (1 - cos(2 * Math.PI * it / (fftSize - 1)))).toFloat()
    }

    private val fftBuffer = FloatArray(fftSize * 2)
    private val NUM_BINS = 96
    private val MIN_FREQ = 20f
    private val MAX_FREQ = 20000f
    private val SAMPLE_RATE = 48000f
    private val freqPerBin = SAMPLE_RATE / fftSize

    private val _spectrumFlow = MutableStateFlow(FloatArray(NUM_BINS) { SILENCE_DB })
    private val _volumeFlow = MutableStateFlow(0f)

    val spectrumFlow = _spectrumFlow.asStateFlow()
    val volumeFlow = _volumeFlow.asStateFlow()



    private fun mapToLogBins(mags: FloatArray): FloatArray {
        val result = FloatArray(NUM_BINS)
        val ratio = MAX_FREQ / MIN_FREQ
        for (i in 0 until NUM_BINS) {
            val freq = MIN_FREQ * 10.0.pow((i.toDouble() / (NUM_BINS - 1)) * log10(ratio.toDouble()))
                .toFloat()
            val fftIndex = freq / freqPerBin
            val indexLow = fftIndex.toInt().coerceAtMost(mags.size - 1)
            val indexHigh = (indexLow + 1).coerceAtMost(mags.size - 1)
            val fraction = fftIndex - indexLow
            result[i] = (1f - fraction) * mags[indexLow] + fraction * mags[indexHigh]
        }
        return result
    }

    fun start() {
        scope.launch {
            while (isActive) {
                val samples = inputQueue.take()
                process(samples)
                processVolume(samples)
            }
        }
    }

    fun stop() {
        scope.cancel()
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
            val denom = ln1p((alpha * maxVolume).toDouble()).toFloat()
            for (i in volumes.indices) {
                volumes[i] = ln1p((alpha * volumes[i]).toDouble()).toFloat() / denom
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
        val n = min(samples.size, fftSize)
        for (i in 0 until n) fftBuffer[i] = samples[i] * window[i]
        for (i in n until fftSize * 2) fftBuffer[i] = 0f // imaginary numbers issue

        fft.realForwardFull(fftBuffer)

        val mags = FloatArray(fftSize / 2)
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
