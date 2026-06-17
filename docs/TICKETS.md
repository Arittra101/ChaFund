# Cha Fund — Engineering Tickets (Jira-style)

| Field | Value |
|---|---|
| Document | Cha Fund Backlog |
| Version | 1.0 |
| Date | 2026-06-17 |
| Project Key | **CHF** |
| Related Docs | [BRD.md](./BRD.md) · [PRD.md](./PRD.md) · [TECH_SPEC.md](./TECH_SPEC.md) |
| Total Tickets | 55 (8 Epics + 47 Stories/Tasks) |

---

## Conventions

- **Ticket ID**: `CHF-<N>` (sequential, never reused).
- **Type**: `Epic` · `Story` · `Task` · `Spike` · `Bug`.
- **Priority**: `P0` (blocker) · `P1` (high) · `P2` (medium) · `P3` (low).
- **Estimate**: story points (1, 2, 3, 5, 8, 13).
- **Status**: `To Do` (default) · `In Progress` · `In Review` · `Done`.
- **Blocked By**: list of `CHF-<N>` IDs that **must be Done** before this can start.
- Dependencies are designed so that ticket order = safe implementation order.

---

## Epic Overview & Dependency Graph

```
EPIC-1 Foundation & Setup
        │
        ▼
EPIC-2 Core Infrastructure  (Result, Money, DB, DAOs, Session, MonthManager)
        │
        ▼
EPIC-3 Navigation & Shared UI  (Routes, Navigator, BottomBar, shared composables)
        │
        ├──────────────┬──────────────┐
        ▼              ▼              ▼
EPIC-4 Fund      EPIC-5 History  EPIC-6 Settings
        │              │              │
        └──────────────┼──────────────┘
                       ▼
              EPIC-7 Quality & Testing
                       ▼
              EPIC-8 Release Preparation
```

| Epic | ID | Title | Tickets |
|---|---|---|---|
| EPIC-1 | CHF-1 | Foundation & Setup | CHF-2 … CHF-6 |
| EPIC-2 | CHF-7 | Core Infrastructure | CHF-8 … CHF-21 |
| EPIC-3 | CHF-22 | Navigation & Shared UI | CHF-23 … CHF-26 |
| EPIC-4 | CHF-27 | Fund Feature (Home + Add) | CHF-28 … CHF-33 |
| EPIC-5 | CHF-34 | History Feature | CHF-35 … CHF-41 |
| EPIC-6 | CHF-42 | Settings Feature | CHF-43 … CHF-47 |
| EPIC-7 | CHF-48 | Quality & Testing | CHF-49 … CHF-52 |
| EPIC-8 | CHF-53 | Release Preparation | CHF-54 … CHF-55 |

---

# EPIC-1 — Foundation & Setup

---

## CHF-1 · Epic: Foundation & Setup
**Type**: Epic · **Priority**: P0 · **Estimate**: —
**Blocked By**: —

**Goal**: Establish a buildable Kotlin + Compose + Material 3 Android project with version catalog, Koin bootstrap, base theme, logging, and a single Activity hosting an empty `AppNavHost`.

**Definition of Done**:
- `./gradlew assembleDebug` succeeds.
- App launches to an empty Compose surface.
- Koin initialized in `App.onCreate`.
- ktlint + detekt configured (rules enforced in CHF-50).

**Child Tickets**: CHF-2, CHF-3, CHF-4, CHF-5, CHF-6.

---

## CHF-2 · Set up Gradle version catalog and core dependencies
**Type**: Task · **Priority**: P0 · **Estimate**: 3
**Blocked By**: —

**Description**: Create `gradle/libs.versions.toml` containing pinned versions for Kotlin, Compose BOM, AGP, KSP, Room, Koin, DataStore, Navigation Compose, Lifecycle, Coroutines, Timber, kotlinx.serialization, JUnit, MockK, Turbine. Wire it into `build.gradle.kts` and `app/build.gradle.kts`. Enable `coreLibraryDesugaring` (min SDK 24).

**Acceptance Criteria**:
- [ ] `libs.versions.toml` exists with all required entries.
- [ ] Module `build.gradle.kts` references via `libs.xxx`.
- [ ] KSP plugin applied.
- [ ] Compose compiler set up (Kotlin 1.9+ via the Compose plugin).
- [ ] `compileOptions { coreLibraryDesugaringEnabled = true }` enabled.
- [ ] App syncs and builds.

**Tech Notes**: See [TECH_SPEC.md §5](./TECH_SPEC.md).

---

## CHF-3 · Set up ChaFundTheme (Material 3, Light/Dark/System)
**Type**: Task · **Priority**: P1 · **Estimate**: 2
**Blocked By**: CHF-2

**Description**: Create `theme/ChaFundTheme.kt`, `Color.kt`, `Type.kt`. Define light + dark `ColorScheme` with disabled dynamic color (consistent brand). Accept `ThemeMode = LIGHT | DARK | SYSTEM`. Default `SYSTEM`.

