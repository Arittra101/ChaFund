package com.example.chafund

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chafund.core.data.storage.LocalStorage
import com.example.chafund.ui.theme.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class AppViewModel(localStorage: LocalStorage) : ViewModel() {
    val themeMode = localStorage.themeMode.stateIn(
        scope         = viewModelScope,
        started       = SharingStarted.Eagerly,
        initialValue  = ThemeMode.SYSTEM,
    )
}
