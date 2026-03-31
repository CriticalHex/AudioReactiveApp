package com.audioreactive.ui.viewmodel.state

import kotlinx.serialization.Serializable

@Serializable
data class LatticeState(
    val startTimeInNano: Long? = null,
    val accumulatedTimeInSeconds: Double = 0.0,
    val timeInSeconds: Double = 0.0
): AudioReactiveState