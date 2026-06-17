package com.example.chafund.feature.settings.data.repository

import com.example.chafund.core.data.database.dao.ExpenseDao
import com.example.chafund.core.data.database.dao.MonthDao
import com.example.chafund.core.data.database.dao.TimeCategoryDao
import com.example.chafund.core.data.database.entity.TimeCategoryEntity
import com.example.chafund.core.data.storage.LocalStorage
import com.example.chafund.core.domain.DataError
import com.example.chafund.core.domain.DispatcherProvider
import com.example.chafund.core.domain.Result
import com.example.chafund.feature.fund.domain.model.TimeCategory
import com.example.chafund.feature.history.data.mapper.toHistoryDomain
import com.example.chafund.feature.history.domain.model.HistoryMonth
import com.example.chafund.feature.settings.domain.SettingsRepository
import com.example.chafund.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SettingsRepositoryImpl(
    private val monthDao: MonthDao,
    private val categoryDao: TimeCategoryDao,
    private val expenseDao: ExpenseDao,
    private val localStorage: LocalStorage,
    private val dispatchers: DispatcherProvider,
) : SettingsRepository {

    override fun observeCurrentMonthLabel(): Flow<String> =
        monthDao.observeCurrent().map { it?.label ?: "" }

    override fun observePastMonths(): Flow<List<HistoryMonth>> =
        monthDao.observeMonthSummaries().map { list ->
            list.filter { !it.isCurrent }.map { it.toHistoryDomain() }
        }

    override fun observeTimeCategories(): Flow<List<TimeCategory>> =
        categoryDao.observeAll().map { list ->
            list.map { TimeCategory(id = it.id, name = it.name, sortOrder = it.sortOrder) }
        }

    override fun themeMode(): Flow<ThemeMode> = localStorage.themeMode

    override suspend fun deletePastMonth(id: Long): Result<Unit, DataError.Local> =
        withContext(dispatchers.io) {
            val rows = monthDao.deletePastById(id)
            if (rows == 0) Result.Error(DataError.Local.NOT_FOUND)
            else Result.Success(Unit)
        }

    override suspend fun addCategory(name: String): Result<Unit, DataError.Local> =
        withContext(dispatchers.io) {
            runCatching {
                categoryDao.insert(
                    TimeCategoryEntity(
                        name      = name.trim(),
                        sortOrder = Int.MAX_VALUE,
                        createdAt = System.currentTimeMillis(),
                    )
                )
                Result.Success(Unit)
            }.getOrElse { Result.Error(DataError.Local.UNKNOWN) }
        }

    override suspend fun renameCategory(id: Long, name: String): Result<Unit, DataError.Local> =
        withContext(dispatchers.io) {
            runCatching {
                categoryDao.rename(id, name.trim())
                Result.Success(Unit)
            }.getOrElse { Result.Error(DataError.Local.UNKNOWN) }
        }

    override suspend fun deleteCategory(id: Long): Result<Int, DataError.Local> =
        withContext(dispatchers.io) {
            val count = expenseDao.countByCategory(id)
            if (count > 0) return@withContext Result.Error(DataError.Local.NOT_FOUND)
            runCatching {
                categoryDao.deleteById(id)
                Result.Success(0)
            }.getOrElse { Result.Error(DataError.Local.UNKNOWN) }
        }

    override suspend fun setTheme(mode: ThemeMode) = localStorage.setThemeMode(mode)
}
