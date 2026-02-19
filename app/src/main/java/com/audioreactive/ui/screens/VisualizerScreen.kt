package com.audioreactive.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

@Composable
<<<<<<< HEAD
fun VisualizerScreen(spectrumState: State<FloatArray>, modifier: Modifier = Modifier) {

    val spectrum by spectrumState

    Canvas(modifier = modifier
        .fillMaxSize()

    ) {
=======
fun VisualizerScreen(spectrum: FloatArray, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
>>>>>>> 71a6123c99fdc52ecf4d8b7ad89942ee84553a07

        if (spectrum.isEmpty()) return@Canvas

        val barCount = 64
        val barWidth = size.width / barCount

        for (i in 0 until barCount) {

            val idx = (i.toFloat() / barCount * spectrum.size).toInt()
            val db = spectrum[idx]

            val heightNorm = ((db + 60f) / 60f).coerceIn(0f, 1f)
            val barHeight = size.height * heightNorm

            drawRect(
                color = Color.Cyan,
                topLeft = Offset(i * barWidth, size.height - barHeight),
                size = Size(barWidth * 0.8f, barHeight)
            )
        }

    }
}
