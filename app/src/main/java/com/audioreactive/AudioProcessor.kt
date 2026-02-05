package com.audioreactive

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.jtransforms.fft.FloatFFT_1D
import java.util.concurrent.BlockingQueue
import kotlin.math.*

class AudioProcessor(
    private val inputQueue: BlockingQueue<FloatArray>
) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val fftSize = 2048
    private val fft = FloatFFT_1D(fftSize.toLong())

    private val window = FloatArray(fftSize) {
        (0.5f * (1 - cos(2 * Math.PI * it / (fftSize - 1)))).toFloat()
    }

    private val fftBuffer = FloatArray(fftSize * 2)
    private val smooth = FloatArray(fftSize / 2)

    private val _spectrumFlow = MutableStateFlow(FloatArray(fftSize / 2))
    val spectrumFlow: StateFlow<FloatArray> = _spectrumFlow

    fun start() {
        scope.launch {
            while (isActive) {
                val samples = inputQueue.take()
                process(samples)
            }
        }
    }

    fun stop() = scope.cancel()

    private fun process(samples: FloatArray) {
        val n = min(samples.size, fftSize)

        for (i in 0 until n) {
            fftBuffer[i] = samples[i] * window[i]
        }
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