**Acceptance Criteria**:
- [ ] `ChaFundTheme(themeMode)` composable wraps content in `MaterialTheme`.
- [ ] Dark mode renders correct surface/onSurface contrast.
- [ ] No dynamic color (no `dynamicLight/DarkColorScheme`).

---

## CHF-4 · Wire AppLogger (Timber)
**Type**: Task · **Priority**: P2 · **Estimate**: 1
**Blocked By**: CHF-2

**Description**: Create `core/utils/AppLogger.kt` as a thin Timber wrapper. Plant `DebugTree` in debug builds; no-op in release.

**Acceptance Criteria**:
- [ ] `AppLogger.init()` called from `App.onCreate`.
- [ ] Release build strips logs via R8.

---

## CHF-5 · Create `App` (Application) with `startKoin` bootstrap
**Type**: Task · **Priority**: P0 · **Estimate**: 2
**Blocked By**: CHF-2, CHF-4

**Description**: Create `App : Application()`. In `onCreate`: call `AppLogger.init()`, `startKoin { androidLogger(); androidContext(this@App); modules(appModules()) }`. `appModules()` returns an empty list for now — modules added in later tickets.

**Acceptance Criteria**:
- [ ] `App` class registered in `AndroidManifest.xml` (`android:name`).
- [ ] Koin starts without crash.
- [ ] `appModules.kt` aggregator exists (empty list initially).

---

## CHF-6 · Single `MainActivity` + empty `AppNavHost`
**Type**: Task · **Priority**: P0 · **Estimate**: 2
**Blocked By**: CHF-3, CHF-5

**Description**: Create `MainActivity` that sets Compose content with `ChaFundTheme { AppNavHost(...) }`. Placeholder `AppNavHost` shows a single empty screen.

**Acceptance Criteria**:
- [ ] App launches to a blank Compose screen.
- [ ] `ComponentActivity` (single Activity) — no Fragment use.
- [ ] No `INTERNET` permission in `AndroidManifest.xml`.

---

# EPIC-2 — Core Infrastructure

---

## CHF-7 · Epic: Core Infrastructure
**Type**: Epic · **Priority**: P0 · **Estimate**: —
**Blocked By**: CHF-1

**Goal**: Stand up the data layer (Room DB + DAOs), domain primitives (`Result`, `DataError`, `Money`), cross-feature `Session`, and `MonthManager` lifecycle hook — everything features will depend on.

**Definition of Done**:
- Database creates on first launch and seeds default Time Categories.
- `MonthManager` upserts and promotes the current month on app launch + `ON_RESUME`.
- `Session.currentMonthId` reflects the active month.
- All core modules registered via Koin.

**Child Tickets**: CHF-8 … CHF-21.

---

## CHF-8 · Domain primitives: `Result`, `RootError`, `DataError.Local`
**Type**: Task · **Priority**: P0 · **Estimate**: 2
**Blocked By**: CHF-2

**Description**: Add `core/domain/Result.kt` and `core/domain/DataError.kt`. `DataError.Local` enum: `NOT_FOUND`, `DISK_FULL`, `UNKNOWN`. No `Remote` variant.

**Acceptance Criteria**:
- [ ] `Result<D, E>` sealed interface with `Success` + `Error`.
- [ ] `RootError` marker interface.
- [ ] `DataError : RootError` with `enum class Local`.
- [ ] Unit test covers exhaustive `when` over `DataError.Local`.

---

## CHF-9 · `DispatcherProvider` for testable threading
**Type**: Task · **Priority**: P1 · **Estimate**: 1
**Blocked By**: CHF-2

**Description**: Interface exposing `io`, `default`, `main`. Provide `DefaultDispatcherProvider` impl.

**Acceptance Criteria**:
- [ ] Interface + impl in `core/domain/`.
- [ ] Test double in `testFixtures` or test sources.

---

## CHF-10 · `Money` value class + `Tk` formatter
**Type**: Task · **Priority**: P0 · **Estimate**: 2
**Blocked By**: CHF-2

**Description**: `Money` wraps `Long amountPaisa`. `formatTk()` returns `"Tk 1,250.50"` with locale-grouped digits. Provide `Money.fromTk(double)` and `Money.zero`.

**Acceptance Criteria**:
- [ ] No floating-point math on amounts.
- [ ] Unit tests: 0, positive, negative, grouping, two-decimal precision.

---

## CHF-11 · `DateTimeFormat` utils (`dd MMMM yy`, day name, `HH:mm`)
**Type**: Task · **Priority**: P1 · **Estimate**: 1
**Blocked By**: CHF-2

**Description**: Centralize date/time formatting. `formatDate(epochDay) → "16 June 26"`. `dayOfWeek(epochDay) → "Tuesday"`. `formatTime(LocalTime) → "14:30"`.

**Acceptance Criteria**:
- [ ] Uses `java.time` (desugared).
- [ ] Unit tests for each formatter.

---

## CHF-12 · Room `ChaFundDb` skeleton + `TypeConverter`s
**Type**: Task · **Priority**: P0 · **Estimate**: 3
**Blocked By**: CHF-2

