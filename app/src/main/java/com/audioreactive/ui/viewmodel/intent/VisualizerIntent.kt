package com.audioreactive.ui.viewmodel.intent

sealed class VisualizerIntent: AudioReactiveIntent {
    class UpdateSpectrum(val spectrum: FloatArray): VisualizerIntent()
    class UpdateVolume(val volume: Float): VisualizerIntent()
}