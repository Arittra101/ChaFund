# High-Level Technical Specification
## Cha Fund — Personal Monthly Fund Tracker (Android)

| Field | Value |
|---|---|
| Document Title | Cha Fund High-Level Tech Spec |
| Version | 2.0 |
| Date | 2026-06-17 |
| Owner | Tech Lead (Cha Fund) |
| Status | Draft for Engineering Handoff |
| Related Docs | [BRD.md](./BRD.md) v1.0 · [PRD.md](./PRD.md) v1.0 |
| Platform | Android (Kotlin) |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 34 (Android 14) |
| UI Toolkit | Jetpack Compose + Material 3 |
| Architecture | Clean Architecture + MVVM |
| DI | **Koin** |
| Persistence | Room (SQLite) + DataStore (theme only) |
| Connectivity | **100% offline** — no `INTERNET` permission |

---

## 1. Purpose & Scope

This document defines the **high-level technical architecture, modules, data layer, DI wiring, navigation, and conventions** for Cha Fund. It adopts the team's standard Android Application Architecture verbatim, **drops the networking half** (no backend), and **swaps in a Room data layer** living in `core/data/database/`.

Class-level signatures are intentionally minimal — that belongs in LLD per feature.

---

## 2. Architecture Overview

**Pattern**: Clean Architecture + MVVM + Unidirectional Data Flow.

```
┌──────────────────────────────────────────────────────┐
│                  Presentation                        │
│  Compose (Material 3)                                │
│  ScreenRoot (stateful, hoists VM)                    │
│  Screen     (stateless, pure UI)                     │
└──────────────────────────────────────────────────────┘
                         ▲ collectAsStateWithLifecycle
                         ▼ onEvent(UiEvent)
┌──────────────────────────────────────────────────────┐
│                  ViewModel                           │
│  StateFlow<UiState> + sealed UiEvent                 │
│  No Room types — domain models only                  │
└──────────────────────────────────────────────────────┘
                         ▲ Flow / suspend
                         ▼ Result<Domain, DataError.Local>
┌──────────────────────────────────────────────────────┐
│              Domain + Repository (per feature)       │
│  feature/<name>/data/repository/  ← owns logic       │
│  feature/<name>/data/mapper/      ← entity→domain    │
│  feature/<name>/domain/           ← models, contracts│
└──────────────────────────────────────────────────────┘
                         ▲ Flow<Entity>
                         ▼ suspend insert/update/delete
┌──────────────────────────────────────────────────────┐
│              Core Infrastructure                     │
│  core/data/database/  (Room: DB, DAOs, Entities, TC) │
│  core/data/storage/   (LocalStorage on DataStore)    │
│  core/data/session/   (Session: currentMonthId)      │
│  core/utils/          (MonthManager, AppLogger, …)   │
└──────────────────────────────────────────────────────┘
```

### Principles
- **Single source of truth**: Room DB. UI never holds derived state — it collects DAO `Flow`s mapped to domain.
- **Repositories never throw** — they return `Result<DomainModel, DataError.Local>`.
- **Features don't import features** — cross-feature reactivity flows through `Session`.
- **Single Activity** hosts a single `NavHost`.
- **No `INTERNET` permission**, no networking code, no analytics SDKs.

---

## 3. What is Kept / Dropped / Added vs. the Standard Spec

### 3.1 Kept verbatim
- Kotlin + Compose + Material 3.
- Clean Architecture + MVVM.
- **Koin** DI (`singleOf`, `viewModelOf`, per-feature modules aggregated in `appModules()`).
- `feature/<name>/{di, data, domain, presentation}` layout.
- `ScreenRoot` (stateful) / `Screen` (stateless) split.
- Type-safe `@Serializable` `Route` + `Navigator` façade + `TopLevelDestination` bottom bar.
- `Session` for cross-feature reactive state.
- `LocalStorage` over DataStore (theme preference).
- `AppLogger` / Timber.
- Single Activity.
- Gradle **version catalog**.
- Flavors / build types.
- Per-feature Definition of Done.

