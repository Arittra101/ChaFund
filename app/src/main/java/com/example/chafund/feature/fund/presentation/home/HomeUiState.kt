package com.example.chafund.feature.fund.presentation.home

import com.example.chafund.core.utils.Money
import com.example.chafund.feature.fund.domain.model.Person
import com.example.chafund.feature.fund.domain.model.TimeCategory

enum class AddMode { ENTRY, EXPENSE }

data class HomeUiState(
    val monthLabel: String = "",
    val balance: Money = Money.Zero,
    val spent: Money = Money.Zero,

    // Add form
    val addMode: AddMode = AddMode.ENTRY,
    val amountInput: String = "",
    val amountError: String? = null,

    // Entry — name picker
    val people: List<Person> = emptyList(),
    val nameQuery: String = "",
    val nameSuggestions: List<Person> = emptyList(),
    val selectedPersonId: Long? = null,
    val selectedPersonLabel: String = "",
    val nameError: String? = null,

    // Expense — time category
    val categories: List<TimeCategory> = emptyList(),
    val selectedCategoryId: Long? = null,
    val categoryError: String? = null,
    val todayHint: String = "",
    val isSaving: Boolean = false,
    val saveEnabled: Boolean = false,

    // Feedback
    val snackbarMessage: String? = null,
)
