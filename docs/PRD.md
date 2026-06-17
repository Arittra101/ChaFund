# Product Requirements Document (PRD)
## Cha Fund — Personal Monthly Fund Tracker (Android)

| Field | Value |
|---|---|
| Document Title | Cha Fund PRD |
| Version | 1.0 |
| Date | 2026-06-17 |
| Owner | Product Owner (Cha Fund) |
| Status | Draft for Engineering Handoff |
| Related Doc | [BRD.md](./BRD.md) v1.0 |
| Platform | Android (Kotlin, Jetpack Compose) |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 34 (Android 14) |
| Currency | Bangladeshi Taka (Tk) |
| Connectivity | 100% Offline |

---

## 1. Product Overview

### 1.1 Vision
Cha Fund is a private, offline-first Android app for tracking a personal monthly money pool. The user adds money (entries) and records spending (expenses) within an isolated calendar-month accounting period. Every month starts at Tk 0 — no carryover, no complexity.

### 1.2 Target User
A single individual who wants to track personal cash flow within a month without using spreadsheets, cloud-synced finance apps, or signing up for any service.

### 1.3 Value Proposition
- **Frictionless** — add an entry or expense in ≤ 3 taps.
- **Private** — no internet, no cloud, no account.
- **Automatic** — months self-manage; no manual closing.
- **Reviewable** — past months remain as read-only history.

### 1.4 Success Metrics
| Metric | Target |
|---|---|
| Time to first entry from cold start | ≤ 5 seconds |
| Taps to add entry / expense | ≤ 3 |
| Home screen render time (12 months data) | ≤ 300 ms |
| Crash-free sessions | ≥ 99.5% |
| User retention (self-reported, manual) | Daily use over 30 days |

---

## 2. User Personas

### Persona 1 — "Rafiq, the Daily Saver"
- Age 28, office worker.
- Wants to know how much pocket money is left for the rest of the month.
- Uses the app several times a day to log small expenses (tea, transport, lunch).
- Cares about: speed of entry, current Balance visibility.

### Persona 2 — "Lina, the Monthly Reviewer"
- Age 35, freelancer.
- Logs less frequently but reviews past months to spot spending patterns.
- Cares about: clarity of monthly history, ability to edit miskeyed entries.

---

## 3. User Stories & Acceptance Criteria

### Epic A — Quick Entry

**US-A1**: As a user, I want to add money to my fund so my Balance increases.
- **AC-A1.1**: Tapping "Add Entry" from Home opens an input with amount field focused.
- **AC-A1.2**: Amount must be > 0; ref note is optional.
- **AC-A1.3**: On save, Home Balance increases by the entered amount within 200ms.
- **AC-A1.4**: Date, day, and time are captured automatically.

**US-A2**: As a user, I want to log an expense tagged by time-of-day so I can review when I spend.
- **AC-A2.1**: Tapping "Add Expense" opens amount + Time Category picker.
- **AC-A2.2**: Time Category is required; amount > 0; ref optional.
- **AC-A2.3**: On save, Balance decreases and Spent increases immediately.
- **AC-A2.4**: Default categories (Morning, Noon, Afternoon, Evening) appear on first launch.

### Epic B — Month Lifecycle

**US-B1**: As a user, I want each calendar month to be its own clean slate so I don't need to manage rollovers.
- **AC-B1.1**: On app launch, the current calendar month is detected via `LocalDate.now()`.
- **AC-B1.2**: If no Month row exists for `(year, month)`, one is created with Balance = Tk 0.
- **AC-B1.3**: The newly-created or detected month is flagged `isCurrent = true`; the previous current month is unflagged.
- **AC-B1.4**: All new entries/expenses bind to the current month only.

**US-B2**: As a user, when I open the app on the first day of a new month, I see Tk 0 and last month is preserved.
- **AC-B2.1**: Previous month rows remain intact and visible under Monthly History.
- **AC-B2.2**: Previous months are read-only for data entry.

