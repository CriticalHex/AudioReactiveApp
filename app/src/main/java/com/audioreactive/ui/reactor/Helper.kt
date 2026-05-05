package com.audioreactive.ui.reactor

import androidx.compose.ui.graphics.Color

private const val DEFAULT_ALPHA = 100f / 255f
private const val HUE_CYCLE_PERIOD_SECONDS = 5.0

private fun lerpColor(c1: Color, c2: Color, t: Float, alpha: Float = DEFAULT_ALPHA): Color {
    val r = c1.red + t * (c2.red - c1.red)
    val g = c1.green + t * (c2.green - c1.green)
    val b = c1.blue + t * (c2.blue - c1.blue)
    return Color(r, g, b, alpha)
}

fun computeColor(time: Double, alpha: Float = DEFAULT_ALPHA): Color {
    val hueRange = 360.0
    val hue = ((time / HUE_CYCLE_PERIOD_SECONDS) % 1.0) * hueRange
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
