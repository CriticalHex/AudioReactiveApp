package com.audioreactive.ui.viewmodel.intent

import com.audioreactive.ui.viewmodel.state.VisualizerBarColorMode

sealed class VisualizerIntent : AudioReactiveIntent {
    class UpdateSpectrum(val spectrum: FloatArray) : VisualizerIntent()
    class UpdateVolume(val volume: Float) : VisualizerIntent()

    class SetBarColorMode(val mode: VisualizerBarColorMode) : VisualizerIntent()
    class SetSolidBarColor(val colorArgb: Int) : VisualizerIntent()
    class SetBarsDisabled(val disabled: Boolean) : VisualizerIntent()
}