**Description**: Create `ChaFundDb : RoomDatabase()` with version 1, empty entity list (entities added in subsequent tickets). Add `ChaFundTypeConverters` for any custom types. Enable `room.schemaLocation` export. **No `fallbackToDestructiveMigration` in release.**

**Acceptance Criteria**:
- [ ] DB compiles with KSP.
- [ ] Schema JSON exported to `app/schemas/`.
- [ ] `room.schemaLocation` build arg configured.

---

## CHF-13 · `MonthEntity` + `MonthDao`
**Type**: Task · **Priority**: P0 · **Estimate**: 3
**Blocked By**: CHF-12

**Description**: Entity per [TECH_SPEC §6.2](./TECH_SPEC.md). Unique index on `(year, month)`. DAO surface:
- `observeCurrent(): Flow<MonthEntity?>`
- `findByYearMonth(y, m): MonthEntity?`
- `upsertByYearMonth(...): Long` (`@Transaction`, idempotent)
- `promoteToCurrent(id)` — `@Transaction`: unflag all + flag this
- `observeAll(): Flow<List<MonthEntity>>`
- `observePast(): Flow<List<MonthEntity>>`
- `deletePastById(id): Int` — `WHERE id = :id AND isCurrent = 0`
- `observeMonthSummaries(): Flow<List<MonthSummaryProjection>>`

**Acceptance Criteria**:
- [ ] FK `onDelete = CASCADE` declared.
- [ ] DAO tests cover idempotent upsert + single-current invariant.
- [ ] `deletePastById` returns 0 when invoked on current month.

---

## CHF-14 · `TimeCategoryEntity` + `TimeCategoryDao` + `SeedCallback`
**Type**: Task · **Priority**: P0 · **Estimate**: 3
**Blocked By**: CHF-12

**Description**: Global category table (no `monthId`). Unique `name COLLATE NOCASE`. DAO: `observeAll`, `insert`, `rename`, `deleteById`. `SeedCallback` (Room `Callback.onCreate`) inserts `Morning`, `Noon`, `Afternoon`, `Evening` (sortOrder 1–4).

**Acceptance Criteria**:
- [ ] Default categories present on first DB creation.
- [ ] Seeding does not run on subsequent launches.
- [ ] Renaming an in-use category reflects on existing expenses (FK preserved).

---

## CHF-15 · `EntryEntity` + `EntryDao`
**Type**: Task · **Priority**: P0 · **Estimate**: 3
**Blocked By**: CHF-13

**Description**: Entity per spec. `amountPaisa: Long`. FK `monthId → Month ON DELETE CASCADE`. DAO:
- `observeByMonth(monthId): Flow<List<EntryEntity>>`
- `observeByDate(monthId, date): Flow<List<EntryEntity>>`
- `sumByMonth(monthId): Flow<Long>`
- `insert`, `update`, `deleteById`

**Acceptance Criteria**:
- [ ] Indexes on `monthId`, `date`.
- [ ] DAO tests cover sum aggregation and cascade delete.

---

## CHF-16 · `ExpenseEntity` + `ExpenseDao`
**Type**: Task · **Priority**: P0 · **Estimate**: 3
**Blocked By**: CHF-13, CHF-14

**Description**: Entity per spec. FK `monthId → Month CASCADE`; `timeCategoryId → TimeCategory RESTRICT`. DAO:
- `observeByMonth(monthId): Flow<List<ExpenseEntity>>`
- `observeByDate(monthId, date): Flow<List<ExpenseWithCategory>>` (`@Relation` or JOIN)
- `sumByMonth(monthId): Flow<Long>`
- `countByCategory(catId): Int`
- `insert`, `update`, `deleteById`

**Acceptance Criteria**:
- [ ] RESTRICT enforces blocked deletion of in-use category.
- [ ] DAO tests cover sum + relation load.

---

## CHF-17 · `databaseModule` (Koin) — DB + DAO providers
**Type**: Task · **Priority**: P0 · **Estimate**: 2
**Blocked By**: CHF-13, CHF-14, CHF-15, CHF-16

**Description**: `databaseModule` provides `ChaFundDb` (single), all four DAOs (single). Register in `appModules()`.

**Acceptance Criteria**:
- [ ] DAOs resolvable via Koin from any feature module.
- [ ] DB built with `SeedCallback` attached.

---

## CHF-18 · `LocalStorage` + `DataStoreLocalStorage` + `storageModule`
**Type**: Task · **Priority**: P1 · **Estimate**: 2
**Blocked By**: CHF-2

**Description**: Interface `LocalStorage` for typed read/write of preferences. Impl uses DataStore Preferences. Single key for now: `theme_mode`. Register module in `appModules()`.

**Acceptance Criteria**:
- [ ] `themeFlow: Flow<ThemeMode>` available.
- [ ] `setTheme(mode)` persists across cold start.

---

