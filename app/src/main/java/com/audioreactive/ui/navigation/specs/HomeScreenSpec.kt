package com.audioreactive.ui.navigation.specs

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.audioreactive.MainActivity
import com.audioreactive.ui.screens.HomeScreen
import com.audioreactive.ui.viewmodel.AudioPlayerViewModel
import com.audioreactive.ui.viewmodel.AudioReactiveViewModelFactory
import com.audioreactive.ui.viewmodel.LatticeViewModel
import com.audioreactive.ui.viewmodel.VisualizerViewModel
import com.audioreactive.ui.viewmodel.collectInLaunchedEffect
import com.audioreactive.ui.viewmodel.effect.AudioPlayerEffect
import com.audioreactive.ui.viewmodel.intent.AudioPlayerIntent
import com.audioreactive.ui.viewmodel.intent.LatticeIntent

private const val LOG_TAG = "AR.HomeScreenSpec"

object HomeScreenSpec : IScreenSpec {
    override val route: String = IScreenSpec.HOME
    override val arguments = emptyList<androidx.navigation.NamedNavArgument>()

    @OptIn(UnstableApi::class)
    @Composable
    override fun Content(
        modifier: Modifier,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry
    ) {
        val context = LocalContext.current
        val activity = context as ComponentActivity
        val mainActivity = activity as MainActivity

        val audioPlayerViewModel = ViewModelProvider(
            store = activity.viewModelStore,
            factory = AudioReactiveViewModelFactory(),
            defaultCreationExtras = AudioReactiveViewModelFactory.creationExtras(
                activity.defaultViewModelCreationExtras,
                context
            )
        )[AudioPlayerViewModel::class.java]

        val visualizerViewModel = ViewModelProvider(
            store = activity.viewModelStore,
            factory = AudioReactiveViewModelFactory(),
            defaultCreationExtras = AudioReactiveViewModelFactory.creationExtras(
                activity.defaultViewModelCreationExtras,
                context
            )
        )[VisualizerViewModel::class.java]

        val latticeViewModel = ViewModelProvider(
            store = activity.viewModelStore,
            factory = AudioReactiveViewModelFactory(),
            defaultCreationExtras = AudioReactiveViewModelFactory.creationExtras(
                activity.defaultViewModelCreationExtras,
                context
            )
        )[LatticeViewModel::class.java]

        val (audioPlayerState, audioPlayerDispatcher, audioPlayerEffects) =
            audioPlayerViewModel.use(navBackStackEntry)
        val visualizerState by visualizerViewModel.stateFlow.collectAsState()
        val latticeState by latticeViewModel.stateFlow.collectAsState()

        var albumCover by remember { mutableStateOf<ImageBitmap?>(null) }

        audioPlayerEffects.collectInLaunchedEffect {
            when (it) {
                is AudioPlayerEffect.ImageChanged -> {
                    albumCover = it.imageBitmap
                }
                null -> {}
            }
        }

//        Log.d(LOG_TAG, "Background image: ${visualizerState.customImage}")

        HomeScreen(
            audioPlayerState = audioPlayerState,
            visualizerState = visualizerState,
            latticeState = latticeState,
            latticeViewModel = latticeViewModel,
            albumCover = albumCover,
            onAudioPrevious = {
                audioPlayerDispatcher.invoke(AudioPlayerIntent.Previous)
            },
            onAudioTogglePlayback = {
                audioPlayerDispatcher.invoke(AudioPlayerIntent.TogglePlayback)
            },
            onAudioNext = {
                audioPlayerDispatcher.invoke(AudioPlayerIntent.Next)
            },
            onLatticeTimeCalculate = { now ->
                latticeViewModel.dispatcher.invoke(LatticeIntent.CalculateTime(now))
            },
            onCaptureClick = {
                if (!visualizerState.running) {
                    mainActivity.launchAudioCaptureRequest()
                } else {
                    mainActivity.stopAudioCapture()
                }
            },
            onPickAudioFile = { mainActivity.launchAudioFilePicker() },
            onOpenSettings = {
                navController.navigate(IScreenSpec.SETTINGS) {
                    launchSingleTop = true
                }
            },
            captureRunning = visualizerState.running,
            onAddSongsToQueue = { mainActivity.launchQueueFilePicker() },
            onSelectQueueIndex = { index ->
                audioPlayerViewModel.dispatcher.invoke(
                    AudioPlayerIntent.SelectQueueIndex(index)
                )
            }
        )
    }
}
