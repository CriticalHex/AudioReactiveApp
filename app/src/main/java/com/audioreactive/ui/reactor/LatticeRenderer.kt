package com.audioreactive.ui.reactor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

private const val DEFAULT_MAX_LINES = 1100
private const val HUE_CYCLE_SPEED = 20.0

fun DrawScope.drawLatticeLines(
    l: Lattice,
    maxLines: Int = DEFAULT_MAX_LINES,
    strokeWidth: Float = 1f,
    hueOffset: Float = 0f,
    dimensionCycle: Boolean = false
) {
    val pts = l.getProjectedPoints()
    val n = minOf(l.edges.size, maxLines)
    val dimColors = if (dimensionCycle) {
        Array(Lattice.DIMENSIONS) { d ->
            val hue = (hueOffset + d.toFloat() / Lattice.DIMENSIONS * 80f) % 360f
            Color.hsv(hue, 1f, 1f, 100f / 255f)
        }
    } else null

    for (k in 0 until n) {
        val (i, j) = l.edges[k]
        val color = dimColors?.get(l.edgeDimensions[k]) ?: l.getColor()
        drawLine(
            color = color,
            start = pts[i],
            end = pts[j],
            strokeWidth = strokeWidth
        )
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
    timeProvider: ((Long) -> Double)? = null,
    dimensionCycle: Boolean = false
) {
    var drawTick by remember { mutableLongStateOf(0L) }
    var hueOffset by remember { mutableFloatStateOf(200f) }
    val latestSpectrum by rememberUpdatedState(spectrum)
    val latestDimensionCycle by rememberUpdatedState(dimensionCycle)

    LaunchedEffect(l, timeScale, timeProvider) {
        val start = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            val t = timeProvider?.invoke(now)
                ?: (((now - start) / 1_000_000_000.0) * timeScale)

            l.update(t, latestSpectrum)
            hueOffset = ((t * HUE_CYCLE_SPEED) % 360.0).toFloat()
            drawTick = now
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        drawTick
        drawLatticeLines(
            l,
            maxLines = maxLines,
            strokeWidth = strokeWidth,
            hueOffset = hueOffset,
            dimensionCycle = latestDimensionCycle
        )
    }
}
