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
import kotlin.math.log10
import kotlin.math.min
import kotlin.math.sqrt

class AudioProcessor(
    private val inputQueue: BlockingQueue<FloatArray>
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val fftSize = 2048
    private val fft = FloatFFT_1D(fftSize.toLong())

    private val window = FloatArray(fftSize) {
        (0.5f * (1 - cos(2 * Math.PI * it / (fftSize - 1)))).toFloat()
    }

    private val fftBuffer = FloatArray(fftSize * 2)
    private val smooth = FloatArray(fftSize / 2)

    private val _spectrumFlow = MutableStateFlow(FloatArray(fftSize / 2))
    private val _volumeFlow = MutableStateFlow(0f)

    val spectrumFlow = _spectrumFlow.asStateFlow()
    val volumeFlow = _volumeFlow.asStateFlow()

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

    private fun normalizedRmsVolume(samples: FloatArray): Float {
        // because the samples are magnitudes, this averages then puts them on a log scale.
        // test it out, see what works best for the lattice
        val rms = samples.sumOf { it.toDouble() * it.toDouble() } / samples.size.toDouble()
        val decibels = 20 * log10(rms + 1e-9f)
        return ((decibels + 60.0) / 60.0).coerceIn(0.0, 1.0).toFloat()
    }

    private fun smoothedPeakVolume(samples: FloatArray): Float {
        // this is what my c++ program was doing
        // not sure if it needs to anything other than return samples.maxOf { abs(it) }
        // since this is magnitude, not whatever windows gives you
        val peakVolume = samples.maxOf { abs(it) }.toDouble()
        if (peakVolume <= 1e-6f) return 0f
        return atan((peakVolume + 0.2f).toDouble()).toFloat()
    }

    private fun processVolume(samples: FloatArray) {
        _volumeFlow.value = smoothedPeakVolume(samples)
//        _volumeFlow.value = normalizedRmsVolume(samples)
    }

    private fun process(samples: FloatArray) {
        val n = min(samples.size, fftSize)

        for (i in 0 until n) fftBuffer[i] = samples[i] * window[i]
        for (i in n until fftSize) fftBuffer[i] = 0f

        fft.realForwardFull(fftBuffer)

        val mags = FloatArray(fftSize / 2)
        for (i in mags.indices) {
            val re = fftBuffer[2 * i]
            val im = fftBuffer[2 * i + 1]
            val mag = sqrt(re * re + im * im)
            val db = (20 * log10(mag + 1e-6f)).toFloat()
            smooth[i] = smooth[i] * 0.8f + db * 0.2f
            mags[i] = smooth[i]
        }

        _spectrumFlow.value = mags
    }
}
