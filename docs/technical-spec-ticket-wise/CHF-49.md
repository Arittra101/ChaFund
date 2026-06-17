# CHF-49 · Unit tests for all ViewModels

| Field | Value |
|---|---|
| Type | Task · P1 · 8 SP |
| Blocked By | CHF-33, CHF-41, CHF-47 |
| Blocks | CHF-55 |

## Goal
≥ 80% line coverage on ViewModel layer. Each `UiEvent` exercised.

## Hints
- Use **Turbine** + fake repositories (no MockK for simple cases).
- Cover: Home, AddEntry, AddExpense, DailyHistory, DayDetail, EditEntry, EditExpense, Delete confirm, MonthlyHistory, Settings, Categories, DeleteMonth.
- Pattern: `runTest { vm.uiState.test { ... } }`.
- Test validation paths (amount ≤ 0, missing category, empty name).
- Test event-to-state transitions, not implementation details.

## Acceptance Criteria
- [ ] All VM events covered.
- [ ] Coverage report ≥ 80% on `feature/*/presentation/`.
