package com.csci448.abhattarai.reactortest.points

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.*

fun dist(p1: Offset, p2: Offset): Double {
    val dx = (p1.x - p2.x).toDouble()
    val dy = (p1.y - p2.y).toDouble()
    return sqrt(dx * dx + dy * dy)
}

private fun lerpColor(c1: Color, c2: Color, t: Float, alpha: Float = 100f / 255f): Color {
    val r = c1.red + t * (c2.red - c1.red)
    val g = c1.green + t * (c2.green - c1.green)
    val b = c1.blue + t * (c2.blue - c1.blue)
    return Color(r, g, b, alpha)
}

fun computeColor(time: Double, alpha: Float = 100f / 255f): Color {
    val hueRange = 360.0
    val hue = ((time / 5.0) % 1.0) * hueRange
    val i = ((hue / 60.0).toInt()) % 6
    val f = (hue / 60.0 - i).toFloat()

    val colors = arrayOf(
        Color.Red,
        Color.Yellow,
        Color.Green,
        Color(0f, 1f, 1f, 1f), // Cyan
        Color.Blue,
        Color(1f, 0f, 1f, 1f)  // Magenta
    )

    val c1 = colors[i]
    val c2 = colors[(i + 1) % 6]
    return lerpColor(c1, c2, f, alpha)
}

fun smooth(x: Float): Double = atan(x.toDouble() + 0.2)


fun Offset.scale(s: Float): Offset = Offset(x * s, y * s)


fun Offset.translate(by: Offset): Offset = this + by