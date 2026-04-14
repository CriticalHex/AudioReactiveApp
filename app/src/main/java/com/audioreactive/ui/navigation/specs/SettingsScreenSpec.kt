package com.audioreactive.ui.navigation.specs

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.audioreactive.ui.screens.SettingsScreen
import com.audioreactive.ui.viewmodel.LatticeViewModel
import com.audioreactive.ui.viewmodel.VisualizerViewModel

class SettingsScreenSpec(
    private val latticeViewModel: LatticeViewModel,
    private val visualizerViewModel: VisualizerViewModel
) : IScreenSpec {

    override val route: String = IScreenSpec.SETTINGS
    override val arguments = emptyList<androidx.navigation.NamedNavArgument>()

    @Composable
    override fun Content(
        modifier: Modifier,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry
    ) {
        SettingsScreen(
            onBack = { navController.popBackStack() },
            latticeViewModel = latticeViewModel,
            visualizerViewModel = visualizerViewModel
        )
    }
}
