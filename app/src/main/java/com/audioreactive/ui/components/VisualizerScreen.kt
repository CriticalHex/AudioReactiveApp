package com.audioreactive.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.audioreactive.ui.viewmodel.state.VisualizerBarColorMode
import com.audioreactive.ui.viewmodel.state.VisualizerBarSoundMode
import kotlinx.coroutines.isActive
import kotlin.math.pow

@Composable
fun VisualizerScreen(
    spectrum: FloatArray,
    modifier: Modifier = Modifier,
    barColorMode: VisualizerBarColorMode = VisualizerBarColorMode.DEFAULT,
    solidBarColorArgb: Int = Color.Cyan.toArgb(),
    barRiseSpeed: Float = 1f,
    barFallSpeed: Float = 1f,
    barSensitivity: Float = 1f,
    barSoundMode: VisualizerBarSoundMode = VisualizerBarSoundMode.BALANCED,
    barMaxHeight: Float = 1f,
    barCount: Int = 96,
    barOpacity: Float = 0.9f
) {
    val displayHeights = remember { FloatArray(768) }
    val latestSpectrum = remember { mutableStateOf(spectrum) }
    val lastUpdateNanos = remember { mutableLongStateOf(0L) }
    LaunchedEffect(spectrum) {
        latestSpectrum.value = spectrum
        lastUpdateNanos.longValue = System.nanoTime()
    }

    var frame by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        while (isActive) {
            withFrameNanos { frame = it }
        }
    }

    val staleThresholdNanos = 120_000_000L

    val solidBarColor = Color(solidBarColorArgb)

    Canvas(modifier = modifier.fillMaxSize()) {
        frame // read so Canvas redraws every frame
        val source = latestSpectrum.value
        if (source.isEmpty()) return@Canvas

        val effectiveBarCount = barCount.coerceIn(1, displayHeights.size)
        val barWidth = size.width / effectiveBarCount
        val isStale = System.nanoTime() - lastUpdateNanos.longValue > staleThresholdNanos

        val riseMul = barRiseSpeed.coerceIn(0.1f, 3f)
        val fallMul = barFallSpeed.coerceIn(0.1f, 3f)
        val sensMul = barSensitivity.coerceIn(0.1f, 3f)
        val maxHeightFrac = barMaxHeight.coerceIn(0.1f, 1f)
        val opacity = barOpacity.coerceIn(0.1f, 1f)
        val hueOffset = (frame / 1_000_000_000f * 30f) % 360f

        for (i in 0 until effectiveBarCount) {
            val freqWeight = i.toFloat() / (effectiveBarCount - 1).coerceAtLeast(1)

            val srcPos = freqWeight * (source.size - 1)
            val srcLo = srcPos.toInt().coerceIn(0, source.size - 1)
            val srcHi = (srcLo + 1).coerceAtMost(source.size - 1)
            val frac = srcPos - srcLo
            val sampled = (1f - frac) * source[srcLo] + frac * source[srcHi]

            val tilt = when (barSoundMode) {
                VisualizerBarSoundMode.FLAT -> 1f
                VisualizerBarSoundMode.BALANCED -> 2f.pow(freqWeight * 1.8f)
            }
            val raw = sampled * sensMul * tilt
            val target = if (isStale) 0f else raw.coerceIn(0f, 1f)

            val attackBoost = 1f + 0.35f * freqWeight
            val attack = (0.5f * attackBoost * riseMul).coerceIn(0.05f, 0.95f)
            val release = (0.4f * fallMul).coerceIn(0.05f, 0.95f)
            val rate = if (target > displayHeights[i]) attack else release
            displayHeights[i] += rate * (target - displayHeights[i])

            val barHeight = size.height * displayHeights[i] * maxHeightFrac

            val barColor = when (barColorMode) {
                VisualizerBarColorMode.DEFAULT -> {
                    Color.hsv(270f * (1f - freqWeight), 1f, 1f, opacity)
                }
                VisualizerBarColorMode.SOLID -> {
                    solidBarColor.copy(alpha = opacity)
                }
                VisualizerBarColorMode.RAINBOW_CYCLE -> {
                    val hue = (360f - hueOffset - freqWeight * 360f).mod(360f)
                    Color.hsv(hue, 1f, 1f, opacity)
                }
            }

            drawRect(
                color = barColor,
                topLeft = Offset(i * barWidth, size.height - barHeight),
                size = Size(barWidth + 0.5f, barHeight)
            )
        }
    }
}
