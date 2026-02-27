package com.csci448.abhattarai.reactortest.points

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState

fun DrawScope.drawLatticeLines(l: lattice, maxLines: Int = 1100, strokeWidth: Float = 1f) {
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
    l: lattice,
    modifier: Modifier = Modifier,
    maxLines: Int = 1100,
    strokeWidth: Float = 1f
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        drawLatticeLines(l, maxLines = maxLines, strokeWidth = strokeWidth)
    }
}

@Composable
fun AnimatedLatticeDisplay(
    l: lattice,
    modifier: Modifier = Modifier,
    maxLines: Int = 1100,
    strokeWidth: Float = 1f,
    timeScale: Double = 1.0,
    volume: Float = 0f,
    timeProvider: ((Long) -> Double)? = null
) {
    var frame by remember { mutableStateOf(0L) }
    val latestVolume by rememberUpdatedState(volume)

    LaunchedEffect(l, timeScale, timeProvider) {
        val start = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            val t = timeProvider?.invoke(now)
                ?: (((now - start) / 1_000_000_000.0) * timeScale)

            l.update(t, latestVolume.toDouble())
            frame = now
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        frame
        drawLatticeLines(l, maxLines = maxLines, strokeWidth = strokeWidth)
    }
}
