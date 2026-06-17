package com.example.chafund.navigation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

interface Navigator {
    val navEvents: SharedFlow<NavEvent>
    fun navigateTo(route: Route)
    fun navigateToTopLevel(dest: TopLevelDestination)
    fun navigateBack()
}

class NavigatorImpl : Navigator {
    private val _navEvents = MutableSharedFlow<NavEvent>(extraBufferCapacity = 1)
    override val navEvents: SharedFlow<NavEvent> = _navEvents.asSharedFlow()

    override fun navigateTo(route: Route) {
        _navEvents.tryEmit(NavEvent.NavigateTo(route))
    }

    override fun navigateToTopLevel(dest: TopLevelDestination) {
        _navEvents.tryEmit(NavEvent.NavigateTopLevel(dest))
    }

    override fun navigateBack() {
        _navEvents.tryEmit(NavEvent.NavigateBack)
    }
}
