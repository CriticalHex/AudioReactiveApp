package com.audioreactive.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.audioreactive.ui.components.VisualizerLattice
import com.audioreactive.ui.components.VisualizerScreen
import com.audioreactive.ui.viewmodel.AudioPlayerViewModel
import com.audioreactive.ui.viewmodel.LatticeViewModel
import com.audioreactive.ui.viewmodel.VisualizerViewModel
import com.audioreactive.ui.viewmodel.intent.AudioPlayerIntent

@Composable
fun HomeScreen(
    audioPlayerViewModel: AudioPlayerViewModel,
    visualizerViewModel: VisualizerViewModel,
    latticeViewModel: LatticeViewModel
) {
    val visualizerState = visualizerViewModel.stateFlow.collectAsState()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black,
        floatingActionButton = {
            val audioPlayerState = audioPlayerViewModel.stateFlow.collectAsState()
            FloatingActionButton(
                onClick = { audioPlayerViewModel.dispatcher.invoke(AudioPlayerIntent.TogglePlayback) }
            ) {
                Icon(
                    imageVector = if (audioPlayerState.value.isPlaying)
                        Icons.Default.Pause
                    else
                        Icons.Default.PlayArrow,
                    contentDescription = "PlayPause"
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            VisualizerScreen(visualizerState.value.spectrum)
            VisualizerLattice(
                modifier = Modifier.fillMaxSize(),
                latticeViewModel = latticeViewModel,
                volume = visualizerState.value.volume
            )
        }
    }
}
