package com.example.chafund.core.data.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.example.chafund.core.data.database.entity.MonthEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MonthDaoTest {

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

    private fun monthDao() = db.monthDao()

    private fun month(year: Int, month: Int, isCurrent: Boolean = false) = MonthEntity(
        year = year, month = month, label = "$month/$year",
        isCurrent = isCurrent, createdAt = System.currentTimeMillis(),
    )

    @Test
    fun upsertIsIdempotent() = runTest {
        val dao = monthDao()
        val id1 = dao.upsertByYearMonth(month(2026, 6))
        val id2 = dao.upsertByYearMonth(month(2026, 6))
        assertEquals(id1, id2)
    }

    @Test
    fun promoteToCurrentLeavesExactlyOneCurrentRow() = runTest {
        val dao = monthDao()
        val id1 = dao.upsertByYearMonth(month(2026, 5))
        val id2 = dao.upsertByYearMonth(month(2026, 6))
        dao.promoteToCurrent(id1)
        dao.promoteToCurrent(id2)

        dao.observeAll().test {
            val rows = awaitItem()
            val currentRows = rows.filter { it.isCurrent }
            assertEquals(1, currentRows.size)
            assertEquals(id2, currentRows.first().id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deletePastByIdBlocksCurrentMonth() = runTest {
        val dao = monthDao()
        val id = dao.upsertByYearMonth(month(2026, 6))
        dao.promoteToCurrent(id)
        val rows = dao.deletePastById(id)
        assertEquals(0, rows)
        assertNotNull(dao.findByYearMonth(2026, 6))
    }

    @Test
    fun deletePastByIdRemovesPastMonth() = runTest {
        val dao = monthDao()
        val pastId    = dao.upsertByYearMonth(month(2026, 5))
        val currentId = dao.upsertByYearMonth(month(2026, 6))
        dao.promoteToCurrent(currentId)
        val rows = dao.deletePastById(pastId)
        assertEquals(1, rows)
        assertNull(dao.findByYearMonth(2026, 5))
    }

    @Test
    fun observePastExcludesCurrentMonth() = runTest {
        val dao = monthDao()
        val id = dao.upsertByYearMonth(month(2026, 6))
        dao.promoteToCurrent(id)
        dao.upsertByYearMonth(month(2026, 5))
        dao.observePast().test {
            val past = awaitItem()
            assertEquals(1, past.size)
            assertTrue(past.none { it.isCurrent })
            cancelAndIgnoreRemainingEvents()
        }
    }
}
