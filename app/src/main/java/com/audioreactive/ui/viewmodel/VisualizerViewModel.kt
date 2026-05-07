package com.audioreactive.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.serialization.saved
import androidx.lifecycle.viewModelScope
import com.audioreactive.data.AudioReactiveRepo
import com.audioreactive.ui.viewmodel.effect.VisualizerEffect
import com.audioreactive.ui.viewmodel.intent.VisualizerIntent
import com.audioreactive.ui.viewmodel.intent.VisualizerIntent.SetBarColorMode
import com.audioreactive.ui.viewmodel.intent.VisualizerIntent.SetBarFallSpeed
import com.audioreactive.ui.viewmodel.intent.VisualizerIntent.SetBarRiseSpeed
import com.audioreactive.ui.viewmodel.intent.VisualizerIntent.SetBarSensitivity
import com.audioreactive.ui.viewmodel.intent.VisualizerIntent.SetBarsDisabled
import com.audioreactive.ui.viewmodel.intent.VisualizerIntent.SetSolidBarColor
import com.audioreactive.ui.viewmodel.intent.VisualizerIntent.UpdateSpectrum
import com.audioreactive.ui.viewmodel.intent.VisualizerIntent.UpdateVolume
import com.audioreactive.ui.viewmodel.state.VisualizerDefaults
import com.audioreactive.ui.viewmodel.state.VisualizerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class VisualizerViewModel
internal constructor(
    audioReactiveRepo: AudioReactiveRepo,
    savedStateHandle: SavedStateHandle
) : ViewModel(), IViewModelContract<VisualizerState, VisualizerIntent, VisualizerEffect> {
    companion object {
        private const val LOG_TAG = "AR.VisualizerViewModel"
    }

    private var _savedState: VisualizerState by savedStateHandle.saved(
        key = "SAVED_VISUALIZER_STATE",
        init = { VisualizerState() }
    )

    private val _stateFlow: MutableStateFlow<VisualizerState> = MutableStateFlow(_savedState)
    override val stateFlow: StateFlow<VisualizerState> = _stateFlow
        .combine(audioReactiveRepo.serviceRunning) { currentState, serviceRunning ->
            currentState.copy(running = serviceRunning).also {
                _savedState = it
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = _savedState
        )

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

            // Updates visualizer bar color and visibility
            is SetBarColorMode -> {
                _stateFlow.update {
                    _savedState.copy(
                        barColorMode = intent.mode
                    ).also { _savedState = it }
                }
            }

            is SetSolidBarColor -> {
                _stateFlow.update {
                    _savedState.copy(
                        solidBarColorArgb = intent.colorArgb
                    ).also { _savedState = it }
                }
            }

            is SetBarsDisabled -> {
                _stateFlow.update {
                    _savedState.copy(
                        disableBars = intent.disabled
                    ).also { _savedState = it }
                }
            }

            is SetBarRiseSpeed -> {
                _stateFlow.update {
                    _savedState.copy(
                        barRiseSpeed = intent.speed.coerceIn(0.1f, 3f)
                    ).also { _savedState = it }
                }
            }

            is SetBarFallSpeed -> {
                _stateFlow.update {
                    _savedState.copy(
                        barFallSpeed = intent.speed.coerceIn(0.1f, 3f)
                    ).also { _savedState = it }
                }
            }

            is SetBarSensitivity -> {
                _stateFlow.update {
                    _savedState.copy(
                        barSensitivity = intent.sensitivity.coerceIn(0.1f, 3f)
                    ).also { _savedState = it }
                }
            }

            is VisualizerIntent.SetBarSoundMode -> {
                _stateFlow.update {
                    _savedState.copy(
                        barSoundMode = intent.mode
                    ).also { _savedState = it }
                }
            }

            is VisualizerIntent.SetBarMaxHeight -> {
                _stateFlow.update {
                    _savedState.copy(
                        barMaxHeight = intent.maxHeight.coerceIn(0.1f, 1f)
                    ).also { _savedState = it }
                }
            }

            is VisualizerIntent.SetBarCount -> {
                _stateFlow.update {
                    _savedState.copy(
                        barCount = intent.count.coerceIn(
                            VisualizerDefaults.BAR_COUNT_MIN,
                            VisualizerDefaults.BAR_COUNT_MAX
                        )
                    ).also { _savedState = it }
                }
            }

            is VisualizerIntent.SetBarOpacity -> {
                _stateFlow.update {
                    _savedState.copy(
                        barOpacity = intent.opacity.coerceIn(0.1f, 1f)
                    ).also { _savedState = it }
                }
            }

            VisualizerIntent.ResetSettings -> {
                _stateFlow.update {
                    _savedState.copy(
                        barColorMode = VisualizerDefaults.BAR_COLOR_MODE,
                        solidBarColorArgb = VisualizerDefaults.SOLID_BAR_COLOR_ARGB,
                        disableBars = VisualizerDefaults.DISABLE_BARS,
                        barRiseSpeed = VisualizerDefaults.BAR_RISE_SPEED,
                        barFallSpeed = VisualizerDefaults.BAR_FALL_SPEED,
                        barSensitivity = VisualizerDefaults.BAR_SENSITIVITY,
                        barSoundMode = VisualizerDefaults.BAR_SOUND_MODE,
                        barMaxHeight = VisualizerDefaults.BAR_MAX_HEIGHT,
                        barCount = VisualizerDefaults.BAR_COUNT,
                        barOpacity = VisualizerDefaults.BAR_OPACITY
                    ).also { _savedState = it }
                }
            }
        }
    }
}
