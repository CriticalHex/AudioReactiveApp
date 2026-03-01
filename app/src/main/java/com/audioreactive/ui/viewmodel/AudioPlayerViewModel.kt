package com.audioreactive.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.compose.runtime.State
import androidx.media3.common.Player

class AudioPlayerViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val LOG_TAG = "AR.AudioPlayerVM"
    }

    private val _player = ExoPlayer.Builder(application).build()
    val player: ExoPlayer get() = _player

    // Compose playback state
    private val _isPlaying = mutableStateOf(false)
    val isPlaying: State<Boolean> = _isPlaying

    init {
        _player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }
        })
    }

    fun togglePlayback() {
        if (_player.isPlaying) pause() else play()
    }

    fun pause() {
        if (_player.currentMediaItem != null)
            _player.pause()
    }

    fun play() {
        if (_player.currentMediaItem != null)
            _player.play()
    }

    fun stop() {
        if (_player.currentMediaItem != null)
            _player.stop()
    }

    fun loadAudio(uri: Uri) {
        val mediaItem = MediaItem.fromUri(uri)
        _player.setMediaItem(mediaItem)
        _player.prepare()
        _player.play()
    }

    override fun onCleared() {
        Log.d(LOG_TAG, "onCleared() called")
        _player.release()
        super.onCleared()
    }
}
