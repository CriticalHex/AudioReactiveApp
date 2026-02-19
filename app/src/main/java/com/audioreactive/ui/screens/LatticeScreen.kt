package com.audioreactive.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.csci448.abhattarai.reactortest.points.AnimatedLatticeDisplay
import com.csci448.abhattarai.reactortest.points.lattice

@Composable
fun VisualizerLattice() {

    val l = remember {
        lattice(
            x = 630, // idk why I have to offset it so wierd
            y = 1080,
            width = 0,
            height = 1080,
            frequencyBand = 0
        )
    }
    AnimatedLatticeDisplay(
        l = l,
        modifier = Modifier.fillMaxSize(),
        strokeWidth = 1f,
        timeScale = 0.1
    )
}