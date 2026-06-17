# CHF-31 · `HomeViewModel` + `HomeScreenRoot` + `HomeScreen`

| Field | Value |
|---|---|
| Type | Story · P0 · 5 SP |
| Blocked By | CHF-26, CHF-30 |
| Blocks | CHF-32 |

## Goal
Home screen showing locked month badge, Balance + Spent, two action buttons.

## Hints
- Package: `feature/fund/presentation/home/`.
- `HomeUiState`: `monthLabel`, `balance: Money`, `spent: Money`, `isLoading`.
- `HomeUiEvent`: `OnAddEntryClicked`, `OnAddExpenseClicked`.
- VM `combine(repo.observeCurrentMonth(), repo.observeCurrentMonthSummary())` → `HomeUiState`.
- `HomeScreenRoot` resolves VM via `koinViewModel()`, calls `HomeScreen(state, onEvent, onNavigate)`.
- Layout: `LockedMonthBadge` → `SummaryCard` → action row with two `PrimaryButton`s → empty state when totals = 0.

## Acceptance Criteria
- [ ] Updates live without manual refresh.
- [ ] Negative balance renders red via `MoneyText`.
- [ ] Empty state shown when no records.
- [ ] Maps to PRD US-A1, US-A2, BR-L8, BR-C4.
