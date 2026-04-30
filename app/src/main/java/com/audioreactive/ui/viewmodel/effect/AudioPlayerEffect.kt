package com.audioreactive.ui.viewmodel.effect

import androidx.compose.ui.graphics.ImageBitmap

sealed class AudioPlayerEffect: AudioReactiveEffect {
    class ImageChanged(val imageBitmap: ImageBitmap?): AudioPlayerEffect()
}