### 3.2 Dropped (no backend)
- All networking: `HttpClientFactory`, `safeApiCall`, `DynamicTokenPlugin`, auth/refresh, `markAsNoAuth`, `networkModule`.
- Auth-guard `protectedRoutes` map.
- `LogoutEventManager`.
- `NetworkMonitor` / `NoInternetScreen`.
- `qa` base-URL plumbing.
- **Paging 3** — lists are bounded (handful of days per month).
- **DTO layer** — nothing is serialized over a wire.

### 3.3 Added / adapted
- **Room data layer** in `core/data/database/` (DB, DAOs, entities, `TypeConverter`s) exposed via a new `databaseModule`. Lives in `core` alongside `storage` and `session` so all features share one DB.
- **`Result<D, E>` kept**, but with `DataError.Local` (`NOT_FOUND`, `DISK_FULL`, `UNKNOWN`) replacing `DataError.Remote`.
- **Entity → domain mapper retained** (DTOs dropped). Mapping lives in `feature/<name>/data/mapper/` so `ViewModel` / `Screen` never see Room annotations.

---

## 4. Package Layout

```
app/src/main/kotlin/<root>/
├── App.kt                       # Koin start + lifecycle hook for MonthManager
├── MainActivity.kt              # Single Activity hosting AppNavHost
│
├── core/
│   ├── di/
│   │   ├── databaseModule.kt    # Room DB + DAOs
│   │   ├── storageModule.kt     # LocalStorage / DataStore
│   │   ├── sessionModule.kt     # Session singleton
│   │   ├── utilsModule.kt       # MonthManager, AppLogger, dispatchers
│   │   └── appModules.kt        # fun appModules() = listOf(...) aggregator
│   │
│   ├── data/
│   │   ├── database/
│   │   │   ├── ChaFundDb.kt             # @Database, version, callback
│   │   │   ├── entity/
│   │   │   │   ├── MonthEntity.kt
│   │   │   │   ├── TimeCategoryEntity.kt
│   │   │   │   ├── EntryEntity.kt
│   │   │   │   └── ExpenseEntity.kt
│   │   │   ├── dao/
│   │   │   │   ├── MonthDao.kt
│   │   │   │   ├── TimeCategoryDao.kt
│   │   │   │   ├── EntryDao.kt
│   │   │   │   └── ExpenseDao.kt
│   │   │   ├── converter/ChaFundTypeConverters.kt
│   │   │   ├── seed/SeedCallback.kt     # default Time Categories
│   │   │   └── migration/               # MIGRATION_x_y
│   │   ├── storage/
│   │   │   ├── LocalStorage.kt          # interface
│   │   │   └── DataStoreLocalStorage.kt # impl (theme only)
│   │   └── session/Session.kt
│   │
│   ├── domain/
│   │   ├── Result.kt
│   │   ├── DataError.kt                 # sealed Local { NOT_FOUND, DISK_FULL, UNKNOWN }
│   │   └── DispatcherProvider.kt
│   │
│   ├── presentation/components/
│   │   ├── PrimaryButton.kt
│   │   ├── ConfirmationBottomSheet.kt
│   │   ├── EmptyView.kt
│   │   ├── PillTag.kt
│   │   ├── MoneyText.kt                 # Tk formatting + negative red
│   │   ├── LockedMonthBadge.kt
│   │   └── SummaryCard.kt               # Balance + Spent
│   │
│   └── utils/
│       ├── AppLogger.kt                 # Timber wrapper
│       ├── DateTimeFormat.kt            # dd MMMM yy, HH:mm helpers
│       ├── Money.kt                     # paisa <-> display
│       └── MonthManager.kt              # detect + upsert + publish to Session
│
├── navigation/
│   ├── Route.kt                         # sealed @Serializable
│   ├── AppNavHost.kt
│   ├── Navigator.kt                     # façade
│   └── TopLevelDestination.kt           # Home, Daily, Months, Settings
│
├── theme/
│   ├── ChaFundTheme.kt
│   ├── Color.kt
│   └── Type.kt
│
├── bottombar/ChaFundBottomBar.kt
│
└── feature/
    ├── fund/        # Home: add entry, add expense, live Balance + Spent
    │   ├── di/fundModule.kt
    │   ├── data/
    │   │   ├── mapper/                  # *Entity ↔ domain
    │   │   └── repository/FundRepositoryImpl.kt
    │   ├── domain/
    │   │   ├── model/                   # Entry, Expense, MonthSummary
    │   │   └── FundRepository.kt        # interface
    │   └── presentation/
    │       ├── home/
    │       │   ├── HomeScreenRoot.kt
    │       │   ├── HomeScreen.kt
    │       │   ├── HomeViewModel.kt
    │       │   ├── HomeUiState.kt
    │       │   └── HomeUiEvent.kt
    │       ├── addentry/                # AddEntrySheet + VM
    │       └── addexpense/              # AddExpenseSheet + VM
    │
    ├── history/     # Daily list, Day detail, Monthly list, Edit flow
    │   ├── di/historyModule.kt
    │   ├── data/{mapper, repository}/
    │   ├── domain/{model, HistoryRepository.kt}/
    │   └── presentation/
    │       ├── daily/                   # DailyHistoryScreenRoot/Screen/VM
    │       ├── daydetail/               # DayDetailScreenRoot/Screen/VM + edit sheets
    │       └── monthly/                 # MonthlyHistoryScreenRoot/Screen/VM
    │
    └── settings/    # Delete month, Time categories, Theme
        ├── di/settingsModule.kt
        ├── data/{mapper, repository}/
        ├── domain/{model, SettingsRepository.kt}/
        └── presentation/
            ├── settings/                # SettingsScreenRoot/Screen/VM
            ├── categories/              # TimeCategoriesScreenRoot/Screen/VM
            └── deletemonth/             # DeleteMonthScreenRoot/Screen/VM
```

