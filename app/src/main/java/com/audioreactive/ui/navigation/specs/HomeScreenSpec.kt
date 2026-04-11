package com.audioreactive.ui.navigation.specs

import androidx.activity.ComponentActivity
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.audioreactive.MainActivity
import com.audioreactive.ui.screens.HomeScreen
import com.audioreactive.ui.viewmodel.AudioPlayerViewModel
import com.audioreactive.ui.viewmodel.LatticeViewModel
import com.audioreactive.ui.viewmodel.VisualizerViewModel

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
        val activity = navController.context as ComponentActivity
        val mainActivity = activity as MainActivity

        val audioPlayerViewModel: AudioPlayerViewModel = viewModel(activity)
        val visualizerViewModel: VisualizerViewModel = viewModel(activity)
        val latticeViewModel: LatticeViewModel = viewModel(activity)

        HomeScreen(
            audioPlayerViewModel = audioPlayerViewModel,
            visualizerViewModel = visualizerViewModel,
            latticeViewModel = latticeViewModel,
            onStartCapture = { mainActivity.launchAudioCaptureRequest() },
            onPickAudioFile = { mainActivity.launchAudioFilePicker() },
            onOpenSettings = {
                navController.navigate(IScreenSpec.SETTINGS) {
                    launchSingleTop = true
                }
            }
        )
    }
}