## CHF-19 · `Session` + `sessionModule`
**Type**: Task · **Priority**: P0 · **Estimate**: 2
**Blocked By**: CHF-2

**Description**: `Session` per [TECH_SPEC §9.1](./TECH_SPEC.md): `currentMonthId: StateFlow<Long>` + `monthChanged: SharedFlow<Unit>`. Register `Session` as `single` in Koin.

**Acceptance Criteria**:
- [ ] `setCurrentMonth(id)` only emits when value changes.
- [ ] Unit test verifies de-dup behavior.

---

## CHF-20 · `MonthManager` + `ProcessLifecycleOwner` hook
**Type**: Task · **Priority**: P0 · **Estimate**: 5
**Blocked By**: CHF-13, CHF-19, CHF-5

**Description**: `MonthManager` (in `core/utils/`) detects current calendar month via `LocalDate.now()`, upserts `MonthEntity`, calls `promoteToCurrent`, publishes id to `Session`. Implements `LifecycleEventObserver`; runs on `ON_RESUME`. Registered as observer in `App.onCreate`.

**Acceptance Criteria**:
- [ ] First launch creates current month at Tk 0.
- [ ] App resumed across a simulated date change promotes the new month.
- [ ] `(year, month)` unique index prevents duplicates.
- [ ] Idempotent: calling `detectAndPromote()` twice is safe.
- [ ] Integration test covers month-boundary simulation.

**Tech Notes**: See [TECH_SPEC §9.2](./TECH_SPEC.md).

---

## CHF-21 · `utilsModule` (Koin) — MonthManager, AppLogger, dispatchers
**Type**: Task · **Priority**: P1 · **Estimate**: 1
**Blocked By**: CHF-9, CHF-20

**Description**: Aggregate `MonthManager`, `DispatcherProvider`, `AppLogger` providers. Register in `appModules()`.

**Acceptance Criteria**:
- [ ] All utilities resolvable via Koin.

---

# EPIC-3 — Navigation & Shared UI

---

## CHF-22 · Epic: Navigation & Shared UI
**Type**: Epic · **Priority**: P0 · **Estimate**: —
**Blocked By**: CHF-7

**Goal**: Type-safe routes, `Navigator` façade, bottom bar with 4 destinations, shared composables used across features.

**Child Tickets**: CHF-23, CHF-24, CHF-25, CHF-26.

---

## CHF-23 · `Route` sealed interface (typed, `@Serializable`)
**Type**: Task · **Priority**: P0 · **Estimate**: 2
**Blocked By**: CHF-2

**Description**: Per [TECH_SPEC §10.1](./TECH_SPEC.md): `Home`, `DailyHistory(monthId)`, `DayDetail(monthId, dateEpoch)`, `MonthlyHistory`, `Settings`. Add kotlinx.serialization plugin.

**Acceptance Criteria**:
- [ ] All routes are `@Serializable`.
- [ ] No auth-related routes.

---

## CHF-24 · `Navigator` façade + `TopLevelDestination`
**Type**: Task · **Priority**: P0 · **Estimate**: 3
**Blocked By**: CHF-23

**Description**: `Navigator` interface with `navigateTo`, `navigateToTopLevel`, `navigateBack`. Backed by a `SharedFlow<NavEvent>` collected in `AppNavHost`. `TopLevelDestination` enum: Home, Daily, Months, Settings.

**Acceptance Criteria**:
- [ ] `Navigator` injectable via Koin (single).
- [ ] `AppNavHost` reacts to `navEvents`.
- [ ] No `protectedRoutes` map; no `onAuthRequired`.

---

## CHF-25 · `ChaFundBottomBar`
**Type**: Task · **Priority**: P1 · **Estimate**: 2
**Blocked By**: CHF-24

**Description**: Material 3 `NavigationBar` with 4 destinations. Selected state synced to current backstack entry.

**Acceptance Criteria**:
- [ ] Tap routes to top-level destination, clearing intermediate stack.
- [ ] Selected icon highlights correctly.

---

## CHF-26 · Shared composables (`PrimaryButton`, `EmptyView`, `ConfirmationBottomSheet`, `PillTag`, `MoneyText`, `SummaryCard`, `LockedMonthBadge`)
**Type**: Task · **Priority**: P1 · **Estimate**: 5
**Blocked By**: CHF-3, CHF-10

**Description**: Build the reusable component library in `core/presentation/components/`. `MoneyText` renders negative amounts in `colorScheme.error` with explicit `-` sign.

**Acceptance Criteria**:
- [ ] Each component has at least one `@Preview`.
- [ ] `MoneyText` snapshot/UI test covers positive, zero, negative.
- [ ] Touch targets ≥ 48dp.

---

# EPIC-4 — Fund Feature (Home + Add)

---

## CHF-27 · Epic: Fund Feature
**Type**: Epic · **Priority**: P0 · **Estimate**: —
**Blocked By**: CHF-22

**Goal**: Implement Home screen showing locked current-month badge, `SummaryCard`, and Add Entry / Add Expense action sheets. Live updates via Flow.

