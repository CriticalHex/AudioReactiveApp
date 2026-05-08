package com.audioreactive.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.media3.common.util.UnstableApi
import com.audioreactive.data.AudioReactiveRepo
import com.audioreactive.player.AudioPlayer

class AudioReactiveViewModelFactory: ViewModelProvider.Factory {
    companion object {
        private const val LOG_TAG = "AR.AudioReactiveViewModelFactory"
        private val CONTEXT_KEY = object : CreationExtras.Key<Context> {}
        fun creationExtras(defaultCreationExtras: CreationExtras, context: Context) = MutableCreationExtras(defaultCreationExtras).apply {
            set(CONTEXT_KEY, context)
        }
    }

    @OptIn(UnstableApi::class)
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras
    ): T =
        with(modelClass) {
            when {
                isAssignableFrom(AudioPlayerViewModel::class.java) -> {
                    Log.d(LOG_TAG, "creating AudioPlayerViewModel")
                    val context = checkNotNull(extras[CONTEXT_KEY])
                    val savedStateHandle = extras.createSavedStateHandle()
                    AudioPlayerViewModel(
                        AudioPlayer.getInstance(context).player,
                        savedStateHandle
                    )
                }
                isAssignableFrom(LatticeViewModel::class.java) -> {
                    Log.d(LOG_TAG, "creating LatticeViewModel")
                    val context = checkNotNull(extras[CONTEXT_KEY])
                    val savedStateHandle = extras.createSavedStateHandle()
                    LatticeViewModel(
                        context.applicationContext,
                        savedStateHandle,
                        AudioReactiveRepo.getInstance(context)
                    )
                }
                isAssignableFrom(VisualizerViewModel::class.java) -> {
                    Log.d(LOG_TAG, "creating VisualizerViewModel")
                    val savedStateHandle = extras.createSavedStateHandle()
                    val context = checkNotNull(extras[CONTEXT_KEY])
                    VisualizerViewModel(
                        AudioReactiveRepo.getInstance(context),
                        savedStateHandle
                    )
                }
                else -> {
                    Log.e(LOG_TAG, "Unknown ViewModel: $modelClass")
                    throw IllegalArgumentException("Unknown ViewModel")
                }
            }
        } as T
}