### Epic C — History & Review

**US-C1**: As a user, I want to see daily activity for the current month grouped by date.
- **AC-C1.1**: Daily History defaults to current month when opened from bottom nav.
- **AC-C1.2**: Only dates with activity appear; sort newest first.
- **AC-C1.3**: Each row shows: `dd MMMM yy`, day's expense total, running Balance at that day.
- **AC-C1.4**: Top bar shows month's Spent and Balance.

**US-C2**: As a user, I want to drill into a specific day to see and edit individual records.
- **AC-C2.1**: Tapping a date opens Day Detail.
- **AC-C2.2**: Expenses grouped under their Time Categories; entries listed separately.
- **AC-C2.3**: Each item has Edit and Delete affordances.
- **AC-C2.4**: Edits/deletes immediately recompute daily and monthly totals.

**US-C3**: As a user, I want to browse all my historical months.
- **AC-C3.1**: Monthly History lists all retained months newest-first.
- **AC-C3.2**: Each row shows total Entries, Spent, Balance.
- **AC-C3.3**: Tapping a past month opens its Daily History in read-only mode.

### Epic D — Time Category Management

**US-D1**: As a user, I want to add my own time categories to fit my routine.
- **AC-D1.1**: From Settings → Time Categories, the user can add a category with a name.
- **AC-D1.2**: Names must be unique and non-empty.

**US-D2**: As a user, I want to rename or delete categories without breaking history.
- **AC-D2.1**: Rename is allowed and reflects everywhere (since stored as FK).
- **AC-D2.2**: Delete is blocked if any expense references the category; a dialog explains why.

### Epic E — Settings & Theming

**US-E1**: As a user, I want to delete old months I no longer need.
- **AC-E1.1**: Settings → Delete Month lists only past months.
- **AC-E1.2**: Deletion requires a confirmation dialog.
- **AC-E1.3**: Cascade removes all entries and expenses for that month.

**US-E2**: As a user, I want my theme preference to stick.
- **AC-E2.1**: Light / Dark / Follow System options available.
- **AC-E2.2**: Selection persisted via DataStore and applied on next launch.

---

## 4. Feature Specifications

### 4.1 Home Screen
**Purpose**: One-glance status of the current month + quick add.

**Layout (top to bottom)**:
1. **Top App Bar**: Title "Cha Fund", optional settings icon.
2. **Current Month Badge**: e.g. `June 2026 🔒` — non-interactive.
3. **Summary Card**:
   - `Balance: Tk 1,250` (large, red if negative)
   - `Spent: Tk 850` (secondary)
4. **Action Row**: Two large buttons — **Add Entry**, **Add Expense**.
5. **Recent Activity** (optional v1.1): last 3–5 records.
6. **Bottom Navigation**: Home · Daily · Monthly · Settings.

**Interactions**:
- Add Entry → modal bottom sheet (amount, optional ref).
- Add Expense → modal bottom sheet (amount, Time Category dropdown, optional ref).
- Save → snackbar confirmation "Entry added" / "Expense added".

### 4.2 Add Entry Sheet
- Input: Amount (numeric, > 0, required).
- Input: Ref (text, optional, max 80 chars).
- Auto-captured (not shown for edit): date, day, time, monthId.
- Buttons: Cancel · Save.
- Save disabled until amount is valid.

### 4.3 Add Expense Sheet
- Input: Amount (numeric, > 0, required).
- Input: Time Category (dropdown, required).
- Input: Ref (text, optional, max 80 chars).
- Validation errors shown inline.

### 4.4 Daily History
- Top bar: month label + Spent + Balance.
- List: dates with activity (descending).
- Empty state: "No activity yet this month."
- Past month context: header shows read-only badge.

### 4.5 Day Detail
- Sections:
  - **Entries** — list of entry rows (amount, ref, time).
  - **Expenses** — grouped under Time Category headers; each row shows amount, ref, time.