**Child Tickets**: CHF-28 … CHF-33.

---

## CHF-28 · `FundRepository` interface + domain models
**Type**: Task · **Priority**: P0 · **Estimate**: 3
**Blocked By**: CHF-8

**Description**: In `feature/fund/domain/`: `Entry`, `Expense`, `MonthSummary`, `TimeCategory` (domain), `FundRepository` interface. Methods:
- `observeCurrentMonthSummary(): Flow<MonthSummary>`
- `observeCurrentMonth(): Flow<Month?>`
- `observeTimeCategories(): Flow<List<TimeCategory>>`
- `addEntry(amount: Money, ref: String?): Result<Unit, DataError.Local>`
- `addExpense(amount: Money, categoryId: Long, ref: String?): Result<Unit, DataError.Local>`

**Acceptance Criteria**:
- [ ] No Room types in domain.
- [ ] All methods return `Flow` or `Result`.

---

## CHF-29 · `FundRepositoryImpl` + entity → domain mappers
**Type**: Task · **Priority**: P0 · **Estimate**: 5
**Blocked By**: CHF-17, CHF-19, CHF-28

**Description**: Implementation depends on `MonthDao`, `EntryDao`, `ExpenseDao`, `TimeCategoryDao`, `Session`. Mappers in `data/mapper/`. Auto-capture `date`/`time` at insert. Never throws — wraps Room exceptions to `DataError.Local`.

**Acceptance Criteria**:
- [ ] `addEntry`/`addExpense` use `session.currentMonthId.value`.
- [ ] Integration test against in-memory Room: balance updates after insert.

---

## CHF-30 · `fundModule` (Koin)
**Type**: Task · **Priority**: P1 · **Estimate**: 1
**Blocked By**: CHF-29

**Description**: Register `FundRepositoryImpl as FundRepository` + ViewModels (`HomeViewModel`, `AddEntryViewModel`, `AddExpenseViewModel`) using `singleOf` / `viewModelOf`. Add to `appModules()`.

**Acceptance Criteria**:
- [ ] All resolvable from `koinViewModel()` in composables.

---

## CHF-31 · `HomeViewModel` + `HomeScreenRoot` + `HomeScreen`
**Type**: Story · **Priority**: P0 · **Estimate**: 5
**Blocked By**: CHF-26, CHF-30

**Description**: VM exposes `StateFlow<HomeUiState>` combining current month + summary. Screen shows locked badge, `SummaryCard(balance, spent)`, two action buttons that open sheets via callback.

**Acceptance Criteria**:
- [ ] Current month badge non-interactive (lock icon).
- [ ] Balance/Spent update live without manual refresh.
- [ ] Empty state shown when no records.
- [ ] Negative balance renders red.

**Maps to PRD**: US-A1, US-A2, BR-L8, BR-C4.

---

## CHF-32 · Add Entry sheet (`AddEntryViewModel` + `AddEntrySheet`)
**Type**: Story · **Priority**: P0 · **Estimate**: 3
**Blocked By**: CHF-31

**Description**: Modal bottom sheet with amount + optional ref (max 80 chars). Validation: amount > 0. On save → `FundRepository.addEntry`; close sheet; snackbar "Entry added".

**Acceptance Criteria**:
- [ ] Save button disabled until amount valid.
- [ ] Inline error for amount ≤ 0.
- [ ] Auto-captured `date`/`time` not shown.
- [ ] Balance updates in Home within 200 ms.

**Maps to PRD**: US-A1, FR-E1…FR-E5.

---

## CHF-33 · Add Expense sheet (`AddExpenseViewModel` + `AddExpenseSheet`)
**Type**: Story · **Priority**: P0 · **Estimate**: 5
**Blocked By**: CHF-32

**Description**: Modal sheet with amount + Time Category picker (required) + optional ref. Validation: amount > 0, category selected.

**Acceptance Criteria**:
- [ ] Category dropdown populated from `observeTimeCategories()`.
- [ ] Inline errors for amount and category.
- [ ] Balance decreases & Spent increases on save.

**Maps to PRD**: US-A2, FR-X1…FR-X5.

---

# EPIC-5 — History Feature

---

## CHF-34 · Epic: History Feature
**Type**: Epic · **Priority**: P0 · **Estimate**: —
**Blocked By**: CHF-22

**Goal**: Daily History, Day Detail, Monthly History, plus inline edit/delete flow for entries and expenses.

**Child Tickets**: CHF-35 … CHF-41.

---

## CHF-35 · `HistoryRepository` interface + domain models
**Type**: Task · **Priority**: P0 · **Estimate**: 3
**Blocked By**: CHF-8

**Description**: Methods:
- `observeMonthSummaries(): Flow<List<MonthSummary>>`
- `observeDailySummaries(monthId): Flow<List<DailySummary>>`
- `observeEntriesForDay(monthId, date): Flow<List<Entry>>`
- `observeExpensesForDay(monthId, date): Flow<List<ExpenseGrouped>>`  // grouped by Time Category
- `updateEntry(...)`, `deleteEntry(id)`, `updateExpense(...)`, `deleteExpense(id)` — all return `Result<Unit, DataError.Local>`