---

## 5. Tech Stack & Dependencies

| Concern | Choice | Notes |
|---|---|---|
| Language | Kotlin 1.9+ | |
| UI | Jetpack Compose + Material 3 | |
| Architecture | Clean Architecture + MVVM | |
| **DI** | **Koin** | `singleOf`, `viewModelOf`, modular |
| Persistence | Room + KSP | DAOs return `Flow` |
| Preferences | DataStore (Preferences) | Wrapped by `LocalStorage` |
| Navigation | Navigation Compose (typed) | `@Serializable` `Route` |
| Coroutines | kotlinx.coroutines | |
| Lifecycle | androidx.lifecycle.compose | `collectAsStateWithLifecycle` |
| Logging | **Timber** via `AppLogger` | Debug only |
| Date/Time | `java.time` + desugaring | min SDK 24 |
| Serialization | kotlinx.serialization | for `Route` types |
| Testing | JUnit4, MockK, Turbine, Compose UI Test | |

### Explicitly excluded
- ❌ Retrofit / OkHttp / Ktor — no networking
- ❌ Hilt — Koin is the team standard
- ❌ Firebase, Crashlytics, Analytics
- ❌ Paging 3
- ❌ Glide / Coil
- ❌ WorkManager

### Java 8+ desugaring
Enable `coreLibraryDesugaringEnabled = true` + `coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:...")` for `java.time` on min SDK 24.

---

## 6. Data Layer (Room)

### 6.1 Database
- **Class**: `ChaFundDb : RoomDatabase()`
- **Name**: `chafund.db`
- **Version**: 1
- **Schema export**: enabled (`room.schemaLocation`) for migration tests
- **Callback**: `SeedCallback` populates default `TimeCategoryEntity` rows on first creation
- **Migrations**: explicit `Migration` objects; **no `fallbackToDestructiveMigration()` in release**

### 6.2 Entities
> IDs are `Long` autogenerated (Room-friendly). PRD §6.2 mentioned UUID; we standardize on `Long` PKs for indexing/perf. Domain models can still use a typed wrapper if desired.