- Row actions: tap → edit sheet; long-press → delete confirmation.
- Read-only mode (past months): edit/delete hidden.

### 4.6 Monthly History
- List of months (latest first).
- Row: `June 2026` · Entries Tk X · Spent Tk Y · Balance Tk Z.
- Tap → opens that month's Daily History.

### 4.7 Settings
| Section | Content |
|---|---|
| Current Month | Read-only row with lock icon. |
| Delete Past Month | List of past months with trash icon. |
| Time Categories | Add / rename / delete. |
| Theme | Light · Dark · Follow System. |
| About | Version, "100% offline" tagline. |

---

## 5. Information Architecture & Navigation

```
[Home] ─┬─ Add Entry (sheet)
        └─ Add Expense (sheet)

[Daily History] ── Day Detail ── Edit/Delete record

[Monthly History] ── Daily History (selected, read-only if past)

[Settings] ─┬─ Current Month (locked)
            ├─ Delete Past Month → confirm
            ├─ Time Categories → add/rename/delete
            └─ Theme
```

Bottom nav order: **Home · Daily · Monthly · Settings**.

---

## 6. Data Model & Storage

### 6.1 Tech Stack
- **Persistence**: Room (SQLite) with `Flow` for reactive UI.
- **Preferences**: Jetpack DataStore for theme.
- **UI**: Jetpack Compose + Material 3.
- **DI**: Hilt (recommended).
- **Architecture**: MVVM with `StateFlow` / `Flow` from DAOs.

### 6.2 Schema (Room Entities)

**Month**
| Column | Type | Notes |
|---|---|---|
| id | Long PK auto | |
| year | Int | e.g. 2026 |
| month | Int | 1–12 |
| label | String | e.g. "June 2026" |
| isCurrent | Boolean | exactly one true |
| createdAt | Long | epoch millis |

Unique index on `(year, month)`.

**TimeCategory**
| Column | Type | Notes |
|---|---|---|
| id | Long PK auto | |
| name | String | unique, non-empty |
| sortOrder | Int | display order |
| createdAt | Long | |

**Entry**
| Column | Type | Notes |
|---|---|---|
| id | Long PK auto | |
| monthId | Long FK → Month, ON DELETE CASCADE | |
| amount | Long (paisa) or Double (Tk) | > 0 |
| ref | String? | optional |
| date | String / Long | `yyyy-MM-dd` |
| day | String | e.g. "Wed" |
| time | String | `HH:mm` |
| createdAt | Long | |
| updatedAt | Long | |

**Expense**
| Column | Type | Notes |
|---|---|---|
| id | Long PK auto | |
| monthId | Long FK → Month, ON DELETE CASCADE | |
| timeCategoryId | Long FK → TimeCategory, ON DELETE RESTRICT | |
| amount | Long / Double | > 0 |
| ref | String? | optional |
| date | String / Long | |
| day | String | |
| time | String | |
| createdAt | Long | |
| updatedAt | Long | |

> **Note on amount storage**: prefer integer paisa (Tk × 100) to avoid floating-point drift, or use `BigDecimal` with TypeConverter.

### 6.3 Derived Queries
- `MonthSummary(monthId)`: aggregate Entries SUM, Expenses SUM, Balance.
- `DailySummary(monthId)`: per-date totals.
- `DayDetail(monthId, date)`: entries + expenses grouped by category.

All exposed as `Flow<...>` for reactive recomposition.

### 6.4 Seed Data
On first DB creation:
- TimeCategories: `Morning`, `Noon`, `Afternoon`, `Evening` (sortOrder 1–4).
- No Month row — created lazily on first `onResume`.

---

## 7. Key Algorithms

### 7.1 Current Month Detection (on `ON_RESUME` / `onCreate`)
```
val today = LocalDate.now()
val (y, m) = today.year to today.monthValue

val existing = monthDao.findByYearMonth(y, m)
if (existing == null) {
    monthDao.unflagAllCurrent()
    monthDao.insert(Month(year=y, month=m, label="...", isCurrent=true))
} else if (!existing.isCurrent) {
    monthDao.unflagAllCurrent()
    monthDao.setCurrent(existing.id)
}
```
- Wrap in `@Transaction`.
- Idempotent via unique index on `(year, month)`.

