package com.audioreactive.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.audioreactive.ui.viewmodel.AudioPlayerViewModel
import com.audioreactive.ui.viewmodel.LatticeViewModel
import com.audioreactive.ui.viewmodel.VisualizerViewModel

@Composable
fun HomeScreen(
    audioPlayerViewModel: AudioPlayerViewModel,
    visualizerViewModel: VisualizerViewModel,
    latticeViewModel: LatticeViewModel
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black,
        floatingActionButton = {
            val isPlaying by audioPlayerViewModel.isPlaying
            FloatingActionButton(
                onClick = { audioPlayerViewModel.togglePlayback() }
            ) {
                Icon(
                    imageVector = if (isPlaying)
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
            VisualizerScreen(visualizerViewModel.state.value.spectrum)
            VisualizerLattice(
                modifier = Modifier.fillMaxSize(),
                vm = latticeViewModel,
                volume = visualizerViewModel.state.value.volume
            )
        }
    }
}