**MonthEntity**
| Column | Type | Notes |
|---|---|---|
| id | Long PK auto | |
| year | Int | |
| month | Int | 1–12 |
| label | String | "June 2026" |
| isCurrent | Boolean | exactly one true |
| createdAt | Long | epoch millis |

Index: `UNIQUE(year, month)`; `INDEX(isCurrent)`.

**TimeCategoryEntity** (global, no `monthId` FK)
| Column | Type | Notes |
|---|---|---|
| id | Long PK auto | |
| name | String | unique, COLLATE NOCASE |
| sortOrder | Int | |
| createdAt | Long | |

**EntryEntity**
| Column | Type | Notes |
|---|---|---|
| id | Long PK auto | |
| monthId | Long FK → Month, **ON DELETE CASCADE** | |
| amountPaisa | Long | Tk × 100 |
| ref | String? | optional |
| date | Long | epoch-day or millis |
| time | String | "HH:mm" |
| createdAt | Long | |
| updatedAt | Long | |

Indices: `INDEX(monthId)`, `INDEX(date)`.

**ExpenseEntity**
| Column | Type | Notes |
|---|---|---|
| id | Long PK auto | |
| monthId | Long FK → Month, **ON DELETE CASCADE** | |
| timeCategoryId | Long FK → TimeCategory, **ON DELETE RESTRICT** | |
| amountPaisa | Long | Tk × 100 |
| ref | String? | optional |
| date | Long | |
| time | String | |
| createdAt | Long | |
| updatedAt | Long | |

Indices: `INDEX(monthId)`, `INDEX(date)`, `INDEX(timeCategoryId)`.

> `day` (e.g. "Tuesday") is **not** stored — it is derived from `date` at display time via `DateTimeFormat`.

### 6.3 Money representation
- Stored as `Long amountPaisa` (Tk × 100) to avoid floating-point drift.
- `Money` value class in `core/utils/Money.kt` wraps `Long` paisa; `formatTk()` → `"Tk 1,250.50"`.

### 6.4 Date storage
- `date` stored as `Long` (epoch-day preferred; cheap range queries).
- `time` stored as `String` `"HH:mm"`.
- `createdAt` / `updatedAt`: `Long` epoch millis.
- `TypeConverter`s in `core/data/database/converter/`.

### 6.5 DAO surface (per the standard — all reads return `Flow`)

**MonthDao**
- `observeCurrent(): Flow<MonthEntity?>`
- `findByYearMonth(year: Int, month: Int): MonthEntity?`
- `upsertByYearMonth(...): Long` — `@Transaction`, idempotent on `(year, month)`
- `promoteToCurrent(id: Long)` — `@Transaction`: unflag all, then flag this
- `observeAll(): Flow<List<MonthEntity>>`
- `observePast(): Flow<List<MonthEntity>>` — excludes current
- `deletePastById(id: Long): Int` — guarded query: `WHERE id = :id AND isCurrent = 0`
- `observeMonthSummaries(): Flow<List<MonthSummaryProjection>>` — SQL aggregate

**EntryDao**
- `observeByMonth(monthId): Flow<List<EntryEntity>>`
- `observeByDate(monthId, date): Flow<List<EntryEntity>>`
- `sumByMonth(monthId): Flow<Long>`
- `insert / update / deleteById`

**ExpenseDao**
- `observeByMonth(monthId): Flow<List<ExpenseEntity>>`
- `observeByDate(monthId, date): Flow<List<ExpenseWithCategory>>` (`@Relation` or `JOIN`)
- `sumByMonth(monthId): Flow<Long>`
- `countByCategory(catId): Int`
- `insert / update / deleteById`

**TimeCategoryDao**
- `observeAll(): Flow<List<TimeCategoryEntity>>`
- `insert / rename / deleteById`

### 6.6 Aggregations — SQL, not in-memory

