package com.audioreactive.ui.viewmodel.intent

import android.net.Uri

data class QueuedAudio(
    val uri: Uri,
    val title: String
)

sealed class AudioPlayerIntent : AudioReactiveIntent {
    class SetQueue(val songs: List<QueuedAudio>) : AudioPlayerIntent()
    class QueueAudio(val song: QueuedAudio) : AudioPlayerIntent()
    class SelectQueueIndex(val index: Int) : AudioPlayerIntent()

    object TogglePlayback : AudioPlayerIntent()
    object Pause : AudioPlayerIntent()
    object Play : AudioPlayerIntent()
    object Stop : AudioPlayerIntent()
    object Previous : AudioPlayerIntent()
    object Next : AudioPlayerIntent()
}
