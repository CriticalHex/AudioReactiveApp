package com.audioreactive.ui.viewmodel.state

import kotlinx.serialization.Serializable

@Serializable
enum class LatticeColorMode {
    DEFAULT,
    SOLID,
    DIMENSION_CYCLE
}

@Serializable
data class LatticeState(
    val startTimeInNano: Long? = null,
    val accumulatedTimeInSeconds: Double = 0.0,
    val timeInSeconds: Double = 0.0,
    val latticeColorMode: LatticeColorMode = LatticeColorMode.DEFAULT,
    val solidColorArgb: Int = 0xFF00FFFF.toInt(),
    val disableLattice: Boolean = false,
    val disableGyros: Boolean = false
) : AudioReactiveState