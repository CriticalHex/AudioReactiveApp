package com.audioreactive.ui.screens

import android.util.DisplayMetrics
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.csci448.abhattarai.reactortest.points.AnimatedLatticeDisplay
import com.csci448.abhattarai.reactortest.points.lattice

@Composable
fun VisualizerLattice(modifier: Modifier) {

    modifier
        .fillMaxSize()
    val context = LocalContext.current
    val displayMetrics: DisplayMetrics = context.resources.displayMetrics
    val screenWidthPx = displayMetrics.widthPixels
    val screenHeightPx = displayMetrics.heightPixels


    val l = remember {

        lattice(
            x = screenWidthPx / 2 ,
            y = screenHeightPx / 2,
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