# CHF-38 · `DayDetailViewModel` + Screen

| Field | Value |
|---|---|
| Type | Story · P0 · 5 SP |
| Blocked By | CHF-37 |
| Blocks | CHF-39 |

## Goal
Day detail showing entries + expenses grouped by Time Category.

## Hints
- Package: `feature/history/presentation/daydetail/`.
- Route args: `monthId`, `dateEpoch`.
- `DayDetailUiState`: `dateLabel`, `dayName`, `isReadOnly`, `entries: List<Entry>`, `groups: List<ExpenseGrouped>`.
- VM combines `observeEntriesForDay` + `observeExpensesForDay`.
- Read-only mode: hide edit/delete affordances.
- Tap row → open edit sheet (CHF-39). Long-press → delete confirm (CHF-40).

## Acceptance Criteria
- [ ] Expense grouping correct (one header per category present that day).
- [ ] Read-only badge shown for past months.
- [ ] Empty state per section.
- [ ] Maps to FR-D1…FR-D3.
