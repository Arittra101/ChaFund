# Cha Fund — Ticket Tech Specs Index

One file per implementation ticket. See [`../TICKETS.md`](../TICKETS.md) for the full backlog with priorities, estimates, and dependency graph.

## Epic 1 — Foundation & Setup
- [CHF-2](./CHF-2.md) Gradle version catalog + dependencies
- [CHF-3](./CHF-3.md) ChaFundTheme (Material 3)
- [CHF-4](./CHF-4.md) AppLogger (Timber)
- [CHF-5](./CHF-5.md) App + Koin bootstrap
- [CHF-6](./CHF-6.md) MainActivity + empty AppNavHost

## Epic 2 — Core Infrastructure
- [CHF-8](./CHF-8.md) Result / DataError.Local
- [CHF-9](./CHF-9.md) DispatcherProvider
- [CHF-10](./CHF-10.md) Money value class
- [CHF-11](./CHF-11.md) DateTimeFormat utils
- [CHF-12](./CHF-12.md) ChaFundDb skeleton + TypeConverters
- [CHF-13](./CHF-13.md) MonthEntity + MonthDao
- [CHF-14](./CHF-14.md) TimeCategoryEntity + DAO + SeedCallback
- [CHF-15](./CHF-15.md) EntryEntity + EntryDao
- [CHF-16](./CHF-16.md) ExpenseEntity + ExpenseDao
- [CHF-17](./CHF-17.md) databaseModule (Koin)
- [CHF-18](./CHF-18.md) LocalStorage + DataStore + storageModule
- [CHF-19](./CHF-19.md) Session + sessionModule
- [CHF-20](./CHF-20.md) MonthManager + lifecycle hook
- [CHF-21](./CHF-21.md) utilsModule (Koin)

## Epic 3 — Navigation & Shared UI
- [CHF-23](./CHF-23.md) Route sealed interface
- [CHF-24](./CHF-24.md) Navigator façade + TopLevelDestination
- [CHF-25](./CHF-25.md) ChaFundBottomBar
- [CHF-26](./CHF-26.md) Shared composables (MoneyText, SummaryCard, …)

## Epic 4 — Fund Feature
- [CHF-28](./CHF-28.md) FundRepository interface + domain models
- [CHF-29](./CHF-29.md) FundRepositoryImpl + mappers
- [CHF-30](./CHF-30.md) fundModule (Koin)
- [CHF-31](./CHF-31.md) HomeViewModel + Screen
- [CHF-32](./CHF-32.md) Add Entry sheet
- [CHF-33](./CHF-33.md) Add Expense sheet

## Epic 5 — History Feature
- [CHF-35](./CHF-35.md) HistoryRepository interface + models
- [CHF-36](./CHF-36.md) HistoryRepositoryImpl + mappers + historyModule
- [CHF-37](./CHF-37.md) DailyHistory ViewModel + Screen
- [CHF-38](./CHF-38.md) DayDetail ViewModel + Screen
- [CHF-39](./CHF-39.md) Edit Entry / Expense sheets
- [CHF-40](./CHF-40.md) Delete entry/expense with confirmation
- [CHF-41](./CHF-41.md) MonthlyHistory ViewModel + Screen

## Epic 6 — Settings Feature
- [CHF-43](./CHF-43.md) SettingsRepository + impl + settingsModule
- [CHF-44](./CHF-44.md) SettingsScreen (locked current month + entry points)
- [CHF-45](./CHF-45.md) Delete Past Month flow
- [CHF-46](./CHF-46.md) Time Categories CRUD
- [CHF-47](./CHF-47.md) Theme selection (Light / Dark / System)

## Epic 7 — Quality & Testing
- [CHF-49](./CHF-49.md) Unit tests for all ViewModels
- [CHF-50](./CHF-50.md) DAO + Repository integration tests
- [CHF-51](./CHF-51.md) Compose UI tests for golden paths
- [CHF-52](./CHF-52.md) ktlint + detekt + Lint + CI

## Epic 8 — Release Preparation
- [CHF-54](./CHF-54.md) Manifest + R8 + accessibility hardening
- [CHF-55](./CHF-55.md) Final QA pass

---

## Spec depth note

- **CHF-2 through CHF-20** include full code sketches (foundation + core data layer where correctness is critical).
- **CHF-21 onward** are kept brief — high-level hints only. Engineers fill in details during implementation against the parent [TECH_SPEC.md](../TECH_SPEC.md).
