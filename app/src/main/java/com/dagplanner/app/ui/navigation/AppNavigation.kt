package com.dagplanner.app.ui.navigation

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dagplanner.app.ui.screens.calendar.AgendaScreen
import com.dagplanner.app.ui.screens.calendar.CalendarScreen
import com.dagplanner.app.ui.screens.email.EmailScreen
import com.dagplanner.app.ui.screens.email.EmailViewModel
import com.dagplanner.app.ui.screens.settings.SettingsScreen
import com.dagplanner.app.ui.screens.shopping.ShoppingScreen
import com.dagplanner.app.ui.screens.tasks.TasksScreen
import com.dagplanner.app.ui.screens.today.TodayScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Today : Screen("today", "Vandaag", Icons.Default.CalendarToday)
    data object Calendar : Screen("calendar", "Agenda", Icons.Default.CalendarMonth)
    data object Tasks : Screen("tasks", "Taken", Icons.Default.CheckBox)
    data object Shopping : Screen("shopping", "Boodschappen", Icons.Default.ShoppingCart)
    data object Email : Screen("email", "E-mail", Icons.Default.Email)
    data object Settings : Screen("settings", "Instellingen", Icons.Default.Settings)
}

val bottomNavItems = listOf(
    Screen.Today,
    Screen.Calendar,
    Screen.Tasks,
    Screen.Shopping,
    Screen.Email,
    Screen.Settings,
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Offline indicator
    val context = LocalContext.current
    var isOnline by remember { mutableStateOf(true) }
    DisposableEffect(Unit) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { isOnline = true }
            override fun onLost(network: Network) { isOnline = false }
        }
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        cm.registerNetworkCallback(request, callback)
        val caps = cm.getNetworkCapabilities(cm.activeNetwork)
        isOnline = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        onDispose { cm.unregisterNetworkCallback(callback) }
    }

    // Unread email badge count (Activity-scoped ViewModel)
    val emailViewModel: EmailViewModel = hiltViewModel()
    val emailState by emailViewModel.uiState.collectAsState()
    val unreadCount = emailState.emails.count { !it.isRead }

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = {
                            if (screen == Screen.Email && unreadCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge {
                                            Text(if (unreadCount > 99) "99+" else unreadCount.toString())
                                        }
                                    }
                                ) {
                                    Icon(screen.icon, contentDescription = screen.label)
                                }
                            } else {
                                Icon(screen.icon, contentDescription = screen.label)
                            }
                        },
                        label = null,
                        selected = selected,
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
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = Screen.Today.route,
            ) {
                composable(Screen.Today.route) { TodayScreen(navController) }
                composable(Screen.Calendar.route) { CalendarScreen(navController) }
                composable("agenda_view") { AgendaScreen(navController) }
                composable(Screen.Tasks.route) { TasksScreen(navController) }
                composable(Screen.Shopping.route) { ShoppingScreen(navController) }
                composable(Screen.Email.route) { EmailScreen(navController) }
                composable(Screen.Settings.route) { SettingsScreen(navController) }
            }

            AnimatedVisibility(
                visible = !isOnline,
                enter = slideInVertically(initialOffsetY = { -it }),
                exit = slideOutVertically(targetOffsetY = { -it }),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.error)
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Geen internetverbinding",
                        color = MaterialTheme.colorScheme.onError,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}
