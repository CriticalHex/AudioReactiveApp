package com.audioreactive.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import com.audioreactive.ui.reactor.AnimatedLatticeDisplay
import com.audioreactive.ui.viewmodel.LatticeViewModel
import com.audioreactive.ui.viewmodel.state.LatticeColorMode
import com.audioreactive.ui.viewmodel.state.LatticeLineDensity

@Composable
fun VisualizerLattice(
    modifier: Modifier = Modifier,
    spectrum: FloatArray,
    timeInSeconds: Double,
    latticeColorMode: LatticeColorMode,
    solidColorArgb: Int,
    speed: Float,
    sensitivity: Float,
    lineDensity: LatticeLineDensity,
    latticeViewModel: LatticeViewModel,
    onCalculateTime: (Long) -> Unit
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val wPx = with(density) { maxWidth.toPx() }.toInt().coerceAtLeast(1)
        val hPx = with(density) { maxHeight.toPx() }.toInt().coerceAtLeast(1)

        val l = remember(wPx, hPx, latticeViewModel) {
            latticeViewModel.getOrCreateLattice(wPx, hPx)
        }

        LaunchedEffect(l, latticeColorMode, solidColorArgb) {
            when (latticeColorMode) {
                LatticeColorMode.SOLID -> l.setColorOverride(Color(solidColorArgb))
                LatticeColorMode.DEFAULT, LatticeColorMode.DIMENSION_CYCLE ->
                    l.clearColorOverride()
            }
        }
        LaunchedEffect(l, speed) { l.speed = speed.toDouble() }
        LaunchedEffect(l, sensitivity) { l.sensitivity = sensitivity }

        DisposableEffect(latticeViewModel, l) {
            latticeViewModel.applySensorState()
            onDispose { latticeViewModel.stopSensor() }
        }

        AnimatedLatticeDisplay(
            l = l,
            modifier = Modifier.fillMaxSize(),
            maxLines = lineDensity.maxLines,
            strokeWidth = 1f,
            timeProvider = { now: Long ->
                onCalculateTime(now)
                timeInSeconds
            },
            timeScale = 1.0,
            spectrum = spectrum,
            dimensionCycle = latticeColorMode == LatticeColorMode.DIMENSION_CYCLE
        )
    }
}
