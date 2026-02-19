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
    timeScale: Double = 1.0
) {
    var frame by remember { mutableStateOf(0L) }

    LaunchedEffect(l, timeScale) {
        val start = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            val t = ((now - start) / 1_000_000_000.0) * timeScale
            l.update(t)
            frame = now // forces recomposition
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        frame // observe changes
        drawLatticeLines(l, maxLines = maxLines, strokeWidth = strokeWidth)
    }
}

