package com.audioreactive.ui.navigation.specs

import androidx.activity.ComponentActivity
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import com.audioreactive.ui.viewmodel.intent.AudioPlayerIntent
import com.audioreactive.ui.viewmodel.intent.LatticeIntent

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

        val audioPlayerState by audioPlayerViewModel.stateFlow.collectAsState()
        val visualizerState by visualizerViewModel.stateFlow.collectAsState()
        val latticeState by latticeViewModel.stateFlow.collectAsState()

        HomeScreen(
            audioPlayerState = audioPlayerState,
            visualizerState = visualizerState,
            latticeState = latticeState,
            onAudioPrevious = {
                audioPlayerViewModel.dispatcher.invoke(AudioPlayerIntent.Previous)
            },
            onAudioTogglePlayback = {
                audioPlayerViewModel.dispatcher.invoke(AudioPlayerIntent.TogglePlayback)
            },
            onAudioNext = {
                audioPlayerViewModel.dispatcher.invoke(AudioPlayerIntent.Next)
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
            captureRunning = visualizerState.running
        )
    }
}
