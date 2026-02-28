package com.dagplanner.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dagplanner.app.ui.screens.calendar.AgendaScreen
import com.dagplanner.app.ui.screens.calendar.CalendarScreen
import com.dagplanner.app.ui.screens.settings.SettingsScreen
import com.dagplanner.app.ui.screens.shopping.ShoppingScreen
import com.dagplanner.app.ui.screens.tasks.TasksScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Calendar : Screen("calendar", "Agenda", Icons.Default.CalendarMonth)
    data object AgendaView : Screen("agenda_view", "Dagoverzicht", Icons.Default.CalendarToday)
    data object Tasks : Screen("tasks", "Taken", Icons.Default.CheckBox)
    data object Shopping : Screen("shopping", "Boodschappen", Icons.Default.ShoppingCart)
    data object Settings : Screen("settings", "Instellingen", Icons.Default.Settings)
}

val bottomNavItems = listOf(
    Screen.Calendar,
    Screen.AgendaView,
    Screen.Tasks,
    Screen.Shopping,
    Screen.Settings,
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Calendar.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Calendar.route) { CalendarScreen(navController) }
            composable(Screen.AgendaView.route) { AgendaScreen(navController) }
            composable(Screen.Tasks.route) { TasksScreen(navController) }
            composable(Screen.Shopping.route) { ShoppingScreen(navController) }
            composable(Screen.Settings.route) { SettingsScreen(navController) }
        }
    }
}
