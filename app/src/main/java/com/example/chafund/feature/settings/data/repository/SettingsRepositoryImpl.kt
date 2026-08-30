package com.example.chafund.feature.settings.data.repository

import com.example.chafund.core.data.database.dao.ExpenseDao
import com.example.chafund.core.data.database.dao.MonthDao
import com.example.chafund.core.data.database.dao.PersonDao
import com.example.chafund.core.data.database.dao.PersonGroupDao
import com.example.chafund.core.data.database.dao.TimeCategoryDao
import com.example.chafund.core.data.database.entity.PersonEntity
import com.example.chafund.core.data.database.entity.PersonGroupEntity
import com.example.chafund.core.data.database.entity.TimeCategoryEntity
import com.example.chafund.core.data.storage.LocalStorage
import com.example.chafund.core.domain.DataError
import com.example.chafund.core.domain.DispatcherProvider
import com.example.chafund.core.domain.Result
import com.example.chafund.feature.fund.data.mapper.toDomain
import com.example.chafund.feature.fund.domain.model.Group
import com.example.chafund.feature.fund.domain.model.Month
import com.example.chafund.feature.fund.domain.model.Person
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
    private val groupDao: PersonGroupDao,
    private val personDao: PersonDao,
    private val localStorage: LocalStorage,
    private val dispatchers: DispatcherProvider,
) : SettingsRepository {

    override fun observeCurrentMonthLabel(): Flow<String> =
        monthDao.observeCurrent().map { it?.label ?: "" }

    override fun observeCurrentMonth(): Flow<Month?> =
        monthDao.observeCurrent().map { it?.toDomain() }

    override fun observePastMonths(): Flow<List<HistoryMonth>> =
        monthDao.observeMonthSummaries().map { list ->
            list.filter { !it.isCurrent }.map { it.toHistoryDomain() }
        }

    override fun observeTimeCategories(): Flow<List<TimeCategory>> =
        categoryDao.observeAll().map { list ->
            list.map { TimeCategory(id = it.id, name = it.name, sortOrder = it.sortOrder) }
        }

    override fun observeGroups(): Flow<List<Group>> =
        groupDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observePeople(): Flow<List<Person>> =
        personDao.observeAllWithGroup().map { list -> list.map { it.toDomain() } }

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
                        name = name.trim(),
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

    override suspend fun addGroup(name: String): Result<Unit, DataError.Local> =
        withContext(dispatchers.io) {
            runCatching {
                groupDao.insert(
                    PersonGroupEntity(name = name.trim(), createdAt = System.currentTimeMillis())
                )
                Result.Success(Unit)
            }.getOrElse { Result.Error(DataError.Local.UNKNOWN) }
        }

    override suspend fun renameGroup(id: Long, name: String): Result<Unit, DataError.Local> =
        withContext(dispatchers.io) {
            runCatching {
                groupDao.rename(id, name.trim())
                Result.Success(Unit)
            }.getOrElse { Result.Error(DataError.Local.UNKNOWN) }
        }

    override suspend fun deleteGroup(id: Long): Result<Unit, DataError.Local> =
        withContext(dispatchers.io) {
            if (groupDao.countPersonsInGroup(id) > 0) {
                return@withContext Result.Error(DataError.Local.NOT_FOUND)
            }
            runCatching {
                groupDao.deleteById(id)
                Result.Success(Unit)
            }.getOrElse { Result.Error(DataError.Local.UNKNOWN) }
        }

    override suspend fun addPerson(name: String, groupId: Long): Result<Unit, DataError.Local> =
        withContext(dispatchers.io) {
            runCatching {
                personDao.insert(
                    PersonEntity(
                        name = name.trim(),
                        groupId = groupId,
                        createdAt = System.currentTimeMillis(),
                    )
                )
                Result.Success(Unit)
            }.getOrElse { Result.Error(DataError.Local.UNKNOWN) }
        }

    override suspend fun updatePerson(
        id: Long,
        name: String,
        groupId: Long,
    ): Result<Unit, DataError.Local> =
        withContext(dispatchers.io) {
            runCatching {
                personDao.update(id, name.trim(), groupId)
                Result.Success(Unit)
            }.getOrElse { Result.Error(DataError.Local.UNKNOWN) }
        }

    override suspend fun deletePerson(id: Long): Result<Unit, DataError.Local> =
        withContext(dispatchers.io) {
            runCatching {
                personDao.clearPersonRefsForEntries(id)
                personDao.deleteById(id)
                Result.Success(Unit)
            }.getOrElse { Result.Error(DataError.Local.UNKNOWN) }
        }

    override suspend fun setCycleStart(monthId: Long, epochDay: Long): Result<Unit, DataError.Local> =
        withContext(dispatchers.io) {
            runCatching {
                monthDao.setCycleStart(monthId, epochDay)
                Result.Success(Unit)
            }.getOrElse { Result.Error(DataError.Local.UNKNOWN) }
        }

    override suspend fun clearCycleStart(monthId: Long): Result<Unit, DataError.Local> =
        withContext(dispatchers.io) {
            runCatching {
                monthDao.clearCycleStart(monthId)
                Result.Success(Unit)
            }.getOrElse { Result.Error(DataError.Local.UNKNOWN) }
        }

    override suspend fun setTheme(mode: ThemeMode) = localStorage.setThemeMode(mode)
}
