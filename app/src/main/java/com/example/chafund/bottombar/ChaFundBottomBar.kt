package com.example.chafund.bottombar

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.chafund.navigation.Navigator
import com.example.chafund.navigation.Route
import com.example.chafund.navigation.TopLevelDestination
import com.example.chafund.ui.theme.AppColors

@Composable
fun ChaFundBottomBar(
    navController: NavController,
    navigator: Navigator,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar {
        TopLevelDestination.entries.forEach { dest ->
            val selected = isDestinationSelected(currentRoute, dest)
            NavigationBarItem(
                selected = selected,
                onClick  = { navigator.navigateToTopLevel(dest) },
                icon = {
                    Icon(
                        imageVector = dest.icon,
                        contentDescription = dest.label,
                    )
                },
                label = { Text(text = dest.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = AppColors.BalanceTextLight,
                    selectedTextColor   = AppColors.BalanceTextLight,
                    indicatorColor      = AppColors.BalanceFillLight,
                    unselectedIconColor = Color.Unspecified,
                    unselectedTextColor = Color.Unspecified,
                ),
            )
        }
    }
}

private fun isDestinationSelected(currentRoute: String?, dest: TopLevelDestination): Boolean {
    return when (dest) {
        TopLevelDestination.HOME     -> currentRoute?.contains("Home") == true
        TopLevelDestination.DAILY    -> currentRoute?.contains("DailyHistory") == true
        TopLevelDestination.MONTHS   -> currentRoute?.contains("MonthlyHistory") == true
        TopLevelDestination.SETTINGS -> currentRoute?.contains("Settings") == true
    }
}
