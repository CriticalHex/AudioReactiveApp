package com.audioreactive.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.serialization.saved
import com.audioreactive.ui.viewmodel.effect.LatticeEffect
import com.audioreactive.ui.viewmodel.intent.LatticeIntent
import com.audioreactive.ui.viewmodel.state.LatticeState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LatticeViewModel
internal constructor(
    savedStateHandle: SavedStateHandle
): ViewModel(), IViewModelContract<LatticeState, LatticeIntent, LatticeEffect> {
    companion object {
        private const val LOG_TAG = "AR.LatticeViewModel"
        private const val TIME_SCALE: Double = 0.1
    }

    private var _savedState: LatticeState by savedStateHandle.saved(
        key = "SAVED_LATTICE_STATE",
        init = { LatticeState() }
    )

    private val _stateFlow: MutableStateFlow<LatticeState> = MutableStateFlow(_savedState)
    override val stateFlow: StateFlow<LatticeState> = _stateFlow.asStateFlow()

    private val _effectFlow: MutableStateFlow<LatticeEffect?> = MutableStateFlow(null)
    override val effectFlow: SharedFlow<LatticeEffect?> = _effectFlow.asSharedFlow()

    override fun handleIntent(intent: LatticeIntent) {
        when (intent) {
            is LatticeIntent.CalculateTime -> {
                if (_savedState.startTimeInNano == null) {
                    _stateFlow.update {
                        _savedState.copy(
                            startTimeInNano = intent.currentTimeInNano,
                            timeInSeconds = _savedState.accumulatedTimeInSeconds
                        ).also { _savedState = it }
                    }
                } else {
                    _stateFlow.update {
                        _savedState.copy(
                            timeInSeconds =
                                _savedState.accumulatedTimeInSeconds + (
                                    (intent.currentTimeInNano - _savedState.startTimeInNano!!
                                ) / 1_000_000_000.0) * TIME_SCALE
                        ).also { _savedState = it }
                    }
                }
            }
            is LatticeIntent.Pause -> {
                if (_savedState.startTimeInNano != null) {
                    _stateFlow.update {
                        _savedState.copy(
                            startTimeInNano = null,
                            accumulatedTimeInSeconds =
                                _savedState.accumulatedTimeInSeconds + (
                                        (intent.currentTimeInNano - _savedState.startTimeInNano!!
                                ) / 1_000_000_000.0) * TIME_SCALE
                        ).also { _savedState = it }
                    }
                }
            }
            LatticeIntent.Reset -> _stateFlow.update {
                _savedState.copy(
                    startTimeInNano = null,
                    accumulatedTimeInSeconds = 0.0
                ).also { _savedState = it }
            }
        }
    }
}