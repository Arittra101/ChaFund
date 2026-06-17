package com.example.chafund.feature.fund.presentation.home

sealed interface HomeUiEvent {
    data class OnModeChange(val mode: AddMode) : HomeUiEvent
    data class OnAmountChange(val value: String) : HomeUiEvent
    data class OnRefChange(val value: String) : HomeUiEvent
    data class OnCategorySelect(val id: Long) : HomeUiEvent
    data object OnSave : HomeUiEvent
    data object OnSnackbarDismissed : HomeUiEvent
}
