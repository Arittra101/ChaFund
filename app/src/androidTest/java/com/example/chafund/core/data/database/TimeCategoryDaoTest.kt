package com.example.chafund.core.data.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.example.chafund.core.data.database.seed.SeedCallback
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TimeCategoryDaoTest {

    private lateinit var db: ChaFundDb

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ChaFundDb::class.java,
        ).addCallback(SeedCallback()).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() { db.close() }

    @Test
    fun seedCallbackInsertsDefaultCategories() = runTest {
        db.timeCategoryDao().observeAll().test {
            val cats = awaitItem()
            assertEquals(4, cats.size)
            assertEquals(listOf("Morning", "Noon", "Afternoon", "Evening"), cats.map { it.name })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun renameUpdatesName() = runTest {
        val dao = db.timeCategoryDao()
        dao.observeAll().test {
            val cats = awaitItem()
            val id = cats.first().id
            dao.rename(id, "Breakfast")
            val updated = awaitItem()
            assertTrue(updated.any { it.name == "Breakfast" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteByIdRemovesCategory() = runTest {
        val dao = db.timeCategoryDao()
        dao.observeAll().test {
            val cats  = awaitItem()
            val count = cats.size
            dao.deleteById(cats.last().id)
            val updated = awaitItem()
            assertEquals(count - 1, updated.size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
