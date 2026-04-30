package com.audioreactive.ui.viewmodel.state

import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.serialization.Serializable

@Serializable
data class AudioPlayerState(
    val isPlaying: Boolean = false
): AudioReactiveState