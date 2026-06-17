# CHF-43 · `SettingsRepository` + impl + `settingsModule`

| Field | Value |
|---|---|
| Type | Task · P1 · 3 SP |
| Blocked By | CHF-8, CHF-17, CHF-18 |
| Blocks | CHF-44 |

## Goal
Contract + implementation for Delete Month, Time Category CRUD, Theme.

## Hints
- Package: `feature/settings/`.
- Interface methods:
  - `observePastMonths(): Flow<List<Month>>`
  - `deletePastMonth(id): Result<Unit, DataError.Local>` — uses `monthDao.deletePastById` (guard built-in)
  - `observeTimeCategories(): Flow<List<TimeCategory>>`
  - `addCategory(name): Result<Unit, DataError.Local>` — unique check
  - `renameCategory(id, name): Result<Unit, DataError.Local>`
  - `deleteCategory(id): Result<Int, DataError.Local>` — pre-check `expenseDao.countByCategory(id)`; return `Error(NOT_FOUND)` if in use (with count via custom typed error if desired)
  - `themeFlow: Flow<ThemeMode>` (delegates to `LocalStorage`)
  - `setTheme(mode)`
- Module: `singleOf(::SettingsRepositoryImpl) bind ...` + VMs.

## Acceptance Criteria
- [ ] Deleting current month returns `NOT_FOUND` (DAO guard).
- [ ] Deleting in-use category returns error with count.
