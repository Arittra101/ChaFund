package com.example.chafund.feature.fund.presentation.home

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import com.example.chafund.core.presentation.components.AmountField
import com.example.chafund.core.presentation.components.CalculatorBottomSheet
import com.example.chafund.core.presentation.components.CategoryChip
import com.example.chafund.core.presentation.components.LockIndicator
import com.example.chafund.core.presentation.components.LockedMonthBadge
import com.example.chafund.core.presentation.components.MetricCard
import com.example.chafund.core.presentation.components.NameOption
import com.example.chafund.core.presentation.components.NamePicker
import com.example.chafund.core.presentation.components.PrimaryButton
import com.example.chafund.core.presentation.components.SegmentedToggle
import com.example.chafund.feature.fund.domain.model.TimeCategory
import com.example.chafund.ui.theme.AppColors

@Composable
fun HomeScreenRoot(
    viewModel: HomeViewModel,
    onNavigateToDailyHistory: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(HomeUiEvent.OnSnackbarDismissed)
        }
    }

    HomeScreen(
        state = state,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
        onNavigateToDailyHistory = onNavigateToDailyHistory,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onNavigateToDailyHistory: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { LockedMonthBadge(monthLabel = state.monthLabel) },
                actions = {
                    IconButton(onClick = onNavigateToDailyHistory) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "View daily history",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    LockIndicator(modifier = Modifier.padding(end = 16.dp))
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            // Summary row — Balance + Spent
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MetricCard(
                    label = "Balance",
                    value = state.balance,
                    fillColor = AppColors.BalanceFillLight,
                    labelColor = AppColors.BalanceTextLight,
                    valueColor = AppColors.BalanceTextLight,
                    modifier = Modifier.weight(1f),
                )
                MetricCard(
                    label = "Spent",
                    value = state.spent,
                    fillColor = AppColors.SpentFill,
                    labelColor = AppColors.SpentText,
                    valueColor = AppColors.SpentText,
                    modifier = Modifier.weight(1f),
                )
            }

            // Add card
            AddCard(state = state, onEvent = onEvent)

            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddCard(
    state: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
) {
    var showCalculator by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(14.dp)

    if (showCalculator) {
        CalculatorBottomSheet(
            onDone = { result ->
                onEvent(HomeUiEvent.OnAmountChange(result))
                showCalculator = false
            },
            onDismiss = { showCalculator = false },
            initialValue = state.amountInput,
        )
    }

    if (showDatePicker) {
        EntryDatePicker(
            initialEpochDay = state.selectedDateEpochDay,
            onPick = {
                onEvent(HomeUiEvent.OnDateSelected(it))
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, AppColors.BorderLight, shape),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Segmented toggle
            SegmentedToggle(
                options = listOf("Add entry", "Add expense"),
                selectedIndex = if (state.addMode == AddMode.ENTRY) 0 else 1,
                onSelect = { idx ->
                    onEvent(HomeUiEvent.OnModeChange(if (idx == 0) AddMode.ENTRY else AddMode.EXPENSE))
                },
            )

            // Amount field with calculator icon
            AmountField(
                value = state.amountInput,
                onValueChange = { onEvent(HomeUiEvent.OnAmountChange(it)) },
                error = state.amountError,
                trailingIcon = {
                    IconButton(onClick = { showCalculator = true }) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = "Open calculator",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )

            // Mode-dependent field
            if (state.addMode == AddMode.ENTRY) {
                NamePicker(
                    query = state.nameQuery,
                    suggestions = state.nameSuggestions.map { NameOption(it.id, it.label) },
                    selectedLabel = state.selectedPersonLabel,
                    hasNames = state.people.isNotEmpty(),
                    onQueryChange = { onEvent(HomeUiEvent.OnNameQueryChange(it)) },
                    onSelect = { onEvent(HomeUiEvent.OnPersonSelect(it)) },
                    onClear = { onEvent(HomeUiEvent.OnPersonClear) },
                    error = state.nameError,
                )
            } else {
                CategorySection(
                    categories = state.categories,
                    selectedCategoryId = state.selectedCategoryId,
                    error = state.categoryError,
                    onSelect = { onEvent(HomeUiEvent.OnCategorySelect(it)) },
                )
            }

            // Date selector — defaults to today, can be pre-dated into next month
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.5.dp, AppColors.BorderLight, RoundedCornerShape(10.dp))
                        .clickable { showDatePicker = true },
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = state.dateLabel,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "Change",
                            fontSize = 11.sp,
                            color = AppColors.BalanceTextLight,
                        )
                    }
                }
                if (state.dateHint.isNotEmpty()) {
                    Text(
                        text = "🕐 ${state.dateHint}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Save button
            PrimaryButton(
                text = if (state.addMode == AddMode.ENTRY) "Save entry" else "Save expense",
                onClick = { onEvent(HomeUiEvent.OnSave) },
                enabled = state.saveEnabled && !state.isSaving,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategorySection(
    categories: List<TimeCategory>,
    selectedCategoryId: Long?,
    error: String?,
    onSelect: (Long) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Time category",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            categories.forEachIndexed { index, cat ->
                CategoryChip(
                    label = cat.name,
                    chipColor = AppColors.chipColorFor(index),
                    selected = cat.id == selectedCategoryId,
                    onClick = { onSelect(cat.id) },
                )
            }
        }
        if (error != null) {
            Text(text = error, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
        }
    }
}

/**
 * Date picker for a new entry/expense. Selectable range is today through the last day of next
 * month, so a record can be filed under the current month or pre-dated into the next one.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EntryDatePicker(
    initialEpochDay: Long,
    onPick: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val today = LocalDate.now()
    val nextMonth = today.plusMonths(1)
    val minEpochDay = today.toEpochDay()
    val maxEpochDay = nextMonth.withDayOfMonth(nextMonth.lengthOfMonth()).toEpochDay()
    val dayMillis = 86_400_000L

    val selectableDates = remember(minEpochDay, maxEpochDay) {
        object : androidx.compose.material3.SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val epochDay = Math.floorDiv(utcTimeMillis, dayMillis)
                return epochDay in minEpochDay..maxEpochDay
            }
            override fun isSelectableYear(year: Int): Boolean =
                year == today.year || year == nextMonth.year
        }
    }

    val initial = if (initialEpochDay in minEpochDay..maxEpochDay) initialEpochDay else minEpochDay
    val pickerState = androidx.compose.material3.rememberDatePickerState(
        initialSelectedDateMillis = initial * dayMillis,
        initialDisplayedMonthMillis = initial * dayMillis,
        selectableDates = selectableDates,
    )

    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = { pickerState.selectedDateMillis?.let { onPick(Math.floorDiv(it, dayMillis)) } },
                enabled = pickerState.selectedDateMillis != null,
            ) { Text("Set") }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    ) {
        androidx.compose.material3.DatePicker(
            state = pickerState,
            title = {
                Text(
                    "Pick a date",
                    fontSize = 13.sp,
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp),
                )
            },
        )
    }
}
