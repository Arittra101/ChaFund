package com.example.chafund.navigation

sealed interface NavEvent {
    data class NavigateTo(val route: Route) : NavEvent
    data class NavigateTopLevel(val dest: TopLevelDestination) : NavEvent
    data object NavigateBack : NavEvent
}
