package com.audioreactive.ui.screens

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import com.audioreactive.ui.components.VisualizerLattice
import com.audioreactive.ui.components.VisualizerScreen
import com.audioreactive.ui.navigation.bars.AudioReactiveBottomBar
import com.audioreactive.ui.navigation.bars.AudioReactiveTopBar
import com.audioreactive.ui.viewmodel.AudioPlayerViewModel
import com.audioreactive.ui.viewmodel.LatticeViewModel
import com.audioreactive.ui.viewmodel.VisualizerViewModel
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    audioPlayerViewModel: AudioPlayerViewModel,
    visualizerViewModel: VisualizerViewModel,
    latticeViewModel: LatticeViewModel,
    onStartCapture: () -> Unit,
    onPickAudioFile: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val visualizerState by visualizerViewModel.stateFlow.collectAsState()

    var controlsVisible by remember { mutableStateOf(false) }
    var touchCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(touchCount, controlsVisible) {
        if (controlsVisible) {
            delay(3000)
            controlsVisible = false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black,
        topBar = {
            if (controlsVisible) {
                AudioReactiveTopBar(
                    onStartCapture = onStartCapture,
                    onPickAudioFile = onPickAudioFile,
                    onOpenSettings = onOpenSettings
                )
            }
        },
        bottomBar = {
            if (controlsVisible) {
                AudioReactiveBottomBar(
                    audioPlayerViewModel = audioPlayerViewModel
                )
            }
        }
    ) { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            controlsVisible = true
                            touchCount++
                        }
                    )
                }
        ) {
            VisualizerScreen(visualizerState.spectrum)

            VisualizerLattice(
                modifier = Modifier.fillMaxSize(),
                latticeViewModel = latticeViewModel,
                volume = visualizerState.volume
            )
        }
    }
}
