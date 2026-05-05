package com.audioreactive.ui.viewmodel.state

import kotlinx.serialization.Serializable

@Serializable
data class AudioPlayerState(
    val isPlaying: Boolean = false
): AudioReactiveState