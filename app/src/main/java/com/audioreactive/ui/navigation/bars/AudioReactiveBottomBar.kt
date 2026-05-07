package com.audioreactive.ui.navigation.bars

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.audioreactive.ui.viewmodel.state.AudioPlayerState

@Composable
fun AudioReactiveBottomBar(
    state: AudioPlayerState,
    albumCover: ImageBitmap?,
    onPrevious: () -> Unit,
    onTogglePlayback: () -> Unit,
    onNext: () -> Unit
) {
    val hasAudioLoaded = state.hasAudioLoaded

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

            // Song title
            if (state.isPlaying && state.songTitle.isNotBlank()) {
                Text(
                    text = state.songTitle,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }

            // Song progress bar
            if (state.isPlaying && state.durationMs > 0L) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    color = Color.White,
                    trackColor = Color.DarkGray
                )
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {

                // Album art stays pinned left
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

                // Music controls centered
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
                            tint = if (hasAudioLoaded)
                                Color.White
                            else
                                Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    IconButton(
                        enabled = hasAudioLoaded,
                        onClick = onTogglePlayback
                    ) {
                        Icon(
                            imageVector =
                                if (state.isPlaying)
                                    Icons.Default.Pause
                                else
                                    Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = if (hasAudioLoaded)
                                Color.White
                            else
                                Color.Gray,
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
                            tint =
                                if (hasAudioLoaded && state.hasNext)
                                    Color.White
                                else
                                    Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}
