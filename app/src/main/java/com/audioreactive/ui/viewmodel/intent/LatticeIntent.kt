package com.audioreactive.ui.viewmodel.intent

import com.audioreactive.ui.viewmodel.state.LatticeColorMode
import com.audioreactive.ui.viewmodel.state.LatticeLineDensity

sealed class LatticeIntent : AudioReactiveIntent {
    class CalculateTime(val currentTimeInNano: Long) : LatticeIntent()
    class Pause(val currentTimeInNano: Long) : LatticeIntent()
    object Reset : LatticeIntent()

    class SetLatticeColorMode(val mode: LatticeColorMode) : LatticeIntent()
    class SetSolidColor(val colorArgb: Int) : LatticeIntent()
    class SetLatticeDisabled(val disabled: Boolean) : LatticeIntent()
    class SetGyrosDisabled(val disabled: Boolean) : LatticeIntent()
    class SetSpeed(val speed: Float) : LatticeIntent()
    class SetSensitivity(val sensitivity: Float) : LatticeIntent()
    class SetLineDensity(val density: LatticeLineDensity) : LatticeIntent()
    class SetInvertGyroSpin(val invert: Boolean) : LatticeIntent()
    class SetInvertGyroHorizontal(val invert: Boolean) : LatticeIntent()
    class SetInvertGyroVertical(val invert: Boolean) : LatticeIntent()
    object ResetSettings : LatticeIntent()
}