### 7.2 Negative Balance
- Allowed; render text in `MaterialTheme.colorScheme.error` when `balance < 0`.

### 7.3 Delete Time Category
```
val refCount = expenseDao.countByCategory(catId)
if (refCount > 0) { showWarnDialog(); return }
timeCategoryDao.delete(catId)
```

---

## 8. UI/UX Specifications

### 8.1 Visual Language
- **Material 3** with dynamic color disabled (consistent brand feel).
- Primary accent: TBD by design (suggest teal or deep green for "money" connotation).
- Negative balance color: `colorScheme.error`.

### 8.2 Typography
- Headline amounts: `displaySmall`.
- Secondary labels: `bodyMedium`.
- Currency always rendered as `Tk 1,250` (space after Tk, locale-grouped digits).

### 8.3 Date & Time Format
- Display date: `dd MMMM yy` → `16 June 26`.
- Display time: `HH:mm` → `14:30`.
- Internally store ISO-8601 for queryability.

### 8.4 Empty States
| Screen | Message |
|---|---|
| Home (no records) | "No entries yet this month. Add one to get started." |
| Daily History | "No activity yet this month." |
| Day Detail | "No records for this day." |
| Monthly History | "Only the current month so far." |

### 8.5 Accessibility
- Minimum touch target: 48dp.
- `contentDescription` for icon buttons.
- Color is never the sole indicator (negative balance also gets a `-` sign).
- Support font scaling up to 200%.

### 8.6 Theming
- Light, Dark, Follow System.
- Persisted via DataStore key `theme_mode`.
- Applied at `Application.onCreate` via `AppCompatDelegate.setDefaultNightMode(...)` or Compose `MaterialTheme(colorScheme = ...)`.

---

## 9. Non-Functional Requirements

Inherited from BRD §8. Engineering specifics:

| ID | Spec |
|---|---|
| NFR-1 | `AndroidManifest.xml` must NOT declare `<uses-permission android:name="android.permission.INTERNET"/>`. |
| NFR-2 | No Firebase, Crashlytics, Analytics, or any networked SDK. |
| NFR-3 | Use Room `Flow` + Compose `collectAsStateWithLifecycle` for reactivity. |
| NFR-5 | All multi-write operations wrapped in `@Transaction` DAOs. |
| NFR-6 | All FK relations declared with `onDelete = CASCADE` (Entry/Expense → Month). |
| NFR-9 | Register lifecycle observer; re-run `detectCurrentMonth()` on `ON_RESUME`. |
| NFR-10 | Implement explicit `Migration(from, to)` for any schema change. No `fallbackToDestructiveMigration()` in release. |

---

## 10. Validation Rules

| Field | Rule | Error Message |
|---|---|---|
| Entry/Expense amount | Numeric, > 0 | "Amount must be greater than 0" |
| Expense Time Category | Required | "Select a time category" |
| Ref | Optional, max 80 chars | "Ref must be 80 characters or fewer" |
| Time Category name | Non-empty, unique (case-insensitive) | "Category name must be unique" |
| Delete current month | Blocked at UI + repository | (no UI affordance) |
| Delete category in use | Blocked | "This category is used by N expenses" |

---

## 11. Edge Cases

| Case | Behavior |
|---|---|
| User changes device date backward to a prior month | Current month detection follows device clock; previous month becomes "current" again (acceptable per BRD R-1). |
| User edits an entry in a past (read-only) month | Not possible — edit affordances hidden. |
| Two devices used by same user | Out of scope (single-device product). |
| Database corruption | Room throws; app shows generic error; user must reinstall (no backup in v1). |
| Time Category deleted while expense sheet is open | On save, validate category still exists; show error if not. |
| Amount input with locale-specific separators (e.g. `1,250`) | Strip non-digits before parsing; reject if not a positive integer/decimal. |

