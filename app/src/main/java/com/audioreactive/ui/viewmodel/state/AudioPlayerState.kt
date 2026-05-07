package com.audioreactive.ui.viewmodel.state

import kotlinx.serialization.Serializable

@Serializable
data class AudioPlayerState(
    val isPlaying: Boolean = false,
    val hasAudioLoaded: Boolean = false,
    val hasNext: Boolean = false,
    val songTitle: String = "",
    val queueTitles: List<String> = emptyList(),
    val currentQueueIndex: Int = 0,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L
) : AudioReactiveState
