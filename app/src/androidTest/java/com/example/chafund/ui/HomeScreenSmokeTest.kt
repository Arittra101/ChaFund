package com.example.chafund.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.chafund.core.utils.Money
import com.example.chafund.feature.fund.presentation.home.AddMode
import com.example.chafund.feature.fund.presentation.home.HomeScreen
import com.example.chafund.feature.fund.presentation.home.HomeUiEvent
import com.example.chafund.feature.fund.presentation.home.HomeUiState
import com.example.chafund.ui.theme.ChaFundTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenSmokeTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val events = mutableListOf<HomeUiEvent>()

    private fun setScreen(state: HomeUiState) {
        composeRule.setContent {
            ChaFundTheme {
                HomeScreen(
                    state             = state,
                    onEvent           = { events.add(it) },
                    snackbarHostState = SnackbarHostState(),
                )
            }
        }
    }

    @Test
    fun balanceAndSpentCardsAreDisplayed() {
        setScreen(HomeUiState(monthLabel = "June 2026", balance = Money(100000), spent = Money(50000)))
        composeRule.onNodeWithText("Balance").assertIsDisplayed()
        composeRule.onNodeWithText("Spent").assertIsDisplayed()
        composeRule.onNodeWithText("Tk 1,000.00").assertIsDisplayed()
        composeRule.onNodeWithText("Tk 500.00").assertIsDisplayed()
    }

    @Test
    fun lockedMonthBadgeIsDisplayed() {
        setScreen(HomeUiState(monthLabel = "June 2026"))
        composeRule.onNodeWithText("June 2026").assertIsDisplayed()
        composeRule.onNodeWithText("Current month").assertIsDisplayed()
    }

    @Test
    fun saveButtonDisabledWithEmptyAmount() {
        setScreen(HomeUiState(saveEnabled = false))
        composeRule.onNodeWithText("Save entry").assertIsNotEnabled()
    }

    @Test
    fun saveButtonEnabledWithValidAmount() {
        setScreen(HomeUiState(saveEnabled = true))
        composeRule.onNodeWithText("Save entry").assertIsEnabled()
    }

    @Test
    fun segmentedToggleSwitchesToExpenseMode() {
        setScreen(HomeUiState(addMode = AddMode.ENTRY))
        composeRule.onNodeWithText("Add expense").performClick()
        assert(events.any { it is HomeUiEvent.OnModeChange && it.mode == AddMode.EXPENSE })
    }

    @Test
    fun amountFieldInputFiresEvent() {
        setScreen(HomeUiState())
        composeRule.onNodeWithText("0").performTextInput("150")
        assert(events.any { it is HomeUiEvent.OnAmountChange })
    }

    @Test
    fun saveLabelChangesWithMode() {
        setScreen(HomeUiState(addMode = AddMode.EXPENSE))
        composeRule.onNodeWithText("Save expense").assertIsDisplayed()
    }
}