```sql
-- MonthSummary projection
SELECT m.id AS monthId,
       m.year, m.month, m.label, m.isCurrent,
       IFNULL((SELECT SUM(amountPaisa) FROM EntryEntity   WHERE monthId = m.id), 0) AS totalEntries,
       IFNULL((SELECT SUM(amountPaisa) FROM ExpenseEntity WHERE monthId = m.id), 0) AS totalSpent
FROM MonthEntity m
ORDER BY m.year DESC, m.month DESC;

-- DailySummary for a month
SELECT date,
       IFNULL((SELECT SUM(amountPaisa) FROM ExpenseEntity e2 WHERE e2.monthId = :monthId AND e2.date = x.date), 0) AS totalSpentForDay,
       IFNULL((SELECT SUM(amountPaisa) FROM EntryEntity   e3 WHERE e3.monthId = :monthId AND e3.date <= x.date), 0)
     - IFNULL((SELECT SUM(amountPaisa) FROM ExpenseEntity e4 WHERE e4.monthId = :monthId AND e4.date <= x.date), 0) AS balanceAtThatPoint
FROM (
  SELECT date FROM EntryEntity   WHERE monthId = :monthId
  UNION
  SELECT date FROM ExpenseEntity WHERE monthId = :monthId
) x
GROUP BY date
ORDER BY date DESC;
```

`balance` in domain is computed as `totalEntries − totalSpent` after projection load.

### 6.7 Seed data — `SeedCallback`
On first DB creation, insert default `TimeCategoryEntity` rows: `Morning`, `Noon`, `Afternoon`, `Evening` (sortOrder 1–4). Implemented via `RoomDatabase.Callback.onCreate`.

---

## 7. Result & Error Model

```kotlin
sealed interface Result<out D, out E : RootError> {
    data class Success<out D>(val data: D) : Result<D, Nothing>
    data class Error<out E : RootError>(val error: E) : Result<Nothing, E>
}

sealed interface RootError
sealed interface DataError : RootError {
    enum class Local : DataError { NOT_FOUND, DISK_FULL, UNKNOWN }
}
```

- **Repositories never throw**. Caught Room exceptions are mapped to `DataError.Local`.
- **ViewModels never see Room types** — only domain models + `Result`.
- `DataError.Remote` does **not** exist in this app — no remote layer.

---

## 8. DI Wiring (Koin)

### 8.1 Module aggregation
```kotlin
// core/di/appModules.kt
fun appModules() = listOf(
    databaseModule,
    storageModule,
    sessionModule,
    utilsModule,
    fundModule,
    historyModule,
    settingsModule,
)
```

### 8.2 `databaseModule` (sketch)
```kotlin
val databaseModule = module {
    single { Room.databaseBuilder(get(), ChaFundDb::class.java, "chafund.db")
        .addCallback(SeedCallback(get()))
        .addMigrations(MIGRATION_1_2 /* future */)
        .build() }
    single { get<ChaFundDb>().monthDao() }
    single { get<ChaFundDb>().timeCategoryDao() }
    single { get<ChaFundDb>().entryDao() }
    single { get<ChaFundDb>().expenseDao() }
}
```

### 8.3 Feature modules
```kotlin
// feature/fund/di/fundModule.kt
val fundModule = module {
    singleOf(::FundRepositoryImpl) bind FundRepository::class
    viewModelOf(::HomeViewModel)
    viewModelOf(::AddEntryViewModel)
    viewModelOf(::AddExpenseViewModel)
}
```

Same pattern for `historyModule`, `settingsModule`.

### 8.4 App bootstrap
```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(appModules())
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(get<MonthManager>())
        AppLogger.init()
    }
}
```

---

## 9. Cross-Feature State — `Session` & `MonthManager`

### 9.1 `Session` (core/data/session/Session.kt)
Owns reactive app-wide state. Features observe; features do **not** import each other.

