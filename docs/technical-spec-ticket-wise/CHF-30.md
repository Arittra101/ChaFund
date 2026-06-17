# CHF-30 · `fundModule` (Koin)

| Field | Value |
|---|---|
| Type | Task · P1 · 1 SP |
| Blocked By | CHF-29 |
| Blocks | CHF-31 |

## Goal
Register fund feature in Koin.

## Hints
- File: `feature/fund/di/fundModule.kt`.
- `singleOf(::FundRepositoryImpl) bind FundRepository::class`
- `viewModelOf(::HomeViewModel)`, `viewModelOf(::AddEntryViewModel)`, `viewModelOf(::AddExpenseViewModel)`.
- Add `fundModule` to `appModules()`.

## Acceptance Criteria
- [ ] VMs resolvable via `koinViewModel()` in composables.
