package com.example.chafund.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class TopLevelDestination(
    val label: String,
    val icon: ImageVector,
    val route: Route,
) {
    HOME(
        label = "Home",
        icon  = Icons.Default.Home,
        route = Route.Home,
    ),
    MONTHS(
        label = "Months",
        icon  = Icons.Default.BarChart,
        route = Route.MonthlyHistory,
    ),
    SETTINGS(
        label = "Settings",
        icon  = Icons.Default.Settings,
        route = Route.Settings,
    ),
}
