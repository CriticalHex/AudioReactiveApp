package com.audioreactive.ui.navigation.bars

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.audioreactive.ui.viewmodel.state.AudioPlayerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.width

@Composable
fun AudioReactiveBottomBar(
    state: AudioPlayerState,
    albumCover: ImageBitmap?,
    onPrevious: () -> Unit,
    onTogglePlayback: () -> Unit,
    onNext: () -> Unit,
    onOpenQueuePicker: () -> Unit,
    onSelectQueueIndex: (Int) -> Unit,
    progressBarHeight: Dp = 4.dp,
    progressBarWidth: Dp = 220.dp,
    onQueueDialogVisibilityChange: (Boolean) -> Unit = {}
) {
    val hasAudioLoaded = state.hasAudioLoaded
    var showQueueDialog by rememberSaveable { mutableStateOf(false) }

    val progress = if (state.durationMs > 0L) {
        (state.currentPositionMs.toFloat() / state.durationMs.toFloat())
            .coerceIn(0f, 1f)
    } else {
        0f
    }

    Surface(
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 50.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (state.isPlaying && state.songTitle.isNotBlank()) {
                Text(
                    text = state.songTitle,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }

            if (state.isPlaying && state.durationMs > 0L) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .width(progressBarWidth)
                        .height(progressBarHeight),
                    color = Color.White,
                    trackColor = Color.DarkGray
                )
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (albumCover != null) {
                    Image(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 24.dp)
                            .size(64.dp),
                        bitmap = albumCover,
                        contentDescription = "Album Art"
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        enabled = hasAudioLoaded,
                        onClick = onPrevious
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous Song",
                            tint = if (hasAudioLoaded) Color.White else Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    IconButton(
                        enabled = hasAudioLoaded,
                        onClick = onTogglePlayback
                    ) {
                        Icon(
                            imageVector = if (state.isPlaying)
                                Icons.Default.Pause
                            else
                                Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = if (hasAudioLoaded) Color.White else Color.Gray,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    IconButton(
                        enabled = hasAudioLoaded && state.hasNext,
                        onClick = onNext
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next Song",
                            tint = if (hasAudioLoaded && state.hasNext) Color.White else Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                IconButton(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 24.dp),
                    enabled = state.hasAudioLoaded,
                    onClick = {
                        showQueueDialog = true
                        onQueueDialogVisibilityChange(true)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "Open Queue",
                        tint = if (state.hasAudioLoaded) Color.White else Color.Gray,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }

    if (showQueueDialog) {
        AlertDialog(
            onDismissRequest = {
                showQueueDialog = false
                onQueueDialogVisibilityChange(false)
            },
            containerColor = Color(0xFF111111),
            title = {
                Text(
                    text = "Queue",
                    color = Color.White
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .height(320.dp)
                        .width(280.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (state.queueTitles.isEmpty()) {
                        Text(
                            text = "No songs queued.",
                            color = Color.White
                        )
                    } else {
                        state.queueTitles.forEachIndexed { index, title ->
                            TextButton(
                                onClick = {
                                    showQueueDialog = false
                                    onQueueDialogVisibilityChange(false)
                                    onSelectQueueIndex(index)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (index == state.currentQueueIndex) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Currently Playing",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Text(
                                        text = title,
                                        color = if (index == state.currentQueueIndex) {
                                            Color.White
                                        } else {
                                            Color.LightGray
                                        },
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showQueueDialog = false
                        onQueueDialogVisibilityChange(false)
                        onOpenQueuePicker()
                    }
                ) {
                    Text(
                        text = "Add Songs",
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showQueueDialog = false
                        onQueueDialogVisibilityChange(false)
                    }
                ) {
                    Text(
                        text = "Close",
                        color = Color.Gray
                    )
                }
            }
        )
    }
}
