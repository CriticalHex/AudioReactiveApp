package com.audioreactive.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.audioreactive.ui.navigation.specs.IScreenSpec
import com.audioreactive.ui.viewmodel.LatticeViewModel
import com.audioreactive.ui.viewmodel.VisualizerViewModel

@Composable
fun AudioReactiveNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    latticeViewModel: LatticeViewModel,
    visualizerViewModel: VisualizerViewModel
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = IScreenSpec.ROOT,
    ) {
        navigation(
            route = IScreenSpec.ROOT,
            startDestination = IScreenSpec.startDestination
        ) {
            IScreenSpec.allScreens.forEach { (_, screenFactory) ->
                val screen = screenFactory(latticeViewModel, visualizerViewModel)

                composable(
                    route = screen.route,
                    arguments = screen.arguments
                ) { navBackStackEntry ->
                    screen.Content(
                        modifier = Modifier,
                        navController = navController,
                        navBackStackEntry = navBackStackEntry
                    )
                }
            }
        }
    }
}