**Acceptance Criteria**:
- [ ] Past-month edit attempts blocked at repository layer (guarded query).
- [ ] No Room types leak.

---

## CHF-36 · `HistoryRepositoryImpl` + mappers + `historyModule`
**Type**: Task · **Priority**: P0 · **Estimate**: 5
**Blocked By**: CHF-17, CHF-35

**Description**: Implementation with SQL aggregations per [TECH_SPEC §6.6](./TECH_SPEC.md). Register module in Koin.

**Acceptance Criteria**:
- [ ] Daily summary projection returns expected `balanceAtThatPoint`.
- [ ] Monthly summary list ordered year/month desc.

---

## CHF-37 · `DailyHistoryViewModel` + `DailyHistoryScreenRoot/Screen`
**Type**: Story · **Priority**: P0 · **Estimate**: 5
**Blocked By**: CHF-26, CHF-36

**Description**: Defaults to current month from `Session.currentMonthId`. Top bar shows `Spent | Balance`; list of dates desc with day total + running balance.

**Acceptance Criteria**:
- [ ] When opened from bottom nav, lands on current month.
- [ ] When opened from Monthly History, shows selected month.
- [ ] Past month → read-only badge, no edit affordances.
- [ ] Empty state when no activity.

**Maps to PRD**: US-C1, FR-H0…FR-H4.

---

## CHF-38 · `DayDetailViewModel` + `DayDetailScreenRoot/Screen`
**Type**: Story · **Priority**: P0 · **Estimate**: 5
**Blocked By**: CHF-37

**Description**: Sections: Entries (list), Expenses (grouped by Time Category). Tap row → edit sheet; long-press → delete confirm.

**Acceptance Criteria**:
- [ ] Grouping correct (one section per Time Category present in that day).
- [ ] Read-only mode for past months hides edit/delete.

**Maps to PRD**: US-C2, FR-D1…FR-D3.

---

## CHF-39 · Edit sheets (`EditEntrySheet`, `EditExpenseSheet`) + edit ViewModels
**Type**: Story · **Priority**: P0 · **Estimate**: 5
**Blocked By**: CHF-38

**Description**: Inline bottom sheets prefilled with current values. Same validation as add flows. On save → repository update → totals recompute via Flow.

**Acceptance Criteria**:
- [ ] Amount and ref editable; date/time editable only here.
- [ ] Edits to current-month records succeed.
- [ ] Edits to past-month records blocked (UI affordance hidden + repository guard).

**Maps to PRD**: FR-ED1…FR-ED4.

---

## CHF-40 · Delete entry/expense with confirmation
**Type**: Story · **Priority**: P1 · **Estimate**: 3
**Blocked By**: CHF-39

**Description**: Confirmation sheet ("Delete this entry/expense?") triggers repository delete. Snackbar with **Undo** action (re-insert) — optional v1.

**Acceptance Criteria**:
- [ ] Confirm dialog gates delete.
- [ ] Totals update immediately after delete.

---

## CHF-41 · `MonthlyHistoryViewModel` + `MonthlyHistoryScreenRoot/Screen`
**Type**: Story · **Priority**: P0 · **Estimate**: 3
**Blocked By**: CHF-36, CHF-26

**Description**: List of all months (latest first) with entries total, Spent, Balance. Tap → Daily History for that month.

**Acceptance Criteria**:
- [ ] Months ordered year desc, month desc.
- [ ] Each row shows three figures with `MoneyText`.

**Maps to PRD**: US-C3, FR-M1, FR-M2.

---

# EPIC-6 — Settings Feature

---

## CHF-42 · Epic: Settings Feature
**Type**: Epic · **Priority**: P1 · **Estimate**: —
**Blocked By**: CHF-22

**Goal**: Settings screen with locked current month row, Delete Past Month flow, Time Categories CRUD, Theme selection.

**Child Tickets**: CHF-43 … CHF-47.

---

## CHF-43 · `SettingsRepository` + impl + `settingsModule`
**Type**: Task · **Priority**: P1 · **Estimate**: 3
**Blocked By**: CHF-8, CHF-17, CHF-18

**Description**: Methods:
- `observePastMonths(): Flow<List<Month>>`
- `deletePastMonth(monthId): Result<Unit, DataError.Local>`  // guarded
- `observeTimeCategories(): Flow<List<TimeCategory>>`
- `addCategory(name)`, `renameCategory(id, name)`, `deleteCategory(id): Result<Unit, DataError.Local>`  // checks `countByCategory`
- `themeFlow: Flow<ThemeMode>`, `setTheme(mode)`

**Acceptance Criteria**:
- [ ] Delete current month attempt returns `NOT_FOUND` (DAO-guarded).
- [ ] Delete in-use category returns error with affected count.

---

