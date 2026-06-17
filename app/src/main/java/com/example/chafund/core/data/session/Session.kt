package com.example.chafund.core.data.session

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class Session {

    private val _currentMonthId = MutableStateFlow(0L)
    val currentMonthId: StateFlow<Long> = _currentMonthId.asStateFlow()

    private val _monthChanged = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val monthChanged: SharedFlow<Unit> = _monthChanged.asSharedFlow()

    fun setCurrentMonth(id: Long): Boolean {
        if (_currentMonthId.value == id) return false
        _currentMonthId.value = id
        _monthChanged.tryEmit(Unit)
        return true
    }
}
