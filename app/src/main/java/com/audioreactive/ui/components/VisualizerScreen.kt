package com.audioreactive.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.isActive

@Composable
fun VisualizerScreen(spectrum: FloatArray, modifier: Modifier = Modifier) {
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

    Canvas(modifier = modifier.fillMaxSize()) {
        frame // read so Canvas redraws every frame
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

            drawRect(
                color = Color.Cyan,
                topLeft = Offset(i * barWidth, size.height - barHeight),
                size = Size(barWidth * 0.8f, barHeight)
            )
        }
    }
}
