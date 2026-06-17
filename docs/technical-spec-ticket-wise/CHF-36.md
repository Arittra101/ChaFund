# CHF-36 · `HistoryRepositoryImpl` + mappers + `historyModule`

| Field | Value |
|---|---|
| Type | Task · P0 · 5 SP |
| Blocked By | CHF-17, CHF-35 |
| Blocks | CHF-37, CHF-41 |

## Goal
Implementation with SQL aggregations + Koin module registration.

## Hints
- Package: `feature/history/data/`.
- Constructor: `MonthDao`, `EntryDao`, `ExpenseDao`, `TimeCategoryDao`, `DispatcherProvider`.
- For `observeDailySummaries`: use SQL projection from TECH_SPEC §6.6 (`UNION` of entry+expense dates, `GROUP BY date`).
- For `observeExpensesForDay`: use `ExpenseWithCategory` and group by `category.id` in Kotlin (small result set).
- Past-month guard on `updateEntry`/`updateExpense`: pre-check `monthDao.findByYearMonth(...)` or filter in `WHERE` clause.
- Map entities → domain via `data/mapper/`.
- Create `historyModule.kt`: `singleOf(::HistoryRepositoryImpl) bind HistoryRepository::class` + VMs.

## Acceptance Criteria
- [ ] Daily summary `balanceAtPoint` matches running balance up to that date.
- [ ] Monthly summary list ordered year/month desc.
- [ ] Edit on past-month record returns `DataError.Local.NOT_FOUND`.
