package com.audioreactive.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.serialization.saved
import com.audioreactive.ui.viewmodel.effect.VisualizerEffect
import com.audioreactive.ui.viewmodel.intent.VisualizerIntent
import com.audioreactive.ui.viewmodel.intent.VisualizerIntent.UpdateSpectrum
import com.audioreactive.ui.viewmodel.intent.VisualizerIntent.UpdateVolume
import com.audioreactive.ui.viewmodel.state.VisualizerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class VisualizerViewModel
internal constructor(
    savedStateHandle: SavedStateHandle
): ViewModel(), IViewModelContract<VisualizerState, VisualizerIntent, VisualizerEffect> {
    companion object {
        private const val LOG_TAG = "AR.VisualizerViewModel"
    }

    private var _savedState: VisualizerState by savedStateHandle.saved(
        key = "SAVED_VISUALIZER_STATE",
        init = { VisualizerState() }
    )

    private val _stateFlow: MutableStateFlow<VisualizerState> = MutableStateFlow(_savedState)
    override val stateFlow: StateFlow<VisualizerState> = _stateFlow.asStateFlow()

    private val _effectFlow: MutableStateFlow<VisualizerEffect?> = MutableStateFlow(null)
    override val effectFlow: SharedFlow<VisualizerEffect?> = _effectFlow.asSharedFlow()

    override fun handleIntent(intent: VisualizerIntent) {
        when (intent) {
            is UpdateSpectrum -> {
                _stateFlow.update {
                    _savedState.copy(
                        spectrum = intent.spectrum
                    ).also { _savedState = it }
                }
            }
            is UpdateVolume -> {
                _stateFlow.update {
                    _savedState.copy(
                        volume = intent.volume
                    ).also { _savedState = it }
                }
            }
        }
    }
}