---

## 12. Analytics & Telemetry
**None.** No data leaves the device (BRD NFR-2).

---

## 13. Release Plan

### v1.0 (MVP — this PRD)
All in-scope items from BRD §3.1.

### v1.1 (post-launch ideas — NOT this release)
- Recent activity card on Home.
- CSV export to local file (Storage Access Framework).
- Search across records.
- Widgets / quick-tile entry.

### v2.0 (long-term)
- Optional encrypted local backup.
- Multiple funds / pools.
- Charts.

---

## 14. Dependencies

| Library | Purpose | Version (suggested) |
|---|---|---|
| `androidx.room:room-runtime` + `room-ktx` + `room-compiler` | Persistence | latest stable |
| `androidx.datastore:datastore-preferences` | Theme pref | latest stable |
| `androidx.compose.*` | UI | BOM latest stable |
| `androidx.lifecycle:lifecycle-runtime-compose` | `collectAsStateWithLifecycle` | latest stable |
| `androidx.navigation:navigation-compose` | Nav graph | latest stable |
| `com.google.dagger:hilt-android` | DI | latest stable |
| `androidx.hilt:hilt-navigation-compose` | DI in Compose | latest stable |

No networking, no analytics, no Firebase.

---

## 15. Testing Strategy

### 15.1 Unit Tests
- Repository: month-detection idempotency, current-month flagging.
- ViewModel: balance/spent recomputation on add/edit/delete.
- Validators: amount > 0, required category, ref length.

### 15.2 Instrumentation Tests (Room)
- DAO insert/update/delete with cascade.
- `MonthSummary` aggregation correctness.
- Unique index on `(year, month)` prevents duplicates.

### 15.3 UI Tests (Compose)
- Add Entry flow: amount input → save → Balance updates.
- Add Expense flow: category required validation.
- Month boundary: simulate clock change, verify new month created.
- Delete past month: confirmation gates the action.

### 15.4 Manual QA Checklist
- [ ] Enable airplane mode → app fully usable.
- [ ] Change device date to next month → on resume, new month at Tk 0.
- [ ] Negative balance renders red.
- [ ] Theme switch persists across cold start.
- [ ] Delete category in use → blocked with message.
- [ ] Past month entries cannot be edited.

---

## 16. Open Decisions (Tracked from BRD §16)

| # | Decision | Status |
|---|---|---|
| Q-1 | Time Categories global | **Confirmed** — implement as global table. |
| Q-2 | Entries not tagged to category | **Confirmed**. |
| Q-3 | Expenses have optional ref | **Confirmed**. |
| Q-4 | Inline edit on Day Detail | **Confirmed** for v1 (bottom sheet, not full-screen). |
| Q-5 | "Follow System" theme | **Confirmed** — third theme option. |

---

## 17. Risks (Engineering View)

| # | Risk | Mitigation |
|---|---|---|
| ER-1 | Floating-point drift on currency | Store amount as integer paisa or use `BigDecimal`. |
| ER-2 | Forgetting to re-run month detection | Bind detection to `ProcessLifecycleOwner` `ON_RESUME`. |
| ER-3 | Schema migration mistakes | Write a Room migration test for every version bump. |
| ER-4 | UI lag on large months | Use paging or `LazyColumn` with stable keys; aggregate via SQL not in-memory. |
| ER-5 | Compose recomposition storms | Hoist state to ViewModel; use `derivedStateOf` for computed UI fields. |

---

## 18. Glossary
Inherited from BRD §5.

---

## 19. Approval

| Role | Name | Signature | Date |
|---|---|---|---|
| Product Owner | | | |
| Tech Lead | | | |
| Design Lead | | | |
| QA Lead | | | |

---

*End of Product Requirements Document — Cha Fund v1.0*
