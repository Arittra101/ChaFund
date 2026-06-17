package com.example.chafund.feature.fund.presentation

import app.cash.turbine.test
import com.example.chafund.core.domain.DataError
import com.example.chafund.core.domain.Result
import com.example.chafund.core.utils.Money
import com.example.chafund.feature.fund.domain.model.Month
import com.example.chafund.feature.fund.domain.model.MonthSummary
import com.example.chafund.feature.fund.domain.model.TimeCategory
import com.example.chafund.feature.fund.presentation.home.AddMode
import com.example.chafund.feature.fund.presentation.home.HomeUiEvent
import com.example.chafund.feature.fund.presentation.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeFundRepository
    private lateinit var vm: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        repo = FakeFundRepository()
        vm   = HomeViewModel(repo)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun initialStateHasZeroBalanceAndSpent() = runTest {
        vm.uiState.test {
            val state = awaitItem()
            assertEquals(Money.Zero, state.balance)
            assertEquals(Money.Zero, state.spent)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun summaryUpdatesWhenRepoEmits() = runTest {
        vm.uiState.test {
            awaitItem() // initial
            repo.setSummary(MonthSummary(1L, Money(50000), Money(20000), Money(30000)))
            dispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem()
            assertEquals(Money(30000), state.balance)
            assertEquals(Money(20000), state.spent)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun monthLabelUpdatesFromRepo() = runTest {
        vm.uiState.test {
            awaitItem()
            repo.setMonth(Month(1L, 2026, 6, "June 2026", true))
            dispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem()
            assertEquals("June 2026", state.monthLabel)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun switchToExpenseModeAutoSelectsFirstCategory() = runTest {
        val categories = listOf(TimeCategory(1L, "Morning", 1), TimeCategory(2L, "Noon", 2))
        repo.setCategories(categories)
        vm.uiState.test {
            awaitItem() // initial
            dispatcher.scheduler.advanceUntilIdle()
            awaitItem() // categories loaded
            vm.onEvent(HomeUiEvent.OnModeChange(AddMode.EXPENSE))
            val state = awaitItem()
            assertEquals(AddMode.EXPENSE, state.addMode)
            assertEquals(1L, state.selectedCategoryId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun saveDisabledWhenAmountEmpty() = runTest {
        vm.uiState.test {
            val state = awaitItem()
            assertTrue(!state.saveEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun saveEnabledAfterValidAmountEntry() = runTest {
        vm.uiState.test {
            awaitItem()
            vm.onEvent(HomeUiEvent.OnAmountChange("100"))
            val state = awaitItem()
            assertTrue(state.saveEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun saveEntryShowsSnackbarOnSuccess() = runTest {
        vm.onEvent(HomeUiEvent.OnAmountChange("500"))
        dispatcher.scheduler.advanceUntilIdle()
        vm.onEvent(HomeUiEvent.OnSave)
        dispatcher.scheduler.advanceUntilIdle()
        vm.uiState.test {
            val state = awaitItem()
            assertNotNull(state.snackbarMessage)
            assertTrue(state.snackbarMessage!!.contains("Entry saved"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun saveEntryShowsErrorOnFailure() = runTest {
        repo.addEntryResult = Result.Error(DataError.Local.UNKNOWN)
        vm.onEvent(HomeUiEvent.OnAmountChange("500"))
        dispatcher.scheduler.advanceUntilIdle()
        vm.onEvent(HomeUiEvent.OnSave)
        dispatcher.scheduler.advanceUntilIdle()
        vm.uiState.test {
            val state = awaitItem()
            assertNotNull(state.snackbarMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun invalidAmountSetsError() = runTest {
        vm.onEvent(HomeUiEvent.OnAmountChange("0"))
        vm.onEvent(HomeUiEvent.OnSave)
        dispatcher.scheduler.advanceUntilIdle()
        vm.uiState.test {
            val state = awaitItem()
            assertNotNull(state.amountError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun expenseSaveBlockedWithoutCategory() = runTest {
        vm.onEvent(HomeUiEvent.OnModeChange(AddMode.EXPENSE))
        vm.onEvent(HomeUiEvent.OnAmountChange("200"))
        vm.onEvent(HomeUiEvent.OnSave)
        dispatcher.scheduler.advanceUntilIdle()
        vm.uiState.test {
            val state = awaitItem()
            assertNotNull(state.categoryError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun formResetsAfterSuccessfulSave() = runTest {
        vm.onEvent(HomeUiEvent.OnAmountChange("300"))
        vm.onEvent(HomeUiEvent.OnRefChange("lunch"))
        vm.onEvent(HomeUiEvent.OnSave)
        dispatcher.scheduler.advanceUntilIdle()
        vm.uiState.test {
            val state = awaitItem()
            assertEquals("", state.amountInput)
            assertEquals("", state.refInput)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
