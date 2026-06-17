# CHF-50 · DAO + Repository integration tests

| Field | Value |
|---|---|
| Type | Task · P1 · 5 SP |
| Blocked By | CHF-29, CHF-36, CHF-43 |
| Blocks | CHF-55 |

## Goal
Verify DB invariants and repository contracts against real Room.

## Hints
- Use `Room.inMemoryDatabaseBuilder` + `AndroidJUnit4`.
- Cover:
  - `MonthDao.upsertByYearMonth` idempotency
  - `promoteToCurrent` single-current invariant
  - `deletePastById` blocks current month
  - Cascade delete on Month removes entries + expenses
  - `ExpenseDao.countByCategory` accuracy + RESTRICT on category delete
  - `SeedCallback` inserts default categories
  - `FundRepositoryImpl.addEntry` updates `sumByMonth` Flow
  - `HistoryRepositoryImpl` past-month edit guard
- Add migration tests via `MigrationTestHelper` (placeholder until v2 schema bump).

## Acceptance Criteria
- [ ] All DAOs have CRUD coverage.
- [ ] Month-boundary simulation passes.
- [ ] CI runs these on emulator matrix.
