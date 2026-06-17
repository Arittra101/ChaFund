package com.example.chafund.core.data.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.example.chafund.core.data.database.entity.EntryEntity
import com.example.chafund.core.data.database.entity.MonthEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EntryDaoTest {

    private lateinit var db: ChaFundDb

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ChaFundDb::class.java,
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() { db.close() }

    private suspend fun insertMonth(id: Long = 1L): Long {
        return db.monthDao().upsertByYearMonth(
            MonthEntity(year = 2026, month = 6, label = "June 2026", isCurrent = true, createdAt = 0)
        )
    }

    private fun entry(monthId: Long, paisa: Long) = EntryEntity(
        monthId = monthId, amountPaisa = paisa, ref = null,
        date = 0L, time = "10:00", createdAt = 0, updatedAt = 0,
    )

    @Test
    fun sumByMonthReturnsZeroWhenEmpty() = runTest {
        val monthId = insertMonth()
        db.entryDao().sumByMonth(monthId).test {
            assertEquals(0L, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun sumByMonthAggregatesCorrectly() = runTest {
        val monthId = insertMonth()
        val dao = db.entryDao()
        dao.insert(entry(monthId, 10000))
        dao.insert(entry(monthId, 20000))
        dao.insert(entry(monthId, 5000))
        dao.sumByMonth(monthId).test {
            awaitItem() // might get intermediate
            val total = expectMostRecentItem()
            assertEquals(35000L, total)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteByIdRemovesEntry() = runTest {
        val monthId = insertMonth()
        val dao = db.entryDao()
        val id = dao.insert(entry(monthId, 10000))
        dao.deleteById(id)
        dao.observeByMonth(monthId).test {
            assertEquals(0, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun cascadeDeleteRemovesEntriesWithMonth() = runTest {
        val monthId = insertMonth()
        db.entryDao().insert(entry(monthId, 10000))
        db.monthDao().deletePastById(monthId) // monthId is current so won't delete
        // Make it a past month then delete
        val newMonthId = db.monthDao().upsertByYearMonth(
            MonthEntity(year = 2026, month = 7, label = "July 2026", isCurrent = true, createdAt = 0)
        )
        db.monthDao().promoteToCurrent(newMonthId)
        db.monthDao().deletePastById(monthId)
        db.entryDao().observeByMonth(monthId).test {
            assertEquals(0, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
