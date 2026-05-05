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
) : AudioReactiveState {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VisualizerState

        if (volume != other.volume) return false
        if (running != other.running) return false
        if (solidBarColorArgb != other.solidBarColorArgb) return false
        if (disableBars != other.disableBars) return false
        if (!spectrum.contentEquals(other.spectrum)) return false
        if (barColorMode != other.barColorMode) return false

        return true
    }

    override fun hashCode(): Int {
        var result = volume.hashCode()
        result = 31 * result + running.hashCode()
        result = 31 * result + solidBarColorArgb
        result = 31 * result + disableBars.hashCode()
        result = 31 * result + spectrum.contentHashCode()
        result = 31 * result + barColorMode.hashCode()
        return result
    }
}