package com.audioreactive.ui.viewmodel

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.serialization.saved
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Player.COMMAND_PREPARE
import androidx.media3.exoplayer.ExoPlayer
import com.audioreactive.ui.viewmodel.effect.AudioPlayerEffect
import com.audioreactive.ui.viewmodel.intent.AudioPlayerIntent
import com.audioreactive.ui.viewmodel.intent.AudioPlayerIntent.Next
import com.audioreactive.ui.viewmodel.intent.AudioPlayerIntent.Pause
import com.audioreactive.ui.viewmodel.intent.AudioPlayerIntent.Play
import com.audioreactive.ui.viewmodel.intent.AudioPlayerIntent.Previous
import com.audioreactive.ui.viewmodel.intent.AudioPlayerIntent.Stop
import com.audioreactive.ui.viewmodel.intent.AudioPlayerIntent.TogglePlayback
import com.audioreactive.ui.viewmodel.intent.QueuedAudio
import com.audioreactive.ui.viewmodel.state.AudioPlayerState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    private var progressJob: Job? = null

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
                updatePlaybackState()

                if (isPlaying) {
                    startProgressUpdates()
                } else {
                    stopProgressUpdates()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updatePlaybackState()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                updatePlaybackState()
            }

            override fun onTimelineChanged(timeline: androidx.media3.common.Timeline, reason: Int) {
                updatePlaybackState()
            }

            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {

                Log.d(LOG_TAG, "metadata title = ${mediaMetadata.title}")

                val title = mediaMetadata.title?.toString()
                    ?: _player.currentMediaItem?.mediaMetadata?.title?.toString()
                    ?: _savedState.queueTitles.getOrNull(_player.currentMediaItemIndex)
                    ?: "Unknown Song"

                _stateFlow.update {
                    _savedState.copy(
                        songTitle = title
                    ).also { _savedState = it }
                }

                if (mediaMetadata.artworkData != null) {
                    Log.d(LOG_TAG, "File has an image")
                    viewModelScope.launch {
                        _effectFlow.update {
                            AudioPlayerEffect.ImageChanged(
                                BitmapFactory.decodeByteArray(
                                    mediaMetadata.artworkData!!,
                                    0,
                                    mediaMetadata.artworkData!!.size
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

        updatePlaybackState()
    }

    override fun onCleared() {
        Log.d(LOG_TAG, "onCleared() called")
        stopProgressUpdates()
        _player.release()
        super.onCleared()
    }

    override fun handleIntent(intent: AudioPlayerIntent) {
        when (intent) {
            is AudioPlayerIntent.SetQueue -> setQueue(intent.songs)
            is AudioPlayerIntent.QueueAudio -> queueAudio(intent.song)
            is AudioPlayerIntent.SelectQueueIndex -> selectQueueIndex(intent.index)
            TogglePlayback -> togglePlayback()
            Play -> play()
            Pause -> pause()
            Next -> playNext()
            Previous -> playPrevious()
            Stop -> stop()
        }
    }

    private fun togglePlayback() {
        if (_player.currentMediaItem == null) return
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

    private fun createMediaItem(song: QueuedAudio): MediaItem {
        return MediaItem.Builder()
            .setUri(song.uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .build()
            )
            .build()
    }

    private fun setQueue(songs: List<QueuedAudio>) {
        if (songs.isEmpty()) return

        val mediaItems = songs.map { createMediaItem(it) }

        _player.setMediaItems(mediaItems)
        if (_player.isCommandAvailable(COMMAND_PREPARE)) {
            _player.prepare()
            _player.play()
        }

        updatePlaybackState()
    }

    private fun queueAudio(song: QueuedAudio) {
        val shouldStartPlaying = _player.mediaItemCount == 0

        _player.addMediaItem(createMediaItem(song))

        if (_player.isCommandAvailable(COMMAND_PREPARE)) {
            _player.prepare()
        }

        if (shouldStartPlaying) {
            _player.play()
        }

        updatePlaybackState()
    }

    private fun selectQueueIndex(index: Int) {
        if (index in 0 until _player.mediaItemCount) {
            _player.seekTo(index, 0L)
            _player.play()
            updatePlaybackState()
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
        if (_player.mediaItemCount > 0 && _player.hasNextMediaItem()) {
            _player.seekToNextMediaItem()
            _player.play()
        }
    }

    private fun startProgressUpdates() {
        if (progressJob?.isActive == true) return

        progressJob = viewModelScope.launch {
            while (true) {
                updatePlaybackState()
                delay(500)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
        updatePlaybackState()
    }

    private fun updatePlaybackState() {
        val duration = if (_player.duration == C.TIME_UNSET || _player.duration <= 0L) {
            0L
        } else {
            _player.duration
        }

        val position = if (duration > 0L) {
            _player.currentPosition
                .coerceAtLeast(0L)
                .coerceAtMost(duration)
        } else {
            0L
        }

        val title = _player.mediaMetadata.title?.toString()
            ?: _savedState.songTitle

        val queueTitles = (0 until _player.mediaItemCount).map { index ->
            _player.getMediaItemAt(index).mediaMetadata.title?.toString()
                ?: _player.getMediaItemAt(index).localConfiguration?.uri?.lastPathSegment
                ?: "Unknown Song"
        }

        val currentIndex = if (_player.currentMediaItemIndex >= 0) {
            _player.currentMediaItemIndex
        } else {
            0
        }

        _stateFlow.update {
            _savedState.copy(
                isPlaying = _player.isPlaying,
                hasAudioLoaded = _player.currentMediaItem != null,
                hasNext = _player.hasNextMediaItem(),
                songTitle = title,
                queueTitles = queueTitles,
                currentQueueIndex = currentIndex,
                currentPositionMs = position,
                durationMs = duration
            ).also { _savedState = it }
        }
    }
}
