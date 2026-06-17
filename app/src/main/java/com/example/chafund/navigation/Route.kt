package com.example.chafund.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object Home : Route
    @Serializable
    data class DailyHistory(val monthId: Long) : Route
    @Serializable
    data class DayDetail(val monthId: Long, val dateEpoch: Long) : Route
    @Serializable
    data object MonthlyHistory : Route
    @Serializable
    data object Settings : Route
}