## CHF-44 · `SettingsScreenRoot/Screen` with locked current-month row
**Type**: Story · **Priority**: P1 · **Estimate**: 3
**Blocked By**: CHF-26, CHF-43

**Description**: Top row: current month, read-only, lock icon. Entry points to sub-screens (Delete Month, Categories, Theme).

**Acceptance Criteria**:
- [ ] Current month row is non-interactive.
- [ ] About section shows version + "100% offline".

**Maps to PRD**: FR-S1.

---

## CHF-45 · Delete Past Month flow
**Type**: Story · **Priority**: P1 · **Estimate**: 3
**Blocked By**: CHF-44

**Description**: Lists past months only (current hidden). Tap → confirm dialog warning of permanent data loss. On confirm → repository delete (CASCADE removes entries/expenses).

**Acceptance Criteria**:
- [ ] Current month never appears.
- [ ] Confirm dialog required.
- [ ] Cascade deletes all related entries/expenses.

**Maps to PRD**: US-E1, FR-S2.

---

## CHF-46 · Time Categories CRUD screen
**Type**: Story · **Priority**: P1 · **Estimate**: 5
**Blocked By**: CHF-44

**Description**: List of categories with add (FAB), rename (tap → edit dialog), delete (long-press → confirm; blocked if in use).

**Acceptance Criteria**:
- [ ] Add: name required, unique (case-insensitive), error inline.
- [ ] Rename: updates references via FK.
- [ ] Delete in-use → warning dialog with count.

**Maps to PRD**: US-D1, US-D2, FR-T1…FR-T3.

---

## CHF-47 · Theme selection (Light / Dark / Follow System)
**Type**: Story · **Priority**: P2 · **Estimate**: 2
**Blocked By**: CHF-44

**Description**: Radio group of three options. Selection persists via `LocalStorage`. Applied at app level via `ChaFundTheme(themeMode)`.

**Acceptance Criteria**:
- [ ] Selection survives cold start.
- [ ] System mode follows OS dark/light toggle live.

**Maps to PRD**: US-E2, FR-S4.

---

# EPIC-7 — Quality & Testing

---

## CHF-48 · Epic: Quality & Testing
**Type**: Epic · **Priority**: P1 · **Estimate**: —
**Blocked By**: CHF-27, CHF-34, CHF-42

**Goal**: Achieve coverage across unit, integration, UI, and migration tests. Enforce code quality tooling.

**Child Tickets**: CHF-49 … CHF-52.

---

## CHF-49 · Unit tests for all ViewModels
**Type**: Task · **Priority**: P1 · **Estimate**: 8
**Blocked By**: CHF-33, CHF-41, CHF-47

**Description**: Use Turbine + fake repositories. Cover validation, state transitions, event handling for: Home, AddEntry, AddExpense, DailyHistory, DayDetail, Edit{Entry,Expense}, MonthlyHistory, Settings, TimeCategories, DeleteMonth.

**Acceptance Criteria**:
- [ ] ≥ 80% line coverage on ViewModel layer.
- [ ] Each `UiEvent` has at least one test.

---

## CHF-50 · DAO + Repository integration tests (in-memory Room)
**Type**: Task · **Priority**: P1 · **Estimate**: 5
**Blocked By**: CHF-29, CHF-36, CHF-43

**Description**: `Room.inMemoryDatabaseBuilder`. Cover: `MonthManager` idempotency, `promoteToCurrent` transaction, cascade delete on Month, sum aggregations, category RESTRICT.

**Acceptance Criteria**:
- [ ] Each DAO has CRUD tests.
- [ ] Month-boundary scenario covered.

---

## CHF-51 · Compose UI tests for golden paths
**Type**: Task · **Priority**: P2 · **Estimate**: 5
**Blocked By**: CHF-33, CHF-41

**Description**: Semantics tests for: add entry → balance updates; add expense → spent updates; validation errors; navigate Home → Daily → Day Detail; theme switch.

**Acceptance Criteria**:
- [ ] All golden-path flows green on CI.

---

## CHF-52 · ktlint + detekt + Android Lint baseline + CI
**Type**: Task · **Priority**: P2 · **Estimate**: 3
**Blocked By**: CHF-2

**Description**: Add ktlint + detekt Gradle plugins. Configure detekt config. Run all three in CI (GitHub Actions or chosen). Lint baseline allowed only with reason.

**Acceptance Criteria**:
- [ ] `./gradlew lint detekt ktlintCheck` all pass on `main`.
- [ ] CI workflow file committed.

---

# EPIC-8 — Release Preparation

---

## CHF-53 · Epic: Release Preparation
**Type**: Epic · **Priority**: P1 · **Estimate**: —
**Blocked By**: CHF-48

**Goal**: Lock down privacy posture, accessibility, and release config. Final QA.

**Child Tickets**: CHF-54, CHF-55.

---

## CHF-54 · Manifest + R8 + accessibility hardening
**Type**: Task · **Priority**: P0 · **Estimate**: 3
**Blocked By**: CHF-47

