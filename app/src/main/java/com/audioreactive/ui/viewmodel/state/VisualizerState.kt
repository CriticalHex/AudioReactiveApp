package com.audioreactive.ui.viewmodel.state

import kotlinx.serialization.Serializable

@Serializable
enum class VisualizerBarColorMode {
    DEFAULT,
    SOLID,
    RAINBOW_CYCLE
}

@Serializable
enum class VisualizerBarSoundMode {
    FLAT,
    BALANCED
}

object VisualizerDefaults {
    const val BAR_RISE_SPEED: Float = 1f
    const val BAR_FALL_SPEED: Float = 1f
    const val BAR_SENSITIVITY: Float = 1f
    const val SOLID_BAR_COLOR_ARGB: Int = 0xFF00FFFF.toInt()
    val BAR_COLOR_MODE: VisualizerBarColorMode = VisualizerBarColorMode.DEFAULT
    val BAR_SOUND_MODE: VisualizerBarSoundMode = VisualizerBarSoundMode.BALANCED
    const val DISABLE_BARS: Boolean = false
    const val BAR_MAX_HEIGHT: Float = 1f
    const val BAR_COUNT: Int = 96
    const val BAR_OPACITY: Float = 0.9f
    const val BAR_COUNT_MIN: Int = 24
    const val BAR_COUNT_MAX: Int = 768
}

@Serializable
data class VisualizerState(
    val spectrum: FloatArray = FloatArray(0),
    val volume: Float = 0f,
    val running: Boolean = false,
    val barColorMode: VisualizerBarColorMode = VisualizerDefaults.BAR_COLOR_MODE,
    val solidBarColorArgb: Int = VisualizerDefaults.SOLID_BAR_COLOR_ARGB,
    val disableBars: Boolean = VisualizerDefaults.DISABLE_BARS,
    val barRiseSpeed: Float = VisualizerDefaults.BAR_RISE_SPEED,
    val barFallSpeed: Float = VisualizerDefaults.BAR_FALL_SPEED,
    val barSensitivity: Float = VisualizerDefaults.BAR_SENSITIVITY,
    val barSoundMode: VisualizerBarSoundMode = VisualizerDefaults.BAR_SOUND_MODE,
    val barMaxHeight: Float = VisualizerDefaults.BAR_MAX_HEIGHT,
    val barCount: Int = VisualizerDefaults.BAR_COUNT,
    val barOpacity: Float = VisualizerDefaults.BAR_OPACITY
) : AudioReactiveState {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VisualizerState

        if (volume != other.volume) return false
        if (running != other.running) return false
        if (solidBarColorArgb != other.solidBarColorArgb) return false
        if (disableBars != other.disableBars) return false
        if (barRiseSpeed != other.barRiseSpeed) return false
        if (barFallSpeed != other.barFallSpeed) return false
        if (barSensitivity != other.barSensitivity) return false
        if (barSoundMode != other.barSoundMode) return false
        if (barMaxHeight != other.barMaxHeight) return false
        if (barCount != other.barCount) return false
        if (barOpacity != other.barOpacity) return false
        if (!spectrum.contentEquals(other.spectrum)) return false
        if (barColorMode != other.barColorMode) return false

        return true
    }

    override fun hashCode(): Int {
        var result = volume.hashCode()
        result = 31 * result + running.hashCode()
        result = 31 * result + solidBarColorArgb
        result = 31 * result + disableBars.hashCode()
        result = 31 * result + barRiseSpeed.hashCode()
        result = 31 * result + barFallSpeed.hashCode()
        result = 31 * result + barSensitivity.hashCode()
        result = 31 * result + barSoundMode.hashCode()
        result = 31 * result + barMaxHeight.hashCode()
        result = 31 * result + barCount
        result = 31 * result + barOpacity.hashCode()
        result = 31 * result + spectrum.contentHashCode()
        result = 31 * result + barColorMode.hashCode()
        return result
    }
}