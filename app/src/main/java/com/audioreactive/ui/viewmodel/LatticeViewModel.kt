package com.audioreactive.ui.viewmodel

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.serialization.saved
import com.audioreactive.sensor.RotationSensorManager
import com.audioreactive.ui.reactor.Lattice
import com.audioreactive.ui.viewmodel.effect.LatticeEffect
import com.audioreactive.ui.viewmodel.intent.LatticeIntent
import com.audioreactive.ui.viewmodel.state.LatticeColorMode
import com.audioreactive.ui.viewmodel.state.LatticeDefaults
import com.audioreactive.ui.viewmodel.state.LatticeState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LatticeViewModel
internal constructor(
    appContext: Context,
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

    private val rotationSensorManager = RotationSensorManager(appContext)
    private var lattice: Lattice? = null
    private var sensorRunning = false

    fun getOrCreateLattice(width: Int, height: Int): Lattice {
        val current = lattice
        if (current != null && current.width == width && current.height == height) {
            return current
        }
        return Lattice(width / 2, height / 2, width, height).also { l ->
            l.speed = _savedState.speed.toDouble()
            l.sensitivity = _savedState.sensitivity
            l.invertGyroSpin = _savedState.invertGyroSpin
            l.invertGyroHorizontal = _savedState.invertGyroHorizontal
            l.invertGyroVertical = _savedState.invertGyroVertical
            when (_savedState.latticeColorMode) {
                LatticeColorMode.SOLID -> l.setColorOverride(Color(_savedState.solidColorArgb))
                LatticeColorMode.DEFAULT, LatticeColorMode.DIMENSION_CYCLE ->
                    l.clearColorOverride()
            }
            lattice = l
        }
    }

    fun applySensorState() {
        val l = lattice ?: return
        val wantRunning = !_savedState.disableGyros
        if (wantRunning && !sensorRunning) {
            rotationSensorManager.start { matrix -> l.setRotation(matrix) }
            sensorRunning = true
        } else if (!wantRunning && sensorRunning) {
            rotationSensorManager.stop()
            sensorRunning = false
            l.setRotation(Lattice.IDENTITY_MATRIX)
        }
    }

    fun stopSensor() {
        if (sensorRunning) {
            rotationSensorManager.stop()
            sensorRunning = false
        }
    }

    override fun onCleared() {
        stopSensor()
        super.onCleared()
    }

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

            LatticeIntent.Reset -> {
                _stateFlow.update {
                    _savedState.copy(
                        startTimeInNano = null,
                        accumulatedTimeInSeconds = 0.0
                    ).also { _savedState = it }
                }
            }

            is LatticeIntent.SetLatticeColorMode -> {
                _stateFlow.update {
                    _savedState.copy(
                        latticeColorMode = intent.mode
                    ).also { _savedState = it }
                }
            }

            is LatticeIntent.SetSolidColor -> {
                _stateFlow.update {
                    _savedState.copy(
                        solidColorArgb = intent.colorArgb
                    ).also { _savedState = it }
                }
            }

            is LatticeIntent.SetLatticeDisabled -> {
                _stateFlow.update {
                    _savedState.copy(
                        disableLattice = intent.disabled
                    ).also { _savedState = it }
                }
            }

            is LatticeIntent.SetGyrosDisabled -> {
                _stateFlow.update {
                    _savedState.copy(
                        disableGyros = intent.disabled
                    ).also { _savedState = it }
                }
                applySensorState()
            }

            is LatticeIntent.SetSpeed -> {
                _stateFlow.update {
                    _savedState.copy(
                        speed = intent.speed.coerceIn(0.05f, 2f)
                    ).also { _savedState = it }
                }
                lattice?.speed = _savedState.speed.toDouble()
            }

            is LatticeIntent.SetSensitivity -> {
                _stateFlow.update {
                    _savedState.copy(
                        sensitivity = intent.sensitivity.coerceIn(0.1f, 3f)
                    ).also { _savedState = it }
                }
                lattice?.sensitivity = _savedState.sensitivity
            }

            is LatticeIntent.SetLineDensity -> {
                _stateFlow.update {
                    _savedState.copy(
                        lineDensity = intent.density
                    ).also { _savedState = it }
                }
            }

            is LatticeIntent.SetInvertGyroSpin -> {
                _stateFlow.update {
                    _savedState.copy(
                        invertGyroSpin = intent.invert
                    ).also { _savedState = it }
                }
                lattice?.invertGyroSpin = _savedState.invertGyroSpin
            }

            is LatticeIntent.SetInvertGyroHorizontal -> {
                _stateFlow.update {
                    _savedState.copy(
                        invertGyroHorizontal = intent.invert
                    ).also { _savedState = it }
                }
                lattice?.invertGyroHorizontal = _savedState.invertGyroHorizontal
            }

            is LatticeIntent.SetInvertGyroVertical -> {
                _stateFlow.update {
                    _savedState.copy(
                        invertGyroVertical = intent.invert
                    ).also { _savedState = it }
                }
                lattice?.invertGyroVertical = _savedState.invertGyroVertical
            }

            LatticeIntent.ResetSettings -> {
                _stateFlow.update {
                    _savedState.copy(
                        latticeColorMode = LatticeDefaults.COLOR_MODE,
                        solidColorArgb = LatticeDefaults.SOLID_COLOR_ARGB,
                        disableLattice = LatticeDefaults.DISABLE_LATTICE,
                        disableGyros = LatticeDefaults.DISABLE_GYROS,
                        speed = LatticeDefaults.SPEED,
                        sensitivity = LatticeDefaults.SENSITIVITY,
                        lineDensity = LatticeDefaults.LINE_DENSITY,
                        invertGyroSpin = LatticeDefaults.INVERT_GYRO_SPIN,
                        invertGyroHorizontal = LatticeDefaults.INVERT_GYRO_HORIZONTAL,
                        invertGyroVertical = LatticeDefaults.INVERT_GYRO_VERTICAL
                    ).also { _savedState = it }
                }
                lattice?.let { l ->
                    l.speed = _savedState.speed.toDouble()
                    l.sensitivity = _savedState.sensitivity
                    l.invertGyroSpin = _savedState.invertGyroSpin
                    l.invertGyroHorizontal = _savedState.invertGyroHorizontal
                    l.invertGyroVertical = _savedState.invertGyroVertical
                    when (_savedState.latticeColorMode) {
                        LatticeColorMode.SOLID -> l.setColorOverride(Color(_savedState.solidColorArgb))
                        LatticeColorMode.DEFAULT, LatticeColorMode.DIMENSION_CYCLE ->
                            l.clearColorOverride()
                    }
                }
                applySensorState()
            }
        }
    }
}