**Description**: Verify no `INTERNET` permission. Set `android:allowBackup="false"`. R8 rules for Room/Koin/serialization. Audit accessibility: contentDescriptions, 48dp touch targets, font scaling up to 200%, color-not-only indicators.

**Acceptance Criteria**:
- [ ] Manifest declares zero permissions beyond defaults.
- [ ] Release APK builds and passes Lint accessibility checks.
- [ ] Manual screen reader pass through all top-level destinations.

---

## CHF-55 · Final QA pass (manual checklist)
**Type**: Task · **Priority**: P0 · **Estimate**: 3
**Blocked By**: CHF-49, CHF-50, CHF-51, CHF-52, CHF-54

**Description**: Execute manual QA per [PRD §15.4](./PRD.md):
- [ ] Airplane mode → app fully usable.
- [ ] Change device date to next month → on resume, new month at Tk 0.
- [ ] Negative balance renders red.
- [ ] Theme switch persists across cold start.
- [ ] Delete category in use → blocked with message.
- [ ] Past month entries cannot be edited.
- [ ] Cascade delete removes entries + expenses on month delete.
- [ ] Add/edit/delete in current month updates Home in ≤ 200 ms.

**Acceptance Criteria**:
- [ ] All checklist items pass.
- [ ] Release build signed and ready for distribution.

---

## Appendix A — Dependency Quick-Reference

| Ticket | Depends On |
|---|---|
| CHF-2 | — |
| CHF-3 | CHF-2 |
| CHF-4 | CHF-2 |
| CHF-5 | CHF-2, CHF-4 |
| CHF-6 | CHF-3, CHF-5 |
| CHF-8 | CHF-2 |
| CHF-9 | CHF-2 |
| CHF-10 | CHF-2 |
| CHF-11 | CHF-2 |
| CHF-12 | CHF-2 |
| CHF-13 | CHF-12 |
| CHF-14 | CHF-12 |
| CHF-15 | CHF-13 |
| CHF-16 | CHF-13, CHF-14 |
| CHF-17 | CHF-13, CHF-14, CHF-15, CHF-16 |
| CHF-18 | CHF-2 |
| CHF-19 | CHF-2 |
| CHF-20 | CHF-13, CHF-19, CHF-5 |
| CHF-21 | CHF-9, CHF-20 |
| CHF-23 | CHF-2 |
| CHF-24 | CHF-23 |
| CHF-25 | CHF-24 |
| CHF-26 | CHF-3, CHF-10 |
| CHF-28 | CHF-8 |
| CHF-29 | CHF-17, CHF-19, CHF-28 |
| CHF-30 | CHF-29 |
| CHF-31 | CHF-26, CHF-30 |
| CHF-32 | CHF-31 |
| CHF-33 | CHF-32 |
| CHF-35 | CHF-8 |
| CHF-36 | CHF-17, CHF-35 |
| CHF-37 | CHF-26, CHF-36 |
| CHF-38 | CHF-37 |
| CHF-39 | CHF-38 |
| CHF-40 | CHF-39 |
| CHF-41 | CHF-36, CHF-26 |
| CHF-43 | CHF-8, CHF-17, CHF-18 |
| CHF-44 | CHF-26, CHF-43 |
| CHF-45 | CHF-44 |
| CHF-46 | CHF-44 |
| CHF-47 | CHF-44 |
| CHF-49 | CHF-33, CHF-41, CHF-47 |
| CHF-50 | CHF-29, CHF-36, CHF-43 |
| CHF-51 | CHF-33, CHF-41 |
| CHF-52 | CHF-2 |
| CHF-54 | CHF-47 |
| CHF-55 | CHF-49, CHF-50, CHF-51, CHF-52, CHF-54 |

---

## Appendix B — Suggested Sprint Plan (2-week sprints, 1 engineer)

| Sprint | Goal | Tickets |
|---|---|---|
| Sprint 1 | Foundation usable | CHF-2 → CHF-6, CHF-52 |
| Sprint 2 | Data layer ready | CHF-8 → CHF-17 |
| Sprint 3 | Lifecycle + nav shell | CHF-18 → CHF-26 |
| Sprint 4 | Home + Add flows | CHF-28 → CHF-33 |
| Sprint 5 | Daily + Day Detail + Edit | CHF-35 → CHF-40 |
| Sprint 6 | Monthly + Settings | CHF-41, CHF-43 → CHF-47 |
| Sprint 7 | Testing + Release | CHF-49 → CHF-55 |

---

## Appendix C — Story Point Summary

| Epic | Total SP |
|---|---|
| EPIC-1 Foundation | 10 |
| EPIC-2 Core Infrastructure | 30 |
| EPIC-3 Navigation & Shared UI | 12 |
| EPIC-4 Fund Feature | 22 |
| EPIC-5 History Feature | 29 |
| EPIC-6 Settings Feature | 16 |
| EPIC-7 Quality & Testing | 21 |
| EPIC-8 Release Preparation | 6 |
| **Grand Total** | **146** |

---

*End of Engineering Tickets — Cha Fund v1.0*
