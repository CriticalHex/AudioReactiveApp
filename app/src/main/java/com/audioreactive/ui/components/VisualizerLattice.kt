package com.audioreactive.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import com.audioreactive.sensor.RotationSensorManager
import com.audioreactive.ui.reactor.AnimatedLatticeDisplay
import com.audioreactive.ui.reactor.Lattice
import com.audioreactive.ui.viewmodel.LatticeViewModel
import com.audioreactive.ui.viewmodel.intent.LatticeIntent

@Composable
fun VisualizerLattice(
    modifier: Modifier = Modifier,
    latticeViewModel: LatticeViewModel,
    spectrum: FloatArray
) {
    val state = latticeViewModel.stateFlow.collectAsState()
    val context = LocalContext.current
    val rotationSensorManager = remember { RotationSensorManager(context) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val wPx = with(density) { maxWidth.toPx() }
        val hPx = with(density) { maxHeight.toPx() }

        val l = remember(wPx.toInt(), hPx.toInt()) {
            Lattice(
                x = (wPx / 2f).toInt(),
                y = (hPx / 2f).toInt(),
                width = wPx.toInt(),
                height = hPx.toInt()
            )
        }

        DisposableEffect(l) {
            rotationSensorManager.start { matrix -> l.setRotation(matrix) }
            onDispose { rotationSensorManager.stop() }
        }

        AnimatedLatticeDisplay(
            l = l,
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 1f,
            timeProvider = { now: Long ->
                latticeViewModel.dispatcher.invoke(LatticeIntent.CalculateTime(now))
                state.value.timeInSeconds
            },
            timeScale = 1.0,
            spectrum = spectrum
        )
    }
}