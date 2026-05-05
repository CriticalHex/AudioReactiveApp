package com.audioreactive.ui.viewmodel.state

import kotlinx.serialization.Serializable

@Serializable
enum class LatticeColorMode {
    DEFAULT,
    SOLID,
    DIMENSION_CYCLE
}

@Serializable
enum class LatticeLineDensity {
    LOW,
    MEDIUM,
    HIGH;

    val maxLines: Int
        get() = when (this) {
            LOW -> 300
            MEDIUM -> 650
            HIGH -> 1100
        }
}

@Serializable
data class LatticeState(
    val startTimeInNano: Long? = null,
    val accumulatedTimeInSeconds: Double = 0.0,
    val timeInSeconds: Double = 0.0,
    val latticeColorMode: LatticeColorMode = LatticeColorMode.DEFAULT,
    val solidColorArgb: Int = 0xFF00FFFF.toInt(),
    val disableLattice: Boolean = false,
    val disableGyros: Boolean = false,
    val speed: Float = 0.4f,
    val sensitivity: Float = 1.0f,
    val lineDensity: LatticeLineDensity = LatticeLineDensity.HIGH
) : AudioReactiveState
