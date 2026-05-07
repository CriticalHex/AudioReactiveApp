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

object LatticeDefaults {
    const val SOLID_COLOR_ARGB: Int = 0xFF00FFFF.toInt()
    val COLOR_MODE: LatticeColorMode = LatticeColorMode.DEFAULT
    const val DISABLE_LATTICE: Boolean = false
    const val DISABLE_GYROS: Boolean = false
    const val SPEED: Float = 0.4f
    const val SENSITIVITY: Float = 1.0f
    val LINE_DENSITY: LatticeLineDensity = LatticeLineDensity.HIGH
    const val INVERT_GYRO_SPIN: Boolean = true
    const val INVERT_GYRO_HORIZONTAL: Boolean = true
    const val INVERT_GYRO_VERTICAL: Boolean = true
}

@Serializable
data class LatticeState(
    val startTimeInNano: Long? = null,
    val accumulatedTimeInSeconds: Double = 0.0,
    val timeInSeconds: Double = 0.0,
    val latticeColorMode: LatticeColorMode = LatticeDefaults.COLOR_MODE,
    val solidColorArgb: Int = LatticeDefaults.SOLID_COLOR_ARGB,
    val disableLattice: Boolean = LatticeDefaults.DISABLE_LATTICE,
    val disableGyros: Boolean = LatticeDefaults.DISABLE_GYROS,
    val speed: Float = LatticeDefaults.SPEED,
    val sensitivity: Float = LatticeDefaults.SENSITIVITY,
    val lineDensity: LatticeLineDensity = LatticeDefaults.LINE_DENSITY,
    val invertGyroSpin: Boolean = LatticeDefaults.INVERT_GYRO_SPIN,
    val invertGyroHorizontal: Boolean = LatticeDefaults.INVERT_GYRO_HORIZONTAL,
    val invertGyroVertical: Boolean = LatticeDefaults.INVERT_GYRO_VERTICAL
) : AudioReactiveState
