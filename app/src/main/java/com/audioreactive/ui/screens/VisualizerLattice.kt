package com.audioreactive.ui.screens

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import com.audioreactive.ui.viewmodel.LatticeViewModel
import com.csci448.abhattarai.reactortest.points.AnimatedLatticeDisplay
import com.csci448.abhattarai.reactortest.points.lattice

@Composable
fun VisualizerLattice(
    modifier: Modifier = Modifier,
    vm: LatticeViewModel
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val wPx = with(density) { maxWidth.toPx() }
        val hPx = with(density) { maxHeight.toPx() }

        val l = remember(wPx.toInt(), hPx.toInt()) {
            lattice(
                x = (wPx / 2f).toInt(),
                y = (hPx / 2f).toInt(),
                width = wPx.toInt(),
                height = hPx.toInt(),
                frequencyBand = 0
            )
        }

        AnimatedLatticeDisplay(
            l = l,
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 1f,
            timeProvider = { now: Long -> vm.timeSeconds(now) },
            timeScale = 1.0
        )
    }
}