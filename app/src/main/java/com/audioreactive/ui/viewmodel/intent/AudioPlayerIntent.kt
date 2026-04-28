package com.audioreactive.ui.viewmodel.intent

import android.net.Uri

sealed class AudioPlayerIntent: AudioReactiveIntent {
    class SetAudio(val uri: Uri): AudioPlayerIntent()
    class QueueAudio(val uri: Uri): AudioPlayerIntent()
    object TogglePlayback: AudioPlayerIntent()
    object Pause: AudioPlayerIntent()
    object Play: AudioPlayerIntent()
    object Stop: AudioPlayerIntent()
    object Previous: AudioPlayerIntent()
    object Next: AudioPlayerIntent()
}