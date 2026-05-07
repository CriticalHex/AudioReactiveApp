package com.audioreactive.ui.navigation.specs

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.audioreactive.ui.screens.SettingsScreen
import com.audioreactive.ui.viewmodel.AudioReactiveViewModelFactory
import com.audioreactive.ui.viewmodel.LatticeViewModel
import com.audioreactive.ui.viewmodel.VisualizerViewModel

data object SettingsScreenSpec : IScreenSpec {

    override val route: String = IScreenSpec.SETTINGS
    override val arguments = emptyList<androidx.navigation.NamedNavArgument>()

    @Composable
    override fun Content(
        modifier: Modifier,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry
    ) {
        val context = LocalContext.current
        val activity = context as ComponentActivity

        val latticeViewModel = ViewModelProvider(
            store = activity.viewModelStore,
            factory = AudioReactiveViewModelFactory(),
            defaultCreationExtras = AudioReactiveViewModelFactory.creationExtras(
                activity.defaultViewModelCreationExtras,
                context
            )
        )[LatticeViewModel::class.java]

        val visualizerViewModel = ViewModelProvider(
            store = activity.viewModelStore,
            factory = AudioReactiveViewModelFactory(),
            defaultCreationExtras = AudioReactiveViewModelFactory.creationExtras(
                activity.defaultViewModelCreationExtras,
                context
            )
        )[VisualizerViewModel::class.java]

        val latticeState = latticeViewModel.stateFlow.collectAsState().value
        val visualizerState = visualizerViewModel.stateFlow.collectAsState().value

        SettingsScreen(
            onBack = { navController.popBackStack() },
            latticeState = latticeState,
            visualizerState = visualizerState,
            onSetLatticeColorMode = {
                latticeViewModel.dispatcher.invoke(
                    com.audioreactive.ui.viewmodel.intent.LatticeIntent.SetLatticeColorMode(it)
                )
            },
            onSetLatticeSolidColor = {
                latticeViewModel.dispatcher.invoke(
                    com.audioreactive.ui.viewmodel.intent.LatticeIntent.SetSolidColor(it)
                )
            },
            onSetGyrosDisabled = {
                latticeViewModel.dispatcher.invoke(
                    com.audioreactive.ui.viewmodel.intent.LatticeIntent.SetGyrosDisabled(it)
                )
            },
            onSetLatticeDisabled = {
                latticeViewModel.dispatcher.invoke(
                    com.audioreactive.ui.viewmodel.intent.LatticeIntent.SetLatticeDisabled(it)
                )
            },
            onSetBarColorMode = {
                visualizerViewModel.dispatcher.invoke(
                    com.audioreactive.ui.viewmodel.intent.VisualizerIntent.SetBarColorMode(it)
                )
            },
            onSetSolidBarColor = {
                visualizerViewModel.dispatcher.invoke(
                    com.audioreactive.ui.viewmodel.intent.VisualizerIntent.SetSolidBarColor(it)
                )
            },
            onSetBarsDisabled = {
                visualizerViewModel.dispatcher.invoke(
                    com.audioreactive.ui.viewmodel.intent.VisualizerIntent.SetBarsDisabled(it)
                )
            },
            onSetLatticeSpeed = {
                latticeViewModel.dispatcher.invoke(
                    com.audioreactive.ui.viewmodel.intent.LatticeIntent.SetSpeed(it)
                )
            },
            onSetLatticeSensitivity = {
                latticeViewModel.dispatcher.invoke(
                    com.audioreactive.ui.viewmodel.intent.LatticeIntent.SetSensitivity(it)
                )
            },
            onSetLatticeLineDensity = {
                latticeViewModel.dispatcher.invoke(
                    com.audioreactive.ui.viewmodel.intent.LatticeIntent.SetLineDensity(it)
                )
            },
            onSetBarRiseSpeed = {
                visualizerViewModel.dispatcher.invoke(
                    com.audioreactive.ui.viewmodel.intent.VisualizerIntent.SetBarRiseSpeed(it)
                )
            },
            onSetBarFallSpeed = {
                visualizerViewModel.dispatcher.invoke(
                    com.audioreactive.ui.viewmodel.intent.VisualizerIntent.SetBarFallSpeed(it)
                )
            },
            onSetBarSensitivity = {
                visualizerViewModel.dispatcher.invoke(
                    com.audioreactive.ui.viewmodel.intent.VisualizerIntent.SetBarSensitivity(it)
                )
            },
            onSetBarSoundMode = {
                visualizerViewModel.dispatcher.invoke(
                    com.audioreactive.ui.viewmodel.intent.VisualizerIntent.SetBarSoundMode(it)
                )
            },
            onSetBarMaxHeight = {
                visualizerViewModel.dispatcher.invoke(
                    com.audioreactive.ui.viewmodel.intent.VisualizerIntent.SetBarMaxHeight(it)
                )
            },
            onSetBarCount = {
                visualizerViewModel.dispatcher.invoke(
                    com.audioreactive.ui.viewmodel.intent.VisualizerIntent.SetBarCount(it)
                )
            },
            onSetBarOpacity = {
                visualizerViewModel.dispatcher.invoke(
                    com.audioreactive.ui.viewmodel.intent.VisualizerIntent.SetBarOpacity(it)
                )
            },
            onSetInvertGyroSpin = {
                latticeViewModel.dispatcher.invoke(
                    com.audioreactive.ui.viewmodel.intent.LatticeIntent.SetInvertGyroSpin(it)
                )
            },
            onSetInvertGyroHorizontal = {
                latticeViewModel.dispatcher.invoke(
                    com.audioreactive.ui.viewmodel.intent.LatticeIntent.SetInvertGyroHorizontal(it)
                )
            },
            onSetInvertGyroVertical = {
                latticeViewModel.dispatcher.invoke(
                    com.audioreactive.ui.viewmodel.intent.LatticeIntent.SetInvertGyroVertical(it)
                )
            },
            onResetToDefaults = {
                latticeViewModel.dispatcher.invoke(
                    com.audioreactive.ui.viewmodel.intent.LatticeIntent.ResetSettings
                )
                visualizerViewModel.dispatcher.invoke(
                    com.audioreactive.ui.viewmodel.intent.VisualizerIntent.ResetSettings
                )
            }
        )
    }
}
