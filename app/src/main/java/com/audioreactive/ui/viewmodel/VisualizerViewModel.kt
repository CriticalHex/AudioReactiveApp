package com.audioreactive.ui.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.serialization.saved
import androidx.lifecycle.viewModelScope
import com.audioreactive.MainActivity
import com.audioreactive.data.AudioReactiveRepo
import com.audioreactive.ui.viewmodel.effect.VisualizerEffect
import com.audioreactive.ui.viewmodel.intent.VisualizerIntent
import com.audioreactive.ui.viewmodel.intent.VisualizerIntent.SetBackgroundImage
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VisualizerViewModel
internal constructor(
    val audioReactiveRepo: AudioReactiveRepo, savedStateHandle: SavedStateHandle
) : ViewModel(), IViewModelContract<VisualizerState, VisualizerIntent, VisualizerEffect> {
    companion object {
        private const val LOG_TAG = "AR.VisualizerViewModel"
        const val CUSTOM_IMAGE_NAME = "custom_background.jpg"
    }

    private var _savedState: VisualizerState by savedStateHandle.saved(
        key = "SAVED_VISUALIZER_STATE", init = { VisualizerState() })

    init {
        viewModelScope.launch(Dispatchers.IO) {
            audioReactiveRepo.getSettingsFlow().collectLatest { settings ->
                _stateFlow.update { state ->
                    state.copy(
                        customImage = settings.customImage,
                        barColorMode = settings.barColorMode,
                        solidBarColorArgb = settings.solidBarColorArgb,
                        disableBars = settings.disableBars,
                        barRiseSpeed = settings.barRiseSpeed,
                        barFallSpeed = settings.barFallSpeed,
                        barSensitivity = settings.barSensitivity,
                        barSoundMode = settings.barSoundMode,
                        barMaxHeight = settings.barMaxHeight,
                        barCount = settings.barCount,
                        barOpacity = settings.barOpacity,
                    ).also { _savedState = it }
                }
            }
        }
    }

    private val _stateFlow: MutableStateFlow<VisualizerState> = MutableStateFlow(_savedState)
    override val stateFlow: StateFlow<VisualizerState> =
        _stateFlow.combine(audioReactiveRepo.serviceRunning) { currentState, serviceRunning ->
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

    private fun updateSettings(state: VisualizerState) {
        viewModelScope.launch(Dispatchers.IO) {
            audioReactiveRepo.updateSettings(
                audioReactiveRepo.getSettings().copy(
                    customImage = state.customImage,
                    barColorMode = state.barColorMode,
                    solidBarColorArgb = state.solidBarColorArgb,
                    disableBars = state.disableBars,
                    barRiseSpeed = state.barRiseSpeed,
                    barFallSpeed = state.barFallSpeed,
                    barSensitivity = state.barSensitivity,
                    barSoundMode = state.barSoundMode,
                    barMaxHeight = state.barMaxHeight,
                    barCount = state.barCount,
                    barOpacity = state.barOpacity,
                )
            )
        }
    }

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

            is SetBackgroundImage -> {
                updateSettings(
                    _savedState.copy(
                        customImage = intent.customImage
                    )
                )
            }

            // Updates visualizer bar color and visibility
            is SetBarColorMode -> {
                updateSettings(
                    _savedState.copy(
                        barColorMode = intent.mode
                    )
                )
            }

            is SetSolidBarColor -> {
                updateSettings(
                    _savedState.copy(
                        solidBarColorArgb = intent.colorArgb
                    )
                )
            }

            is SetBarsDisabled -> {
                updateSettings(
                    _savedState.copy(
                        disableBars = intent.disabled
                    )
                )
            }

            is SetBarRiseSpeed -> {
                updateSettings(
                    _savedState.copy(
                        barRiseSpeed = intent.speed.coerceIn(0.1f, 3f)
                    )
                )
            }

            is SetBarFallSpeed -> {
                updateSettings(
                    _savedState.copy(
                        barFallSpeed = intent.speed.coerceIn(0.1f, 3f)
                    )
                )
            }

            is SetBarSensitivity -> {
                updateSettings(
                    _savedState.copy(
                        barSensitivity = intent.sensitivity.coerceIn(0.1f, 3f)
                    )
                )
            }

            is VisualizerIntent.SetBarSoundMode -> {
                updateSettings(
                    _savedState.copy(
                        barSoundMode = intent.mode
                    )
                )
            }

            is VisualizerIntent.SetBarMaxHeight -> {
                updateSettings(
                    _savedState.copy(
                        barMaxHeight = intent.maxHeight.coerceIn(0.1f, 1f)
                    )
                )
            }

            is VisualizerIntent.SetBarCount -> {
                updateSettings(
                    _savedState.copy(
                        barCount = intent.count.coerceIn(
                            VisualizerDefaults.BAR_COUNT_MIN, VisualizerDefaults.BAR_COUNT_MAX
                        )
                    )
                )
            }

            is VisualizerIntent.SetBarOpacity -> {
                updateSettings(
                    _savedState.copy(
                        barOpacity = intent.opacity.coerceIn(0.1f, 1f)
                    )
                )
            }

            VisualizerIntent.ResetSettings -> {
                updateSettings(
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
                    )
                )
            }
        }
    }
}
