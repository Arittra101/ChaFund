package com.example.chafund.feature.settings.domain

import com.example.chafund.core.domain.DataError
import com.example.chafund.core.domain.Result
import com.example.chafund.core.utils.Money
import com.example.chafund.feature.history.domain.model.HistoryMonth
import com.example.chafund.feature.fund.domain.model.Group
import com.example.chafund.feature.fund.domain.model.Month
import com.example.chafund.feature.fund.domain.model.Person
import com.example.chafund.feature.fund.domain.model.TimeCategory
import com.example.chafund.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow

/**
 * State for the "bring last month's balance into the current month" action.
 * [canCarry] is true only when there is a previous month, its balance is positive, and the
 * current month hasn't already carried it in.
 */
data class CarryInfo(
    val hasPreviousMonth: Boolean = false,
    val previousMonthName: String = "",
    val previousBalance: Money = Money.Zero,
    val alreadyCarried: Boolean = false,
    val canCarry: Boolean = false,
)

interface SettingsRepository {
    fun observeCurrentMonthLabel(): Flow<String>
    fun observeCurrentMonth(): Flow<Month?>
    fun observePastMonths(): Flow<List<HistoryMonth>>
    fun observeTimeCategories(): Flow<List<TimeCategory>>
    fun observeGroups(): Flow<List<Group>>
    fun observePeople(): Flow<List<Person>>
    fun themeMode(): Flow<ThemeMode>
    fun observeCarryInfo(): Flow<CarryInfo>
    suspend fun carryLastMonthBalance(): Result<Unit, DataError.Local>
    suspend fun deletePastMonth(id: Long): Result<Unit, DataError.Local>
    suspend fun addCategory(name: String): Result<Unit, DataError.Local>
    suspend fun renameCategory(id: Long, name: String): Result<Unit, DataError.Local>
    suspend fun deleteCategory(id: Long): Result<Int, DataError.Local>
    suspend fun addGroup(name: String): Result<Unit, DataError.Local>
    suspend fun renameGroup(id: Long, name: String): Result<Unit, DataError.Local>
    suspend fun deleteGroup(id: Long): Result<Unit, DataError.Local>
    suspend fun addPerson(name: String, groupId: Long): Result<Unit, DataError.Local>
    suspend fun updatePerson(id: Long, name: String, groupId: Long): Result<Unit, DataError.Local>
    suspend fun deletePerson(id: Long): Result<Unit, DataError.Local>
    suspend fun setTheme(mode: ThemeMode)
}
