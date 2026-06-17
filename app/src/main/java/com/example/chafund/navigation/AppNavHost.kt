package com.example.chafund.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.chafund.bottombar.ChaFundBottomBar
import com.example.chafund.feature.fund.presentation.home.HomeScreenRoot
import com.example.chafund.feature.fund.presentation.home.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavHost(navigator: Navigator) {
    val navController = rememberNavController()

    // Collect nav events and apply to navController
    LaunchedEffect(navigator) {
        navigator.navEvents.collect { event ->
            when (event) {
                is NavEvent.NavigateTo -> navController.navigate(event.route)
                is NavEvent.NavigateTopLevel -> {
                    val dest = event.dest
                    val route: Route = when (dest) {
                        TopLevelDestination.HOME    -> Route.Home
                        TopLevelDestination.DAILY   -> {
                            // Navigate to current month; monthId resolved on the screen
                            Route.DailyHistory(monthId = 0L)
                        }
                        TopLevelDestination.MONTHS  -> Route.MonthlyHistory
                        TopLevelDestination.SETTINGS -> Route.Settings
                    }
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState    = true
                    }
                }
                NavEvent.NavigateBack -> navController.popBackStack()
            }
        }
    }

    Scaffold(
        bottomBar = { ChaFundBottomBar(navController, navigator) },
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = Route.Home,
        ) {
            composable<Route.Home> {
                val vm: HomeViewModel = koinViewModel()
                HomeScreenRoot(viewModel = vm)
            }
            composable<Route.DailyHistory> {
                // Implemented in CHF-37
                PlaceholderScreen("Daily History")
            }
            composable<Route.DayDetail> {
                // Implemented in CHF-38
                PlaceholderScreen("Day Detail")
            }
            composable<Route.MonthlyHistory> {
                // Implemented in CHF-41
                PlaceholderScreen("Monthly History")
            }
            composable<Route.Settings> {
                // Implemented in CHF-44
                PlaceholderScreen("Settings")
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(title)
    }
}
