# CHF-28 · `FundRepository` interface + domain models

| Field | Value |
|---|---|
| Type | Task · P0 · 3 SP |
| Blocked By | CHF-8 |
| Blocks | CHF-29 |

## Goal
Domain contract for Home + add flows. No Room types leak past this layer.

## Hints
- Package: `feature/fund/domain/`.
- Domain models: `Entry`, `Expense`, `MonthSummary`, `TimeCategory`, `Month`.
- Use `Money` for amounts (CHF-10).
- Interface methods:
  - `observeCurrentMonth(): Flow<Month?>`
  - `observeCurrentMonthSummary(): Flow<MonthSummary>`
  - `observeTimeCategories(): Flow<List<TimeCategory>>`
  - `addEntry(amount: Money, ref: String?): Result<Unit, DataError.Local>`
  - `addExpense(amount: Money, categoryId: Long, ref: String?): Result<Unit, DataError.Local>`

## Acceptance Criteria
- [ ] No Room imports in `domain/`.
- [ ] All mutations return `Result`.
