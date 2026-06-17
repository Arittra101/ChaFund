package com.example.chafund.feature.settings.domain

import com.example.chafund.core.domain.DataError
import com.example.chafund.core.domain.Result
import com.example.chafund.feature.history.domain.model.HistoryMonth
import com.example.chafund.feature.fund.domain.model.TimeCategory
import com.example.chafund.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeCurrentMonthLabel(): Flow<String>
    fun observePastMonths(): Flow<List<HistoryMonth>>
    fun observeTimeCategories(): Flow<List<TimeCategory>>
    fun themeMode(): Flow<ThemeMode>
    suspend fun deletePastMonth(id: Long): Result<Unit, DataError.Local>
    suspend fun addCategory(name: String): Result<Unit, DataError.Local>
    suspend fun renameCategory(id: Long, name: String): Result<Unit, DataError.Local>
    suspend fun deleteCategory(id: Long): Result<Int, DataError.Local>
    suspend fun setTheme(mode: ThemeMode)
}
