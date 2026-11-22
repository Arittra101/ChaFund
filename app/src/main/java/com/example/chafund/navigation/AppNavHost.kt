package com.example.chafund.navigation

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.chafund.bottombar.ChaFundBottomBar
import com.example.chafund.core.presentation.components.ConfirmationBottomSheet
import com.example.chafund.feature.fund.presentation.home.HomeScreenRoot
import com.example.chafund.feature.fund.presentation.home.HomeViewModel
import com.example.chafund.feature.history.presentation.daily.DailyHistoryScreenRoot
import com.example.chafund.feature.history.presentation.daily.DailyHistoryViewModel
import com.example.chafund.feature.history.presentation.daydetail.DayDetailScreenRoot
import com.example.chafund.feature.history.presentation.daydetail.DayDetailViewModel
import com.example.chafund.feature.history.presentation.entries.MonthEntriesScreenRoot
import com.example.chafund.feature.history.presentation.entries.MonthEntriesViewModel
import com.example.chafund.feature.history.presentation.monthly.MonthlyHistoryScreenRoot
import com.example.chafund.feature.history.presentation.monthly.MonthlyHistoryViewModel
import com.example.chafund.feature.settings.presentation.settings.NamesGroupsScreenRoot
import com.example.chafund.feature.settings.presentation.settings.SettingsScreenRoot
import com.example.chafund.feature.settings.presentation.settings.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@SuppressLint("RestrictedApi")
@OptIn(ExperimentalMaterial3Api::class)
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
    val shouldShowBottomBar = TopLevelDestination.entries.any { dest ->
        navBackStackEntry?.destination?.hasRoute(dest.route::class) == true
    }


    // Pressing back on the root destination asks before leaving the app.
    val activity = LocalActivity.current
    var showExitSheet by remember { mutableStateOf(false) }
    val isAtRootDestination = navController.previousBackStackEntry == null

    BackHandler(enabled = isAtRootDestination && !showExitSheet) { showExitSheet = true }

    if (showExitSheet) {
        ConfirmationBottomSheet(
            title = "Exit From ChaFund?",
            body = "You're about to close the app. Your saved entries stay safe.",
            confirmLabel = "Exit",
            onConfirm = {
                showExitSheet = false
                activity?.finish()
            },
            onDismiss = { showExitSheet = false },
        )
    }

    Scaffold(
        bottomBar = { if (shouldShowBottomBar) ChaFundBottomBar(navController, navigator) },
    ) { innerPadding ->
        // Inset the whole graph by the Scaffold's padding so screen content ends above the
        // bottom bar instead of scrolling under it, and consume those insets so the per-screen
        // Scaffolds don't apply the system bars a second time.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            NavHost(navController = navController, startDestination = Route.Home) {
                composable<Route.Home> {
                    HomeScreenRoot(
                        viewModel = koinViewModel<HomeViewModel>(),
                        onNavigateToDailyHistory = { navController.navigate(Route.DailyHistory(null)) },
                    )
                }
                composable<Route.DailyHistory> { backStack ->
                    val route = backStack.toRoute<Route.DailyHistory>()
                    DailyHistoryScreenRoot(
                        viewModel = koinViewModel<DailyHistoryViewModel>(),
                        monthId = route.monthId,
                        onDayClick = { monthId, date -> navController.navigate(Route.DayDetail(monthId, date)) },
                        onBack = { navController.popBackStack() },
                        onOpenEntries = { monthId -> navController.navigate(Route.MonthEntries(monthId)) },
                    )
                }
                composable<Route.MonthEntries> {
                    MonthEntriesScreenRoot(
                        viewModel = koinViewModel<MonthEntriesViewModel>(),
                        onBack = { navController.popBackStack() },
                    )
                }
                composable<Route.DayDetail> {
                    DayDetailScreenRoot(
                        viewModel = koinViewModel<DayDetailViewModel>(),
                        onBack = { navController.popBackStack() },
                    )
                }
                composable<Route.MonthlyHistory> {
                    MonthlyHistoryScreenRoot(
                        viewModel = koinViewModel<MonthlyHistoryViewModel>(),
                        onMonthClick = { monthId -> navController.navigate(Route.DailyHistory(monthId)) },
                    )
                }
                composable<Route.Settings> {
                    SettingsScreenRoot(
                        viewModel = koinViewModel<SettingsViewModel>(),
                        onOpenNamesGroups = { navController.navigate(Route.NamesGroups) },
                    )
                }
                composable<Route.NamesGroups> {
                    NamesGroupsScreenRoot(
                        viewModel = koinViewModel<SettingsViewModel>(),
                        onBack = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}
