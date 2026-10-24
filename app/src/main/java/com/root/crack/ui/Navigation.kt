package com.root.crack.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.root.crack.ui.screen.*

enum class RcDestination(val route: String, val label: String) {
    Home("home", "Home"),
    Install("install", "Install"),
    Modules("modules", "Modules"),
    Kpm("kpm", "KPM"),
    Superuser("superuser", "SuperUser"),
    Settings("settings", "Settings"),
}

@Composable
fun RootCrackNavHost() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStack by navController.currentBackStackEntryAsState()
                val current = backStack?.destination

                fun icon(d: RcDestination) = when (d) {
                    RcDestination.Home -> Icons.Default.Home
                    RcDestination.Install -> Icons.Default.GetApp
                    RcDestination.Modules -> Icons.Default.Extension
                    RcDestination.Kpm -> Icons.Default.Memory
                    RcDestination.Superuser -> Icons.Default.Security
                    RcDestination.Settings -> Icons.Default.Settings
                }

                RcDestination.entries.forEach { dest ->
                    NavigationBarItem(
                        selected = current?.hierarchy?.any { it.route == dest.route } == true,
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(icon(dest), contentDescription = dest.label) },
                        label = { Text(dest.label) },
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = RcDestination.Home.route,
            modifier = androidx.compose.ui.Modifier.padding(padding),
        ) {
            composable(RcDestination.Home.route) {
                HomeScreen(onGoInstall = { navController.navigate(RcDestination.Install.route) })
            }
            composable(RcDestination.Install.route) { InstallScreen() }
            composable(RcDestination.Modules.route) { ModulesScreen() }
            composable(RcDestination.Kpm.route) { KpmScreen() }
            composable(RcDestination.Superuser.route) { SuperuserScreen() }
            composable(RcDestination.Settings.route) { SettingsScreen() }
        }
    }
}
