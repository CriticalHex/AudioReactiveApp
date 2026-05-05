package com.audioreactive.ui.navigation.specs

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController

interface IScreenSpec {
    val route: String
    val arguments: List<NamedNavArgument>

    @Composable
    fun Content(
        modifier: Modifier,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry
    )

    companion object {
        const val ROOT = "root"
        const val HOME = "home"
        const val SETTINGS = "settings"
        val startDestination = HOME

        val allScreens: Map<String, IScreenSpec> = mapOf(
            HOME to HomeScreenSpec,
            SETTINGS to SettingsScreenSpec
        )
    }
}
