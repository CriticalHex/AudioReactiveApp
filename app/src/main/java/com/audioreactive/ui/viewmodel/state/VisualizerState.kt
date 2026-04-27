package com.audioreactive.ui.viewmodel.state

import kotlinx.serialization.Serializable

@Serializable
enum class VisualizerBarColorMode {
    DEFAULT,
    SOLID
}

@Serializable
data class VisualizerState(
    val spectrum: FloatArray = FloatArray(0),
    val volume: Float = 0f,
    val running: Boolean = false,
    val barColorMode: VisualizerBarColorMode = VisualizerBarColorMode.DEFAULT,
    val solidBarColorArgb: Int = 0xFF00FFFF.toInt(),
    val disableBars: Boolean = false
) : AudioReactiveState