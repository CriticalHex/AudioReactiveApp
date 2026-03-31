package com.audioreactive.ui.viewmodel.intent

sealed class LatticeIntent: AudioReactiveIntent {
    class CalculateTime(val currentTimeInNano: Long): LatticeIntent()
    class Pause(val currentTimeInNano: Long): LatticeIntent()
    object Reset: LatticeIntent()
}