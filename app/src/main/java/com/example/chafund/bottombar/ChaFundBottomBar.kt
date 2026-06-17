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
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.chafund.navigation.Navigator
import com.example.chafund.navigation.TopLevelDestination
import com.example.chafund.ui.theme.AppColors

@Composable
fun ChaFundBottomBar(
    navController: NavController,
    navigator: Navigator,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()

    NavigationBar {
        TopLevelDestination.entries.forEach { dest ->
            val selected = backStackEntry?.destination?.hasRoute(dest.route::class) == true
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

