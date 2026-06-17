package com.example.chafund.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.chafund.bottombar.ChaFundBottomBar
import com.example.chafund.feature.fund.presentation.home.HomeScreenRoot
import com.example.chafund.feature.fund.presentation.home.HomeViewModel
import com.example.chafund.feature.history.presentation.daily.DailyHistoryScreenRoot
import com.example.chafund.feature.history.presentation.daily.DailyHistoryViewModel
import com.example.chafund.feature.history.presentation.daydetail.DayDetailScreenRoot
import com.example.chafund.feature.history.presentation.daydetail.DayDetailViewModel
import com.example.chafund.feature.history.presentation.monthly.MonthlyHistoryScreenRoot
import com.example.chafund.feature.history.presentation.monthly.MonthlyHistoryViewModel
import com.example.chafund.feature.settings.presentation.settings.SettingsScreenRoot
import com.example.chafund.feature.settings.presentation.settings.SettingsViewModel
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavHost(navigator: Navigator) {
    val navController = rememberNavController()

    LaunchedEffect(navigator) {
        navigator.navEvents.collect { event ->
            when (event) {
                is NavEvent.NavigateTo -> navController.navigate(event.route)
                is NavEvent.NavigateTopLevel -> {
                    val route: Route = when (event.dest) {
                        TopLevelDestination.HOME     -> Route.Home
                        TopLevelDestination.MONTHS   -> Route.MonthlyHistory
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

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val topLevelRoutes = TopLevelDestination.entries.map { it.route::class.qualifiedName }
    val shouldShowBottomBar = topLevelRoutes.contains(
        currentRoute?.substringBefore("?")
    )


    Scaffold(
        bottomBar = { if (shouldShowBottomBar) ChaFundBottomBar(navController, navigator) },
    ) { _ ->
        NavHost(navController = navController, startDestination = Route.Home) {
            composable<Route.Home> {
                HomeScreenRoot(
                    viewModel                = koinViewModel<HomeViewModel>(),
                    onNavigateToDailyHistory = { navController.navigate(Route.DailyHistory(0L)) },
                )
            }
            composable<Route.DailyHistory> { backStack ->
                val route = backStack.toRoute<Route.DailyHistory>()
                DailyHistoryScreenRoot(
                    viewModel  = koinViewModel<DailyHistoryViewModel>(),
                    monthId    = route.monthId,
                    onDayClick = { monthId, date -> navController.navigate(Route.DayDetail(monthId, date)) },
                )
            }
            composable<Route.DayDetail> {
                DayDetailScreenRoot(
                    viewModel = koinViewModel<DayDetailViewModel>(),
                    onBack    = { navController.popBackStack() },
                )
            }
            composable<Route.MonthlyHistory> {
                MonthlyHistoryScreenRoot(
                    viewModel    = koinViewModel<MonthlyHistoryViewModel>(),
                    onMonthClick = { monthId -> navController.navigate(Route.DailyHistory(monthId)) },
                )
            }
            composable<Route.Settings> {
                SettingsScreenRoot(viewModel = koinViewModel<SettingsViewModel>())
            }
        }
    }
}
