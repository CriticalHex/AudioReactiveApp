package com.audioreactive.ui.navigation.specs

import androidx.activity.ComponentActivity
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.audioreactive.MainActivity
import com.audioreactive.ui.screens.SettingsScreen
import com.audioreactive.ui.viewmodel.AudioReactiveViewModelFactory
import com.audioreactive.ui.viewmodel.LatticeViewModel
import com.audioreactive.ui.viewmodel.VisualizerViewModel
import com.audioreactive.ui.viewmodel.intent.LatticeIntent
import com.audioreactive.ui.viewmodel.intent.VisualizerIntent

data object SettingsScreenSpec : IScreenSpec {

    override val route: String = IScreenSpec.SETTINGS
    override val arguments = emptyList<NamedNavArgument>()

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
                    LatticeIntent.SetLatticeColorMode(it)
                )
            },
            onSetLatticeSolidColor = {
                latticeViewModel.dispatcher.invoke(
                    LatticeIntent.SetSolidColor(it)
                )
            },
            onSetGyrosDisabled = {
                latticeViewModel.dispatcher.invoke(
                    LatticeIntent.SetGyrosDisabled(it)
                )
            },
            onSetLatticeDisabled = {
                latticeViewModel.dispatcher.invoke(
                    LatticeIntent.SetLatticeDisabled(it)
                )
            },
            onSetBarColorMode = {
                visualizerViewModel.dispatcher.invoke(
                    VisualizerIntent.SetBarColorMode(it)
                )
            },
            onSetSolidBarColor = {
                visualizerViewModel.dispatcher.invoke(
                    VisualizerIntent.SetSolidBarColor(it)
                )
            },
            onSetBarsDisabled = {
                visualizerViewModel.dispatcher.invoke(
                    VisualizerIntent.SetBarsDisabled(it)
                )
            },
            onSetLatticeSpeed = {
                latticeViewModel.dispatcher.invoke(
                    LatticeIntent.SetSpeed(it)
                )
            },
            onSetLatticeSensitivity = {
                latticeViewModel.dispatcher.invoke(
                    LatticeIntent.SetSensitivity(it)
                )
            },
            onSetLatticeLineDensity = {
                latticeViewModel.dispatcher.invoke(
                    LatticeIntent.SetLineDensity(it)
                )
            },
            onSetBarRiseSpeed = {
                visualizerViewModel.dispatcher.invoke(
                    VisualizerIntent.SetBarRiseSpeed(it)
                )
            },
            onSetBarFallSpeed = {
                visualizerViewModel.dispatcher.invoke(
                    VisualizerIntent.SetBarFallSpeed(it)
                )
            },
            onSetBarSensitivity = {
                visualizerViewModel.dispatcher.invoke(
                    VisualizerIntent.SetBarSensitivity(it)
                )
            },
            onSetBarSoundMode = {
                visualizerViewModel.dispatcher.invoke(
                    VisualizerIntent.SetBarSoundMode(it)
                )
            },
            onSetBarMaxHeight = {
                visualizerViewModel.dispatcher.invoke(
                    VisualizerIntent.SetBarMaxHeight(it)
                )
            },
            onSetBarCount = {
                visualizerViewModel.dispatcher.invoke(
                    VisualizerIntent.SetBarCount(it)
                )
            },
            onSetBarOpacity = {
                visualizerViewModel.dispatcher.invoke(
                    VisualizerIntent.SetBarOpacity(it)
                )
            },
            onSetInvertGyroSpin = {
                latticeViewModel.dispatcher.invoke(
                    LatticeIntent.SetInvertGyroSpin(it)
                )
            },
            onSetInvertGyroHorizontal = {
                latticeViewModel.dispatcher.invoke(
                    LatticeIntent.SetInvertGyroHorizontal(it)
                )
            },
            onSetInvertGyroVertical = {
                latticeViewModel.dispatcher.invoke(
                    LatticeIntent.SetInvertGyroVertical(it)
                )
            },
            onResetToDefaults = {
                latticeViewModel.dispatcher.invoke(
                    LatticeIntent.ResetSettings
                )
                visualizerViewModel.dispatcher.invoke(
                    VisualizerIntent.ResetSettings
                )
            },
            onSetBackgroundImage = {
                mainActivity.photoPickerLauncher.launch(
                    PickVisualMediaRequest.Builder().setMediaType(
                        ActivityResultContracts.PickVisualMedia.SingleMimeType(MimeTypes.IMAGE_JPEG)
                    ).build()
                )
            },
            onRemoveBackgroundImage = {
                visualizerViewModel.dispatcher.invoke(VisualizerIntent.SetBackgroundImage(false))
            }
        )
    }
}