```kotlin
class Session {
    private val _currentMonthId = MutableStateFlow<Long>(0L)
    val currentMonthId: StateFlow<Long> = _currentMonthId.asStateFlow()

    private val _monthChanged = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val monthChanged: SharedFlow<Unit> = _monthChanged.asSharedFlow()

    fun setCurrentMonth(id: Long) {
        if (_currentMonthId.value != id) {
            _currentMonthId.value = id
            _monthChanged.tryEmit(Unit)
        }
    }
}
```

### 9.2 `MonthManager` (core/utils/MonthManager.kt)
Single owner of the month-lifecycle rules from PRD §3.5.

Runs at:
- **App start** — called from `App.onCreate` (or first frame).
- **`ON_RESUME`** — observes `ProcessLifecycleOwner` to catch midnight / month-boundary crossings (FR-L7).

```kotlin
class MonthManager(
    private val monthDao: MonthDao,
    private val session: Session,
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
) : LifecycleEventObserver {

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_RESUME) detectAndPromote()
    }

    fun detectAndPromote() = scope.launch(dispatcher) {
        val today = LocalDate.now()
        val existing = monthDao.findByYearMonth(today.year, today.monthValue)
        val id = existing?.id ?: monthDao.upsertByYearMonth(
            year = today.year, month = today.monthValue,
            label = formatLabel(today), createdAt = nowMs()
        )
        monthDao.promoteToCurrent(id)   // @Transaction: unflag-all + flag-this
        session.setCurrentMonth(id)
    }
}
```

- Idempotent via `UNIQUE(year, month)`.
- `promoteToCurrent` is `@Transaction` — never leaves DB in a "two-current" state.
- Never owned by a feature.

---

## 10. Navigation

### 10.1 Routes — type-safe, no auth guard
```kotlin
sealed interface Route {
    @Serializable data object Home          : Route
    @Serializable data class  DailyHistory(val monthId: Long) : Route
    @Serializable data class  DayDetail(val monthId: Long, val dateEpoch: Long) : Route
    @Serializable data object MonthlyHistory: Route
    @Serializable data object Settings      : Route
}
```

### 10.2 `Navigator` façade
```kotlin
interface Navigator {
    val navEvents: SharedFlow<NavEvent>
    fun navigateTo(route: Route)
    fun navigateToTopLevel(dest: TopLevelDestination)
    fun navigateBack()
}
```
- No `protectedRoutes` map.
- No `onAuthRequired`.

### 10.3 `TopLevelDestination`
Bottom-bar order: **Home · Daily · Months · Settings**.

### 10.4 `AppNavHost`
Single `NavHost` rooted in `MainActivity`; deep links: none in v1.

---

## 11. Presentation — `ScreenRoot` / `Screen` Split

Per the team standard, every screen has a stateful root and a stateless body.

```kotlin
@Composable
fun HomeScreenRoot(
    vm: HomeViewModel = koinViewModel(),
    onNavigate: (Route) -> Unit,
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        onEvent = vm::onEvent,
        onNavigate = onNavigate,
    )
}

@Composable
fun HomeScreen(
    state: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
    onNavigate: (Route) -> Unit,
) { /* pure UI */ }
```

### Conventions
- `XyzUiState` — immutable data class, `@Immutable`.
- `XyzUiEvent` — `sealed interface`.
- `fun onEvent(event: XyzUiEvent)` — single entry point.
- ViewModels expose `StateFlow<XyzUiState>` via `viewModelScope`.
- Side-effects (snackbars, navigation) emitted as one-shot `SharedFlow<UiEffect>` (optional).

---

## 12. Feature Specs

### 12.1 `fund` — Home + add flows
**Owns**: `FundRepository` (insert entry, insert expense, observe current `MonthSummary`, observe categories for picker).

**Screens**:
- `HomeScreenRoot`/`HomeScreen` — locked month badge, `SummaryCard(balance, spent)`, two action buttons.
- `AddEntrySheet` — amount + optional ref.
- `AddExpenseSheet` — amount + Time Category dropdown + optional ref.

**State source**:
- `session.currentMonthId` drives all queries.
- `combine(monthSummaryFlow, currentMonthFlow)` → `HomeUiState`.

