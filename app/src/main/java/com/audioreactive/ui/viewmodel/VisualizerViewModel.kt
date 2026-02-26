package com.audioreactive.ui.viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.audioreactive.ui.viewmodel.VisualizerViewModel.VisualizerIntent.*

class VisualizerViewModel : ViewModel() {
    data class VisualizerState(val spectrum: FloatArray = FloatArray(0), val volume: Float = 0f)

    sealed class VisualizerIntent {
        class UpdateSpectrumIntent(val spectrum: FloatArray): VisualizerIntent()
        class UpdateVolumeIntent(val volume: Float): VisualizerIntent()
    }

    private val _state = mutableStateOf(VisualizerState())

    val state = derivedStateOf { _state.value }

    fun handleIntent(intent: VisualizerIntent) {
        when (intent) {
            is UpdateSpectrumIntent -> {
                _state.value = _state.value.copy(spectrum = intent.spectrum)
            }
            is UpdateVolumeIntent -> {
                _state.value = _state.value.copy(volume = intent.volume)
            }
        }
    }
}