package com.audioreactive.ui.viewmodel

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.serialization.saved
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Player.COMMAND_PREPARE
import androidx.media3.exoplayer.ExoPlayer
import com.audioreactive.ui.viewmodel.effect.AudioPlayerEffect
import com.audioreactive.ui.viewmodel.intent.AudioPlayerIntent
import com.audioreactive.ui.viewmodel.intent.AudioPlayerIntent.SetAudio
import com.audioreactive.ui.viewmodel.intent.AudioPlayerIntent.Next
import com.audioreactive.ui.viewmodel.intent.AudioPlayerIntent.Pause
import com.audioreactive.ui.viewmodel.intent.AudioPlayerIntent.Play
import com.audioreactive.ui.viewmodel.intent.AudioPlayerIntent.Previous
import com.audioreactive.ui.viewmodel.intent.AudioPlayerIntent.Stop
import com.audioreactive.ui.viewmodel.intent.AudioPlayerIntent.TogglePlayback
import com.audioreactive.ui.viewmodel.state.AudioPlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AudioPlayerViewModel
internal constructor(
    private val _player: ExoPlayer,
    savedStateHandle: SavedStateHandle
): ViewModel(), IViewModelContract<AudioPlayerState, AudioPlayerIntent, AudioPlayerEffect> {
    companion object {
        private const val LOG_TAG = "AR.AudioPlayerViewModel"
    }

    private var _savedState: AudioPlayerState by savedStateHandle.saved(
        key = "SAVED_AUDIO_PLAYER_STATE",
        init = { AudioPlayerState() }
    )

    private val _stateFlow: MutableStateFlow<AudioPlayerState> = MutableStateFlow(_savedState)
    override val stateFlow: StateFlow<AudioPlayerState> = _stateFlow.asStateFlow()

    private val _effectFlow: MutableStateFlow<AudioPlayerEffect?> = MutableStateFlow(null)
    override val effectFlow: SharedFlow<AudioPlayerEffect?> = _effectFlow.asSharedFlow()

    init {
        _player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _stateFlow.update {
                    _savedState.copy(
                        isPlaying = isPlaying
                    ).also { _savedState = it }
                }
            }

            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                if (mediaMetadata.artworkData != null) {
                    Log.d(LOG_TAG, "File has an image")
                    viewModelScope.launch {
                        _effectFlow.update {
                            AudioPlayerEffect.ImageChanged(
                                BitmapFactory.decodeByteArray(
                                    mediaMetadata.artworkData!!, 0, mediaMetadata.artworkData!!.size
                                ).asImageBitmap()
                            )
                        }
                    }
                } else {
                    Log.d(LOG_TAG, "File has no image")
                    _effectFlow.update {
                        AudioPlayerEffect.ImageChanged(null)
                    }
                }
            }
        })
    }

    override fun onCleared() {
        Log.d(LOG_TAG, "onCleared() called")
        _player.release()
        super.onCleared()
    }

    override fun handleIntent(intent: AudioPlayerIntent) {
        when (intent) {
            is SetAudio -> setAudio(intent.uri)
            is AudioPlayerIntent.QueueAudio -> queueAudio(intent.uri)
            TogglePlayback -> togglePlayback()
            Play -> play()
            Pause -> pause()
            Next -> playNext()
            Previous -> playPrevious()
            Stop -> stop()
        }
    }

    private fun togglePlayback() {
        if (_player.isPlaying) pause() else play()
    }

    private fun pause() {
        if (_player.currentMediaItem != null)
            _player.pause()
    }

    private fun play() {
        if (_player.currentMediaItem != null)
            _player.play()
    }

    private fun stop() {
        if (_player.currentMediaItem != null)
            _player.stop()
    }

    private fun setAudio(uri: Uri) {
        val mediaItem = MediaItem.fromUri(uri)
        _player.setMediaItem(mediaItem)
        if (_player.isCommandAvailable(COMMAND_PREPARE)) {
            _player.prepare()
            _player.play()
        }
    }

    private fun queueAudio(uri: Uri) {
        val mediaItem = MediaItem.fromUri(uri)
        _player.addMediaItem(mediaItem)
        if (_player.isCommandAvailable(COMMAND_PREPARE)) {
            _player.prepare()
        }
    }

    private fun playPrevious() {
        if (_player.mediaItemCount > 0) {
            if (_player.hasPreviousMediaItem()) {
                _player.seekToPreviousMediaItem()
            } else {
                _player.seekTo(0)
            }
            _player.play()
        }
    }

    private fun playNext() {
        if (_player.mediaItemCount > 0) {
            if (_player.hasNextMediaItem()) {
                _player.seekToNextMediaItem()
                _player.play()
            }
        }
    }
}
