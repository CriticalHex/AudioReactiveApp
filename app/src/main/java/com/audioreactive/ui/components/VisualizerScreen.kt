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
fun VisualizerScreenFast(
    spectrum: FloatArray,
    modifier: Modifier = Modifier,
    barColorMode: VisualizerBarColorMode = VisualizerBarColorMode.DEFAULT,
    solidBarColorArgb: Int = Color.Cyan.toArgb()
) {
    val barCount = 96
    val displayHeights = remember { FloatArray(barCount) }
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
        frame
        val current = latestSpectrum.value
        if (current.isEmpty()) return@Canvas

        val barWidth = size.width / barCount

        for (i in 0 until barCount) {
            val target = current[i].coerceIn(0f, 1f)

            displayHeights[i] = if (target == 0f) {
                displayHeights[i] * 0.98f
            } else {
                target
            }

            val barHeight = size.height * displayHeights[i]

            val barColor = when (barColorMode) {
                VisualizerBarColorMode.DEFAULT -> {
                    Color.hsv(270f * (1f - i.toFloat() / barCount), 1f, 1f, 0.2f)
                }
                VisualizerBarColorMode.SOLID -> {
                    solidBarColor.copy(alpha = 0.2f)
                }
            }

            drawRect(
                // color = Color.hsv(0f, 0f, 0.2f + 0.8f * (i.toFloat() / barCount), 1f),
                color = barColor,
                topLeft = Offset(i * barWidth, size.height - barHeight),
                size = Size(barWidth, barHeight)
            )
        }
    }
}

@Composable
fun VisualizerScreen(
    spectrum: FloatArray,
    modifier: Modifier = Modifier,
    barColorMode: VisualizerBarColorMode = VisualizerBarColorMode.DEFAULT,
    solidBarColorArgb: Int = Color.Cyan.toArgb()
) {
    val barCount = 96
    val displayHeights = remember { FloatArray(barCount) }
    // hold latest spectrum so the frame loop can always read it
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

        val barWidth = size.width / barCount

        for (i in 0 until barCount) {
            val target = current[i].coerceIn(0f, 1f)
            val rate = if (target > displayHeights[i]) 0.5f else 0.4f // first val is the increase rate, second val is the decrease rate
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
                size = Size(barWidth, barHeight)
            )
        }
    }
}
