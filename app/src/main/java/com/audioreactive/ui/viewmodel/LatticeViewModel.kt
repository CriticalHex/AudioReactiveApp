package com.audioreactive.ui.viewmodel

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.serialization.saved
import androidx.lifecycle.viewModelScope
import com.audioreactive.data.AudioReactiveRepo
import com.audioreactive.sensor.RotationSensorManager
import com.audioreactive.ui.reactor.Lattice
import com.audioreactive.ui.viewmodel.effect.LatticeEffect
import com.audioreactive.ui.viewmodel.intent.LatticeIntent
import com.audioreactive.ui.viewmodel.state.LatticeColorMode
import com.audioreactive.ui.viewmodel.state.LatticeDefaults
import com.audioreactive.ui.viewmodel.state.LatticeState
import com.audioreactive.ui.viewmodel.state.VisualizerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LatticeViewModel
internal constructor(
    appContext: Context,
    savedStateHandle: SavedStateHandle,
    val audioReactiveRepo: AudioReactiveRepo
) : ViewModel(), IViewModelContract<LatticeState, LatticeIntent, LatticeEffect> {
    companion object {
        private const val LOG_TAG = "AR.LatticeViewModel"
        private const val TIME_SCALE: Double = 0.1
    }

    private var _savedState: LatticeState by savedStateHandle.saved(
        key = "SAVED_LATTICE_STATE", init = { LatticeState() })

    private val _stateFlow: MutableStateFlow<LatticeState> = MutableStateFlow(_savedState)
    override val stateFlow: StateFlow<LatticeState> = _stateFlow.asStateFlow()

    private val _effectFlow: MutableStateFlow<LatticeEffect?> = MutableStateFlow(null)
    override val effectFlow: SharedFlow<LatticeEffect?> = _effectFlow.asSharedFlow()

    private val rotationSensorManager = RotationSensorManager(appContext)
    private var lattice: Lattice? = null
    private var sensorRunning = false

    init {
        viewModelScope.launch(Dispatchers.IO) {
            audioReactiveRepo.getSettingsFlow().collectLatest { settings ->
                _stateFlow.update { state ->
                    state.copy(
                        latticeColorMode = settings.latticeColorMode,
                        solidColorArgb = settings.solidColorArgb,
                        disableLattice = settings.disableLattice,
                        disableGyros = settings.disableGyros,
                        speed = settings.speed,
                        sensitivity = settings.sensitivity,
                        lineDensity = settings.lineDensity,
                        invertGyroSpin = settings.invertGyroSpin,
                        invertGyroHorizontal = settings.invertGyroHorizontal,
                        invertGyroVertical = settings.invertGyroVertical,
                    ).also { _savedState = it }
                }
            }
        }
    }

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
                LatticeColorMode.DEFAULT, LatticeColorMode.DIMENSION_CYCLE -> l.clearColorOverride()
            }
            lattice = l
        }
    }

    fun applySensorState(wantRunning: Boolean = !_savedState.disableGyros) {
        val l = lattice ?: return
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

    private fun updateSettings(state: LatticeState) {
        viewModelScope.launch(Dispatchers.IO) {
            audioReactiveRepo.updateSettings(
                audioReactiveRepo.getSettings().copy(
                    latticeColorMode = state.latticeColorMode,
                    solidColorArgb = state.solidColorArgb,
                    disableLattice = state.disableLattice,
                    disableGyros = state.disableGyros,
                    speed = state.speed,
                    sensitivity = state.sensitivity,
                    lineDensity = state.lineDensity,
                    invertGyroSpin = state.invertGyroSpin,
                    invertGyroHorizontal = state.invertGyroHorizontal,
                    invertGyroVertical = state.invertGyroVertical,
                )
            )
        }
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
                            timeInSeconds = _savedState.accumulatedTimeInSeconds + ((intent.currentTimeInNano - _savedState.startTimeInNano!!) / 1_000_000_000.0) * TIME_SCALE
                        ).also { _savedState = it }
                    }
                }
            }

            is LatticeIntent.Pause -> {
                if (_savedState.startTimeInNano != null) {
                    _stateFlow.update {
                        _savedState.copy(
                            startTimeInNano = null,
                            accumulatedTimeInSeconds = _savedState.accumulatedTimeInSeconds + ((intent.currentTimeInNano - _savedState.startTimeInNano!!) / 1_000_000_000.0) * TIME_SCALE
                        ).also { _savedState = it }
                    }
                }
            }

            LatticeIntent.Reset -> {
                _stateFlow.update {
                    _savedState.copy(
                        startTimeInNano = null, accumulatedTimeInSeconds = 0.0
                    ).also { _savedState = it }
                }
            }

            is LatticeIntent.SetLatticeColorMode -> {
                updateSettings(
                    _savedState.copy(
                        latticeColorMode = intent.mode
                    )
                )
            }

            is LatticeIntent.SetSolidColor -> {
                updateSettings(
                    _savedState.copy(
                        solidColorArgb = intent.colorArgb
                    )
                )
            }

            is LatticeIntent.SetLatticeDisabled -> {
                updateSettings(
                    _savedState.copy(
                        disableLattice = intent.disabled
                    )
                )
            }

            is LatticeIntent.SetGyrosDisabled -> {
                updateSettings(
                    _savedState.copy(
                        disableGyros = intent.disabled
                    )
                )
                applySensorState(intent.disabled)
            }

            is LatticeIntent.SetSpeed -> {
                val speed = intent.speed.coerceIn(0.05f, 2f)
                updateSettings(
                    _savedState.copy(
                        speed = speed
                    )
                )
                lattice?.speed = speed.toDouble()
            }

            is LatticeIntent.SetSensitivity -> {
                val sensitivity = intent.sensitivity.coerceIn(0.1f, 3f)
                updateSettings(
                    _savedState.copy(
                        sensitivity = sensitivity
                    )
                )
                lattice?.sensitivity = sensitivity
            }

            is LatticeIntent.SetLineDensity -> {
                updateSettings(
                    _savedState.copy(
                        lineDensity = intent.density
                    )
                )
            }

            is LatticeIntent.SetInvertGyroSpin -> {
                updateSettings(
                    _savedState.copy(
                        invertGyroSpin = intent.invert
                    )
                )
                lattice?.invertGyroSpin = intent.invert
            }

            is LatticeIntent.SetInvertGyroHorizontal -> {
                updateSettings(
                    _savedState.copy(
                        invertGyroHorizontal = intent.invert
                    )
                )
                lattice?.invertGyroHorizontal = intent.invert
            }

            is LatticeIntent.SetInvertGyroVertical -> {
                updateSettings(
                    _savedState.copy(
                        invertGyroVertical = intent.invert
                    )
                )
                lattice?.invertGyroVertical = intent.invert
            }

            LatticeIntent.ResetSettings -> {
                updateSettings(
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
                    )
                )
                lattice?.let { l ->
                    l.speed = LatticeDefaults.SPEED.toDouble()
                    l.sensitivity = LatticeDefaults.SENSITIVITY
                    l.invertGyroSpin = LatticeDefaults.INVERT_GYRO_SPIN
                    l.invertGyroHorizontal = LatticeDefaults.INVERT_GYRO_HORIZONTAL
                    l.invertGyroVertical = LatticeDefaults.INVERT_GYRO_VERTICAL
                    when (LatticeDefaults.COLOR_MODE) {
                        LatticeColorMode.SOLID -> l.setColorOverride(Color(LatticeDefaults.SOLID_COLOR_ARGB))
                        LatticeColorMode.DEFAULT, LatticeColorMode.DIMENSION_CYCLE -> l.clearColorOverride()
                    }
                }
                applySensorState()
            }
        }
    }
}
