# ChaFund

A private, offline-first Android app for tracking a personal monthly money pool in Bangladeshi Taka (৳). Every calendar month starts at ৳0 — no carryover, no complexity, no internet required.

---

## Features

| Feature | Details |
|---|---|
| **Add entries** | Record money coming in with an optional ref note |
| **Add expenses** | Log spending categorised by time of day (Morning / Noon / Afternoon / Evening) |
| **Auto month management** | A new month is created automatically on first use after a calendar rollover — no manual closing |
| **Monthly history** | Scrollable list of all past months with balance and total spent |
| **Daily history** | Drill into any month to see a day-by-day breakdown |
| **Day detail** | View, edit, and delete individual entries and expenses per day |
| **Read-only past months** | Completed months are locked — no accidental edits |
| **Light / Dark / System theme** | Fully adapted colour schemes for both modes |
| **100 % offline** | No network, no account, no cloud — all data stays on device |

---

## Screens

```
Home ──────────────────► Daily History ──► Day Detail
 │                              ▲
 │                              │
Bottom Bar                      │
 ├─ Monthly History ────────────┘
 └─ Settings
```

- **Home** — running balance + total spent + add entry / add expense form
- **Monthly History** — month cards with balance, total spent, and entry count
- **Daily History** — per-day rows with entry delta and spend; tapping a row opens Day Detail
- **Day Detail** — entries (blue) and expenses grouped by time category (chip + rows); inline edit sheet and delete confirmation
- **Settings** — manage time categories and choose app theme

---

## Tech Stack

| Layer | Library / Tool | Version |
|---|---|---|
| Language | Kotlin | 2.0.21 |
| UI | Jetpack Compose (Material 3) | BOM 2024.09.00 |
| Navigation | Navigation Compose (type-safe routes) | 2.9.0 |
| Database | Room | 2.7.1 |
| Preferences | DataStore Preferences | 1.1.4 |
| DI | Koin | 4.0.4 |
| Async | Kotlin Coroutines + Flow | 1.10.2 |
| Serialization | kotlinx.serialization | 1.8.1 |
| Logging | Timber | 5.0.1 |
| Min SDK | Android 7.0 | API 24 |
| Target SDK | Android 16 | API 36 |

---

## Architecture

Clean Architecture with a feature-first package structure and MVVM presentation layer.

```
app/
└── feature/
│   ├── fund/          ← Home screen (entries + expenses)
│   ├── history/       ← Monthly / Daily / Day Detail screens
│   └── settings/      ← Theme + time categories
├── core/
│   ├── data/          ← Room DB, DAOs, entities, DataStore, Session
│   ├── domain/        ← Result type, errors, DispatcherProvider
│   ├── presentation/  ← Shared Compose components
│   └── utils/         ← Money, DateTimeFormat, MonthManager
├── navigation/        ← Type-safe routes, Navigator, AppNavHost
└── ui/theme/          ← ChaFundTheme, AppColors (CompositionLocal), typography
```

**Patterns used:**
- `ViewModel` + `StateFlow` for UI state
- `UiEvent` sealed interface for user actions (MVI-style)
- `Result<T, E>` for error handling without exceptions
- `Session` singleton (Koin) tracks the current active month ID
- `MonthManager` auto-creates a new month on calendar rollover
- `LocalAppColors` CompositionLocal switches semantic colours between light and dark mode

---

## Project Structure

```
ChaFund/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/chafund/
│   │   └── res/
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── docs/
│   ├── PRD.md
│   ├── BRD.md
│   ├── TECH_SPEC.md
│   ├── TICKETS.md
│   ├── QA_CHECKLIST.md
│   └── cha_fund_app_ui_spec.md
├── gradle/
│   └── libs.versions.toml
└── build.gradle.kts
```

---

## Getting Started

**Prerequisites**
- Android Studio Meerkat (2024.3) or later
- JDK 11+
- Android SDK with API 24–36

**Clone and run**

```bash
git clone https://github.com/Arittra101/ChaFund.git
cd ChaFund
```

Open in Android Studio → **Run** on a device or emulator running Android 7.0+.

**Build variants**

| Variant | Minify | Notes |
|---|---|---|
| `debug` | No | Fast builds, full logging |
| `release` | Yes (R8) | Signed with debug key by default |

---

## Key Design Decisions

**Currency precision** — all amounts are stored as `Long` paisa (1 Tk = 100 paisa) to avoid floating-point rounding. `Money` is a value class wrapping paisa.

**Month isolation** — each `MonthEntity` is a self-contained accounting period. Past months are marked read-only automatically when a new month is created.

**Type-safe navigation** — routes are `@Serializable` data objects/classes. `NavDestination.hasRoute(KClass)` is used for bottom-bar selection checks — safe under R8 obfuscation.

**Offline-only** — Room is the single source of truth. No network layer exists by design.

**Theme-aware colours** — `AppColors` properties are `@Composable` getters backed by `LocalAppColors` (a `CompositionLocal`). The theme switches the entire semantic colour scheme (fills, text, borders, chip palette) without touching any individual screen.

---

## Docs

Full product and engineering documentation is in the `docs/` folder:

- `PRD.md` — product requirements and user stories
- `BRD.md` — business requirements
- `TECH_SPEC.md` — technical specification
- `TICKETS.md` — implementation tickets
- `QA_CHECKLIST.md` — QA test cases
- `cha_fund_app_ui_spec.md` — UI design spec (colours, layout tokens, component rules)

---

## License

Private project. All rights reserved.
