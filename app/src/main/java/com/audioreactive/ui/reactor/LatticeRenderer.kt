package com.audioreactive.ui.reactor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.DrawScope

private const val DEFAULT_MAX_LINES = 1100

fun DrawScope.drawLatticeLines(l: Lattice, maxLines: Int = DEFAULT_MAX_LINES, strokeWidth: Float = 1f) {
    val pts = l.getProjectedPoints()
    val c = l.getColor()
    val n = minOf(l.edges.size, maxLines)

    for (k in 0 until n) {
        val (i, j) = l.edges[k]
        drawLine(
            color = c,
            start = pts[i],
            end = pts[j],
            strokeWidth = strokeWidth
        )
    }
}

@Composable
fun LatticeDisplay(
    l: Lattice,
    modifier: Modifier = Modifier,
    maxLines: Int = DEFAULT_MAX_LINES,
    strokeWidth: Float = 1f
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        drawLatticeLines(l, maxLines = maxLines, strokeWidth = strokeWidth)
    }
}

@Composable
fun AnimatedLatticeDisplay(
    l: Lattice,
    modifier: Modifier = Modifier,
    maxLines: Int = DEFAULT_MAX_LINES,
    strokeWidth: Float = 1f,
    timeScale: Double = 1.0,
    spectrum: FloatArray = FloatArray(0),
    timeProvider: ((Long) -> Double)? = null
) {
    var drawTick by remember { mutableLongStateOf(0L) }
    val latestSpectrum by rememberUpdatedState(spectrum)

    LaunchedEffect(l, timeScale, timeProvider) {
        val start = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            val t = timeProvider?.invoke(now)
                ?: (((now - start) / 1_000_000_000.0) * timeScale)

            l.update(t, latestSpectrum)
            drawTick = now
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        drawTick
        drawLatticeLines(l, maxLines = maxLines, strokeWidth = strokeWidth)
    }
}
