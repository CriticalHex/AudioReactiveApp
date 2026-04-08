package com.audioreactive.ui.navigation.specs

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.audioreactive.ui.screens.HomeScreen
import com.audioreactive.ui.viewmodel.AudioPlayerViewModel
import com.audioreactive.ui.viewmodel.LatticeViewModel
import com.audioreactive.ui.viewmodel.VisualizerViewModel

object HomeScreenSpec : IScreenSpec {
    override val route: String = IScreenSpec.HOME
    override val arguments = emptyList<androidx.navigation.NamedNavArgument>()

    @Composable
    override fun Content(
        modifier: Modifier,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry
    ) {
        val activity = navController.context as ComponentActivity

        val audioPlayerViewModel: AudioPlayerViewModel = viewModel(activity)
        val visualizerViewModel: VisualizerViewModel = viewModel(activity)
        val latticeViewModel: LatticeViewModel = viewModel(activity)

        HomeScreen(
            audioPlayerViewModel = audioPlayerViewModel,
            visualizerViewModel = visualizerViewModel,
            latticeViewModel = latticeViewModel
        )
    }
}
