# CHF-21 · `utilsModule` (Koin)

| Field | Value |
|---|---|
| Type | Task · P1 · 1 SP |
| Blocked By | CHF-9, CHF-20 |

## Goal
Register `DispatcherProvider` and `MonthManager` as Koin singletons.

## Hints
- Create `core/di/utilsModule.kt`.
- `single<DispatcherProvider> { DefaultDispatcherProvider() }`
- `singleOf(::MonthManager)`
- Add `utilsModule` to `appModules()` list.

## Acceptance Criteria
- [ ] `MonthManager` resolvable in `App.onCreate` for lifecycle registration.
- [ ] Koin module verification passes.