### 12.2 `history` — Daily, Day Detail, Monthly + edits
**Owns**: `HistoryRepository` (observe `DailySummary` list, observe day records, edit/delete entry, edit/delete expense, observe `MonthSummary` list).

**Screens**:
- `DailyHistoryScreenRoot`/`Screen` — top bar `Spent | Balance`, date list desc.
- `DayDetailScreenRoot`/`Screen` — entries section + expenses grouped by Time Category.
- `MonthlyHistoryScreenRoot`/`Screen` — month list with totals.
- Edit sheets: `EditEntrySheet`, `EditExpenseSheet` (inline per Q3 default).

**Read-only mode**: past months hide edit/delete affordances.

### 12.3 `settings` — Delete month, categories, theme
**Owns**: `SettingsRepository` (observe past months, delete past month with current-month guard, manage Time Categories, read/write theme via `LocalStorage`).

**Screens**:
- `SettingsScreenRoot`/`Screen` — locked current month row + entry points.
- `DeleteMonthScreenRoot`/`Screen` — list past months, confirm dialog.
- `TimeCategoriesScreenRoot`/`Screen` — add/rename/delete with in-use guard.

---

## 13. Lifecycle Hooks

| Hook | What runs |
|---|---|
| `App.onCreate` | `startKoin`, register `MonthManager` as `ProcessLifecycleOwner` observer, init `AppLogger`. |
| `ProcessLifecycleOwner` `ON_RESUME` | `MonthManager.detectAndPromote()`. |
| Room `Callback.onCreate` | `SeedCallback` inserts default Time Categories. |
| `MainActivity.onCreate` | Sets Compose content with `ChaFundTheme(themeMode)`. |

---

## 14. Theme Pipeline

```
PreferencesRepository.themeFlow (LocalStorage on DataStore)
        ↓
AppThemeViewModel.themeMode : StateFlow<ThemeMode>
        ↓
ChaFundTheme(themeMode) { AppNavHost(...) }
```

`ThemeMode = LIGHT | DARK | SYSTEM`. Stored under key `theme_mode`. Default: `SYSTEM`.

---

## 15. Validation & Edge Cases (engineering view)

| Case | Layer | Behavior |
|---|---|---|
| Amount ≤ 0 / empty | ViewModel | `amountError` field in `UiState`; Save button disabled. |
| Missing Time Category on expense | ViewModel | `categoryError` field; Save disabled. |
| Delete current month | Repository + DAO | DAO query `WHERE id = :id AND isCurrent = 0`; repo returns `DataError.Local.NOT_FOUND` if no row matched. |
| Delete category referenced by expense | Repository | Pre-check `countByCategory`; warn or return error. |
| Negative balance | UI | `MoneyText` renders in `colorScheme.error` with explicit `-` sign. |
| App resumed across month boundary | `MonthManager` | `ON_RESUME` re-runs detection, promotes new month, `Session` notifies subscribers. |
| Device clock manipulation | `MonthManager` | Idempotent upsert by `(year, month)` — no duplicate rows; behavior documented. |

---

## 16. Performance Strategy

| Target | Strategy |
|---|---|
| Home render ≤ 300 ms (12 mo data) | SQL aggregation, indexed FKs, `LazyColumn`, stable keys, `@Immutable` UI models. |
| Add Entry → Balance update ≤ 200 ms | `Flow`-driven recomposition; no manual reload. |
| Cold start ≤ 1.5 s | Heavy work off `App.onCreate`; `MonthManager` runs async. |
| Smooth scroll | Compose stability, `derivedStateOf` for computed fields. |

---

## 17. Privacy & Security

| Concern | Posture |
|---|---|
| Network | No `INTERNET` permission in `AndroidManifest.xml`. |
| Backup | `android:allowBackup="false"` (documented in BRD R-6). |
| Logs | `AppLogger` debug-only; release builds strip via R8/ProGuard. |
| At-rest encryption | Out of scope v1; SQLCipher noted for v2. |
| Telemetry | None. |

