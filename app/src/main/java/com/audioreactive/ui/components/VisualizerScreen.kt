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
import kotlinx.coroutines.isActive

@Composable
fun VisualizerScreen(
    spectrum: FloatArray,
    modifier: Modifier = Modifier,
    barColorMode: VisualizerBarColorMode = VisualizerBarColorMode.DEFAULT,
    solidBarColorArgb: Int = Color.Cyan.toArgb()
) {
    val displayHeights = remember { FloatArray(1024) }
    val latestSpectrum = remember { mutableStateOf(spectrum) }
    LaunchedEffect(spectrum) { latestSpectrum.value = spectrum }

    var frame by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        while (isActive) {
            withFrameNanos { frame = it }
        }
    }

    val solidBarColor = Color(solidBarColorArgb)

    Canvas(modifier = modifier.fillMaxSize()) {
        frame // read so Canvas redraws every frame
        val current = latestSpectrum.value
        if (current.isEmpty()) return@Canvas

        val barCount = current.size
        val barWidth = size.width / barCount

        for (i in 0 until barCount) {
            val target = current[i].coerceIn(0f, 1f)

            val freqWeight = i.toFloat() / (barCount - 1).coerceAtLeast(1)
            val attackBoost = 1f + 0.35f * freqWeight
            val attack = (0.5f * attackBoost).coerceAtMost(0.9f)
            val release = 0.4f
            val rate = if (target > displayHeights[i]) attack else release
            displayHeights[i] += rate * (target - displayHeights[i])

            val barHeight = size.height * displayHeights[i]

            val barColor = when (barColorMode) {
                VisualizerBarColorMode.DEFAULT -> {
                    Color.hsv(270f * (1f - i.toFloat() / barCount), 1f, 1f, .9f)
                }
                VisualizerBarColorMode.SOLID -> {
                    solidBarColor.copy(alpha = 0.9f)
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
