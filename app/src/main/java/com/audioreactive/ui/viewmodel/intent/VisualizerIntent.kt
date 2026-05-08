package com.audioreactive.ui.viewmodel.intent

import com.audioreactive.ui.viewmodel.state.VisualizerBarColorMode
import com.audioreactive.ui.viewmodel.state.VisualizerBarSoundMode

sealed class VisualizerIntent : AudioReactiveIntent {
    class UpdateSpectrum(val spectrum: FloatArray) : VisualizerIntent()
    class UpdateVolume(val volume: Float) : VisualizerIntent()
    class SetBackgroundImage(val customImage: Boolean) : VisualizerIntent()

    class SetBarColorMode(val mode: VisualizerBarColorMode) : VisualizerIntent()
    class SetSolidBarColor(val colorArgb: Int) : VisualizerIntent()
    class SetBarsDisabled(val disabled: Boolean) : VisualizerIntent()
    class SetBarRiseSpeed(val speed: Float) : VisualizerIntent()
    class SetBarFallSpeed(val speed: Float) : VisualizerIntent()
    class SetBarSensitivity(val sensitivity: Float) : VisualizerIntent()
    class SetBarSoundMode(val mode: VisualizerBarSoundMode) : VisualizerIntent()
    class SetBarMaxHeight(val maxHeight: Float) : VisualizerIntent()
    class SetBarCount(val count: Int) : VisualizerIntent()
    class SetBarOpacity(val opacity: Float) : VisualizerIntent()
    object ResetSettings : VisualizerIntent()
}