---

## 18. Build & Tooling

- Kotlin DSL (`build.gradle.kts`) already in repo.
- **Version catalog** (`libs.versions.toml`) for all dependencies.
- **KSP** for Room.
- ktlint + detekt + Android Lint (baseline allowed with reason).
- Flavors / build types per standard spec.

---

## 19. Testing Strategy

### 19.1 Pyramid
- **Unit** — ViewModel state transitions (Turbine), `Money`, `DateTimeFormat`, validators, repository mappers.
- **Integration** — Room DAO + `@Transaction` correctness, `MonthManager.detectAndPromote` idempotency, cascade delete.
- **UI** — Compose semantics: add flow, validation, navigation, read-only past months.
- **Migration** — `MigrationTestHelper` per schema bump.

### 19.2 Test doubles
- Fake repositories for ViewModel tests; MockK for collaborator-heavy cases.
- In-memory Room (`Room.inMemoryDatabaseBuilder`) for DAO tests.

### 19.3 Definition of Done (per feature)
1. Repository returns `Result<Domain, DataError.Local>` — no throws.
2. ViewModel exposes single `StateFlow<UiState>` + `onEvent`.
3. `ScreenRoot` / `Screen` split honoured.
4. Koin module registered in `appModules()`.
5. No Room types leak past the repository.
6. Unit + UI tests added.

---

## 20. Open Technical Questions

| # | Question | Recommendation |
|---|---|---|
| TQ-1 | PK type — `Long` (Room-friendly) vs `UUID` (PRD) | **`Long` autogenerated** — better indexing/perf. Domain layer wraps as needed. |
| TQ-2 | Single-module vs feature `:feature-*` modules | **Single-module** v1; split when build time hurts. |
| TQ-3 | KSP vs kapt | **KSP** for Room. |
| TQ-4 | Compose Material 3 dynamic color | **Disabled** — consistent brand. |
| TQ-5 | Inline edit vs separate edit screen | **Inline bottom sheet** (PRD §16 Q-4 confirmed). |
| TQ-6 | `allowBackup=false` | **Yes** — explicit privacy. |
| TQ-7 | Entries tagged to Time Category? | **No** — per PRD §16 Q-2. |
| TQ-8 | Expenses have optional `ref`? | **Yes** — per PRD §16 Q-3. |

---

## 21. Risks & Mitigations (Engineering)

| # | Risk | Mitigation |
|---|---|---|
| TR-1 | Float drift on currency | `Long amountPaisa` + `Money` value class. |
| TR-2 | Two months flagged `isCurrent` | `promoteToCurrent` is a single `@Transaction`. |
| TR-3 | Missed month detection across midnight | `ProcessLifecycleOwner` `ON_RESUME` observer in `MonthManager`. |
| TR-4 | Schema migration data loss | Explicit `Migration` objects + migration tests; **no `fallbackToDestructiveMigration` in release**. |
| TR-5 | Recomposition storms | `@Immutable` UI models, `derivedStateOf`, stable keys. |
| TR-6 | Feature coupling regressions | Lint rule / review gate: `feature/<a>` may not import `feature/<b>`. |
| TR-7 | DB grows unbounded | v1: user-driven month deletion. v2: optional auto-prune. |

---

## 22. Roadmap Beyond v1.0

| Phase | Items |
|---|---|
| v1.1 | Recent activity card, CSV export via SAF, in-app search. |
| v1.2 | App widget (current Balance), quick-tile add expense. |
| v2.0 | Optional SQLCipher encryption, multiple funds/pools, charts. |
| v2.x | User-controlled encrypted export/import (no cloud account). |

---

## 23. Approval

| Role | Name | Signature | Date |
|---|---|---|---|
| Tech Lead | | | |
| Senior Android Engineer | | | |
| QA Lead | | | |
| Product Owner | | | |

---

*End of High-Level Technical Specification — Cha Fund v2.0*
