package com.audioreactive.ui.viewmodel.state

import kotlinx.serialization.Serializable

@Serializable
data class VisualizerState(
    val spectrum: FloatArray = FloatArray(0),
    val volume: Float = 0f
): AudioReactiveState