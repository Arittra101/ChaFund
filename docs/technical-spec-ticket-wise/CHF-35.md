# CHF-35 · `HistoryRepository` interface + domain models

| Field | Value |
|---|---|
| Type | Task · P0 · 3 SP |
| Blocked By | CHF-8 |
| Blocks | CHF-36 |

## Goal
Domain contract for daily/day-detail/monthly history + edit flow.

## Hints
- Package: `feature/history/domain/`.
- Domain models: `DailySummary(date, totalSpent: Money, balanceAtPoint: Money)`, `MonthSummary`, `ExpenseGrouped(category, expenses: List<Expense>)`.
- Methods:
  - `observeMonthSummaries(): Flow<List<MonthSummary>>`
  - `observeDailySummaries(monthId): Flow<List<DailySummary>>`
  - `observeEntriesForDay(monthId, date): Flow<List<Entry>>`
  - `observeExpensesForDay(monthId, date): Flow<List<ExpenseGrouped>>`
  - `updateEntry(id, amount, ref, date, time): Result<Unit, DataError.Local>`
  - `deleteEntry(id): Result<Unit, DataError.Local>`
  - `updateExpense(id, amount, categoryId, ref, date, time): Result<Unit, DataError.Local>`
  - `deleteExpense(id): Result<Unit, DataError.Local>`

## Acceptance Criteria
- [ ] No Room types in domain.
- [ ] All mutations return `Result`.
