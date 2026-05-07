package com.audioreactive.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import com.audioreactive.ui.components.VisualizerLattice
import com.audioreactive.ui.components.VisualizerScreen
import com.audioreactive.ui.navigation.bars.AudioReactiveBottomBar
import com.audioreactive.ui.navigation.bars.AudioReactiveTopBar
import com.audioreactive.ui.viewmodel.LatticeViewModel
import com.audioreactive.ui.viewmodel.state.AudioPlayerState
import com.audioreactive.ui.viewmodel.state.LatticeState
import com.audioreactive.ui.viewmodel.state.VisualizerState
import kotlinx.coroutines.delay

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    audioPlayerState: AudioPlayerState,
    visualizerState: VisualizerState,
    latticeState: LatticeState,
    latticeViewModel: LatticeViewModel,
    onAudioPrevious: () -> Unit,
    onAudioTogglePlayback: () -> Unit,
    onAudioNext: () -> Unit,
    onLatticeTimeCalculate: (Long) -> Unit,
    onCaptureClick: () -> Unit,
    onPickAudioFile: () -> Unit,
    onOpenSettings: () -> Unit,
    captureRunning: Boolean,
    albumCover: ImageBitmap?,
    onAddSongsToQueue: () -> Unit,
    onSelectQueueIndex: (Int) -> Unit
) {
    var controlsVisible by remember { mutableStateOf(true) }
    var touchCount by remember { mutableIntStateOf(0) }
    var showCaptureDialog by remember { mutableStateOf(false) }
    var queueDialogOpen by remember { mutableStateOf(false) }

    LaunchedEffect(touchCount, controlsVisible) {
        if (controlsVisible) {
            delay(4500)
            controlsVisible = false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black,
        topBar = {
            if (controlsVisible) {
                AudioReactiveTopBar(
                    captureRunning = captureRunning,
                    onCaptureClick = {
                        if (!captureRunning) {
                            showCaptureDialog = true
                        } else {
                            onCaptureClick()
                        }
                    },
                    onPickAudioFile = onPickAudioFile,
                    onOpenSettings = onOpenSettings
                )
            }
        },
        bottomBar = {
            if (controlsVisible || queueDialogOpen) {
                AudioReactiveBottomBar(
                    state = audioPlayerState,
                    albumCover = albumCover,
                    onPrevious = onAudioPrevious,
                    onTogglePlayback = onAudioTogglePlayback,
                    onNext = onAudioNext,
                    onOpenQueuePicker = onAddSongsToQueue,
                    onSelectQueueIndex = onSelectQueueIndex,
                    onQueueDialogVisibilityChange = { queueDialogOpen = it }
                )
            }
        }
    ) { padding ->
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
            if (!visualizerState.disableBars) {
                VisualizerScreen(
                    spectrum = visualizerState.spectrum,
                    barColorMode = visualizerState.barColorMode,
                    solidBarColorArgb = visualizerState.solidBarColorArgb,
                    barRiseSpeed = visualizerState.barRiseSpeed,
                    barFallSpeed = visualizerState.barFallSpeed,
                    barSensitivity = visualizerState.barSensitivity,
                    barSoundMode = visualizerState.barSoundMode,
                    barMaxHeight = visualizerState.barMaxHeight,
                    barCount = visualizerState.barCount,
                    barOpacity = visualizerState.barOpacity
                )
            }

            if (!latticeState.disableLattice) {
                VisualizerLattice(
                    modifier = Modifier.fillMaxSize(),
                    spectrum = visualizerState.spectrum,
                    timeInSeconds = latticeState.timeInSeconds,
                    latticeColorMode = latticeState.latticeColorMode,
                    solidColorArgb = latticeState.solidColorArgb,
                    speed = latticeState.speed,
                    sensitivity = latticeState.sensitivity,
                    lineDensity = latticeState.lineDensity,
                    latticeViewModel = latticeViewModel,
                    onCalculateTime = onLatticeTimeCalculate
                )
            }
        }
    }
    if (showCaptureDialog) {
        AlertDialog(
            onDismissRequest = {
                showCaptureDialog = false
            },
            containerColor = Color(0xFF111111),
            title = {
                Text(
                    text = "Start Screen Capture",
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "Aurora Audio will begin capturing audio from your device. Continue?",
                    color = Color.White
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCaptureDialog = false
                        onCaptureClick()
                    }
                ) {
                    Text(
                        text = "Start",
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCaptureDialog = false
                    }
                ) {
                    Text(
                        text = "Cancel",
                        color = Color.Gray
                    )
                }
            }
        )
    }
}
