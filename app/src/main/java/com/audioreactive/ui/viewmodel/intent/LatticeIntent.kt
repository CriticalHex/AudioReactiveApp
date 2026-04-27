package com.audioreactive.ui.viewmodel.intent

import com.audioreactive.ui.viewmodel.state.LatticeColorMode

sealed class LatticeIntent : AudioReactiveIntent {
    class CalculateTime(val currentTimeInNano: Long) : LatticeIntent()
    class Pause(val currentTimeInNano: Long) : LatticeIntent()
    object Reset : LatticeIntent()

    class SetLatticeColorMode(val mode: LatticeColorMode) : LatticeIntent()
    class SetSolidColor(val colorArgb: Int) : LatticeIntent()
    class SetLatticeDisabled(val disabled: Boolean) : LatticeIntent()
    class SetGyrosDisabled(val disabled: Boolean) : LatticeIntent()
}
