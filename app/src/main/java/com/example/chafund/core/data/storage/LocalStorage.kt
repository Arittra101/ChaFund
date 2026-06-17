package com.example.chafund.core.data.storage

import com.example.chafund.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow

interface LocalStorage {
    val themeMode: Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
}
