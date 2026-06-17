# CHF-29 · `FundRepositoryImpl` + entity → domain mappers

| Field | Value |
|---|---|
| Type | Task · P0 · 5 SP |
| Blocked By | CHF-17, CHF-19, CHF-28 |
| Blocks | CHF-30 |

## Goal
Implement `FundRepository` against shared DAOs + `Session`. Never throw — wrap errors as `DataError.Local`.

## Hints
- Package: `feature/fund/data/`.
- Constructor: `MonthDao`, `EntryDao`, `ExpenseDao`, `TimeCategoryDao`, `Session`, `DispatcherProvider`.
- Read flows via `session.currentMonthId.flatMapLatest { ... }`.
- Combine `entryDao.sumByMonth(id)` + `expenseDao.sumByMonth(id)` + `monthDao.observeCurrent()` → `MonthSummary`.
- Insert path: auto-capture `date = LocalDate.now().toEpochDay()`, `time = LocalTime.now().format("HH:mm")`, `createdAt/updatedAt = System.currentTimeMillis()`.
- Catch `SQLiteException` → `DataError.Local.UNKNOWN` (full disk → `DISK_FULL`).
- Mappers in `data/mapper/` — `EntryEntity.toDomain()`, etc.

## Acceptance Criteria
- [ ] `addEntry`/`addExpense` use `session.currentMonthId.value`.
- [ ] No throws — all paths return `Result`.
- [ ] Integration test against in-memory Room verifies sums update via Flow.
