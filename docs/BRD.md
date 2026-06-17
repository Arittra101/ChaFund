# Business Requirements Document (BRD)
## Cha Fund — Personal Monthly Fund Tracker (Android)

| Field | Value |
|---|---|
| Document Title | Cha Fund BRD |
| Version | 1.0 |
| Date | 2026-06-17 |
| Owner | Product Owner (Cha Fund) |
| Status | Draft for Approval |
| Platform | Android (Kotlin) |
| Currency | Bangladeshi Taka (Tk) |
| Connectivity | 100% Offline — no backend, no network calls |

---

## 1. Executive Summary

Cha Fund is a lightweight, fully offline Android application that helps an individual track a personal money pool on a **per-calendar-month** basis. The user adds money into the pool (entries) and records expenses against it, tagged by **Time Category** (e.g. Morning, Noon). Each calendar month is an isolated accounting period that starts at Tk 0 — there is no carryover.

The app is single-user, on-device only. All data is persisted in a local Room (SQLite) database. There is no authentication, no cloud sync, and no internet permission requirement.

---

## 2. Business Objectives

| # | Objective |
|---|---|
| BO-1 | Give the user a frictionless way to record money added and money spent without internet dependency. |
| BO-2 | Automatically isolate each month's accounting — eliminate manual month management. |
| BO-3 | Provide a clear at-a-glance view of **Balance** (what's left) and **Spent** (what's gone) for the current month. |
| BO-4 | Preserve historical months as read-only records so the user can review past spending behavior. |
| BO-5 | Keep the app 100% private — no data leaves the device. |

---

## 3. Scope

### 3.1 In Scope
- Recording **entries** (money added) and **expenses** (money spent) for the current calendar month.
- Auto-creating the current month on app open / resume.
- Auto-capturing `date`, `day`, `time` for every record (not user-entered at creation).
- Managing user-defined **Time Categories** for expense tagging.
- Daily history and Day Detail views for the active or selected month.
- Monthly history view across all retained months.
- Editing and deleting individual entries and expenses, with live recalculation.
- Deleting past months (with confirmation); current month is protected.
- Light / Dark theme persisted locally.

### 3.2 Out of Scope
- Multi-user / multi-account support.
- Cloud sync, backup, export, or sharing.
- Authentication, biometric lock, or in-app PIN.
- Budget targets, recurring transactions, or forecasting.
- Multi-currency support.
- Reporting / charts / analytics dashboards.
- Push notifications or reminders.
- Carrying balance forward between months.

---

## 4. Stakeholders

| Stakeholder | Role | Interest |
|---|---|---|
| End User (Owner) | Primary user | Track personal monthly fund quickly and privately. |
| Product Owner | Defines requirements & priority | Ensure spec accurately reflects user needs. |
| Android Developer | Builds the app | Implement per spec using Room + Kotlin. |
| QA / Tester | Validates behavior | Confirm calculations, month rollover, edit flows. |

---

## 5. Glossary

| Term | Definition |
|---|---|
| **Entry** | Money the user adds to the fund. Increases Balance. |
| **Expense** | Money the user spends, tagged to a Time Category. Decreases Balance, increases Spent. |
| **Time Category** | User-defined bucket for *when* an expense occurred (e.g. Morning, Noon, Afternoon, Evening, Night). |
| **Month** | An accounting period equal to one calendar month. Isolated from all other months. |
| **Current Month** | The month matching today's device calendar date. Auto-created if missing. The only writable month. |
| **Balance** (Total Amount We Have) | `SUM(entries) − SUM(expenses)` for a month. |
| **Spent** (Total Cost) | `SUM(expenses)` for a month. |

---

## 6. Business Requirements

### 6.1 Entry Management
| ID | Requirement |
|---|---|
| BR-E1 | The user shall add a money entry by entering an amount (Tk, > 0). |
| BR-E2 | The user may optionally provide a short ref/note on an entry. |
| BR-E3 | The system shall auto-capture date, day, and time of the entry. |
| BR-E4 | An entry shall be bound to the current month. |
| BR-E5 | Creating an entry shall immediately increase the Balance shown to the user. |

### 6.2 Expense Management
| ID | Requirement |
|---|---|
| BR-X1 | The user shall add an expense by entering an amount (Tk, > 0). |
| BR-X2 | The user shall select a Time Category for the expense (required). |
| BR-X3 | The user may optionally provide a short ref/note on an expense. |
| BR-X4 | The system shall auto-capture date, day, and time of the expense. |
| BR-X5 | An expense shall be bound to the current month. |
| BR-X6 | Creating an expense shall decrease Balance and increase Spent immediately. |

### 6.3 Time Category Management
| ID | Requirement |
|---|---|
| BR-T1 | The user shall create Time Categories from Settings. |
| BR-T2 | Time Categories shall be global (reusable across all months). |
| BR-T3 | The user shall rename a Time Category. |
| BR-T4 | The user shall delete a Time Category only if it is not referenced by any expense; otherwise the system shall warn or block deletion. |
| BR-T5 | The system shall seed default Time Categories (Morning, Noon, Afternoon, Evening) on first launch. |

### 6.4 Month Lifecycle
| ID | Requirement |
|---|---|
| BR-L1 | On app open and on resume, the system shall detect the device's current calendar month. |
| BR-L2 | If no record exists for the current calendar month, the system shall auto-create it (starting at Tk 0). |
| BR-L3 | Exactly one month shall be flagged as `current` at any time. |
| BR-L4 | When the calendar crosses into a new month, the prior current month shall become read-only history. |
| BR-L5 | All past months shall be retained as read-only and visible in Monthly History. |
| BR-L6 | New entries and expenses shall bind only to the current month. |
| BR-L7 | The current month shall not be deletable. |
| BR-L8 | The current month shall be displayed on Home as a fixed, non-interactive label (no picker). |

### 6.5 Calculations
| ID | Requirement |
|---|---|
| BR-C1 | All totals (Balance, Spent) shall be scoped per month. |
| BR-C2 | Balance = SUM(entries in month) − SUM(expenses in month). |
| BR-C3 | Spent = SUM(expenses in month). |
| BR-C4 | A negative Balance shall be allowed and displayed in red. |
| BR-C5 | Each new month shall start at Tk 0 — no carryover from prior months. |

### 6.6 Daily History (within a month)
| ID | Requirement |
|---|---|
| BR-H1 | The Daily History screen shall, when opened from the bottom nav, default to the current month. |
| BR-H2 | The screen shall list dates that have activity, sorted descending (newest first). |
| BR-H3 | Each date row shall display the date (format `dd MMMM yy`), total expenses for that day, and Balance at that point. |
| BR-H4 | The top bar shall display the month's Spent and Balance. |
| BR-H5 | Tapping a date shall open the Day Detail view. |

### 6.7 Day Detail
| ID | Requirement |
|---|---|
| BR-D1 | The Day Detail shall list all expenses for that date, grouped by Time Category. |
| BR-D2 | The Day Detail shall list all entries for that date. |
| BR-D3 | The user shall edit any entry or expense from this view. |
| BR-D4 | The user shall delete any individual entry or expense from this view. |
| BR-D5 | Edits and deletions shall trigger immediate recomputation of daily and monthly totals. |

### 6.8 Monthly History
| ID | Requirement |
|---|---|
| BR-M1 | The Monthly History screen shall list all retained months. |
| BR-M2 | Each month row shall show total entries, total expenses (Spent), and Balance. |
| BR-M3 | Tapping a month shall open that month's Daily History (read-only for past months). |

### 6.9 Settings
| ID | Requirement |
|---|---|
| BR-S1 | Settings shall display the current month as a read-only, locked row. |
| BR-S2 | Settings shall offer Delete Month, listing only past months (current month hidden). |
| BR-S3 | Deleting a past month shall require user confirmation and cascade-delete its entries and expenses. |
| BR-S4 | Settings shall offer creation and management of Time Categories. |
| BR-S5 | Settings shall offer a theme selection: Light, Dark, and (recommended) Follow System. |
| BR-S6 | The theme preference shall persist locally across launches. |

---

## 7. Functional Flow Summary

```
App Launch / Resume
   └─ Detect current calendar month
        ├─ Exists?  → mark as current, unmark previous
        └─ Missing? → auto-create at Tk 0, mark current

Home (Current Month, locked)
   ├─ Show Balance + Spent
   ├─ Add Entry  → updates Balance
   └─ Add Expense (pick Time Category) → updates Balance + Spent

Daily History (current month by default)
   └─ Date list → Day Detail → Edit / Delete records

Monthly History
   └─ Month list → Daily History (selected month, read-only if past)

Settings
   ├─ Current Month (read-only)
   ├─ Delete Past Month (with confirm)
   ├─ Time Categories (create / rename / delete)
   └─ Theme (Light / Dark / System)
```

---

## 8. Non-Functional Requirements

| ID | Category | Requirement |
|---|---|---|
| NFR-1 | Offline | The app shall function fully without any network connection. The app shall not request `INTERNET` permission. |
| NFR-2 | Privacy | No user data shall leave the device. No telemetry or analytics. |
| NFR-3 | Performance | Home, Daily History, and Monthly History screens shall render within 300 ms on a typical mid-range device for up to 12 months of data. |
| NFR-4 | Reactivity | Balance and Spent shall update without manual refresh whenever data changes (Room `Flow`-driven). |
| NFR-5 | Reliability | All writes shall be transactional; partial saves are not permitted. |
| NFR-6 | Data Integrity | Deleting a month shall cascade-delete its entries and expenses (foreign key `ON DELETE CASCADE`). |
| NFR-7 | Accessibility | Currency, dates, and amounts shall be readable in both Light and Dark themes; minimum touch target 48dp. |
| NFR-8 | Localization | Currency prefix `Tk` shall be shown consistently. Date format `dd MMMM yy`. |
| NFR-9 | Resilience | App shall survive backgrounding across a month boundary and correctly switch the current month on resume. |
| NFR-10 | Maintainability | Schema migrations shall be implemented (no destructive migrations on release builds). |

---

## 9. Data Model (High-Level)

| Entity | Key Fields | Notes |
|---|---|---|
| `Month` | id, year, month, label, isCurrent, createdAt | Exactly one `isCurrent = true` at a time. |
| `TimeCategory` | id, name, sortOrder, createdAt | Global; seeded on first launch. |
| `Entry` | id, monthId (FK), amount, ref?, date, day, time, createdAt, updatedAt | Bound to a month. |
| `Expense` | id, monthId (FK), timeCategoryId (FK), amount, ref?, date, day, time, createdAt, updatedAt | Bound to a month and a category. |

Derived (computed via SQL aggregation, not stored):
- `MonthSummary` = (totalEntries, totalCost/Spent, balance) per month.
- `DailySummary` = (date, totalExpensesForDay, balanceAtThatPoint) per date within a month.

---

## 10. Validation Rules

| Case | Behavior |
|---|---|
| Amount empty or ≤ 0 | Block save; show inline validation error. |
| Expense missing Time Category | Block save. |
| Entry/Expense ref blank | Allowed (optional). |
| Negative balance | Allowed; displayed in red. |
| Delete current month | Blocked at UI and repository layer. |
| Delete Time Category referenced by expenses | Warn or block. |
| App opened in a new calendar month | Auto-create new month before any UI renders entries. |
| App resumed across midnight on month boundary | Detection re-runs on `ON_RESUME`; current month switches. |

---

## 11. UI / UX Principles

- **Currency**: always prefixed with `Tk`.
- **Date format**: `dd MMMM yy` (e.g. `16 June 26`).
- **Short labels**: top bar uses **Balance** and **Spent** instead of long phrases.
- **Current month label**: shown on Home as a fixed locked badge (e.g. "June 2026 🔒") — never a dropdown.
- **Negative balance**: rendered in red.
- **Empty states**: friendly messages — "No entries yet this month", "No expenses for this day".
- **Theming**: Light, Dark, and Follow System; persisted via DataStore.

---

## 12. Assumptions

1. The user has a single device; data is not synced.
2. Device clock is accurate enough to determine the current calendar month.
3. Time Categories are global, not per-month.
4. Entries are not tagged to a Time Category (only expenses are).
5. Expense `ref` follows the same optional pattern as entry `ref`.
6. Editing is performed inline (no dedicated full-screen edit page) unless decided otherwise during design.
7. Default time categories on first launch: Morning, Noon, Afternoon, Evening.

---

## 13. Constraints

| Constraint | Reason |
|---|---|
| No backend / no API | Privacy-first, offline-only product decision. |
| Room (SQLite) only for relational data | Single source of truth on device. |
| Kotlin + Android only | Target platform. |
| Single currency (Tk) | Personal-use product scope. |
| Single user | No auth model in this version. |

---

## 14. Risks & Mitigations

| # | Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|---|
| R-1 | User changes device date/time and corrupts month detection. | Low | Medium | Detect month using `LocalDate.now()`; document behavior. Idempotent upsert by `(year, month)` avoids duplicates. |
| R-2 | Destructive schema migration during update wipes user data. | Medium | High | Implement proper `Migration` objects before any release; avoid `fallbackToDestructiveMigration` in production. |
| R-3 | Time Category deletion breaks historical expenses. | Medium | Medium | Block deletion when referenced, or implement soft-delete. |
| R-4 | App left open across month boundary shows stale month. | Medium | Medium | Re-run detection on `ON_RESUME` (BR-L1). |
| R-5 | Negative balance confuses user. | Low | Low | Color-code red and keep numeric sign visible. |
| R-6 | Data loss on uninstall (no cloud backup). | High | High (for user) | Document clearly; consider future export feature (out of scope for v1). |

---

## 15. Success Criteria

1. The user can add an entry or expense in **≤ 3 taps** from app launch.
2. Balance and Spent on Home update **instantly** after any add / edit / delete.
3. After app is reopened on the first day of a new month, the Home screen shows the new month at Tk 0, with the previous month preserved in Monthly History.
4. The current month is **never deletable** and **never user-switchable** for data entry.
5. The app functions identically with Wi-Fi and mobile data fully disabled.
6. No data loss across normal app updates (with proper migrations in place).

---

## 16. Open Questions for Approval

| # | Question | Default / Recommendation |
|---|---|---|
| Q-1 | Time Categories: global or per-month? | **Global (recommended)** |
| Q-2 | Should entries be tagged to a Time Category? | **No — only expenses** |
| Q-3 | Do expenses need an optional `ref` note? | **Yes — optional, mirroring entries** |
| Q-4 | Edit flow: inline on Day Detail vs. separate screen? | **Inline (recommended for v1)** |
| Q-5 | Provide "Follow System" theme option in addition to Light/Dark? | **Yes (recommended)** |

---

## 17. Approval

| Role | Name | Signature | Date |
|---|---|---|---|
| Product Owner | | | |
| Tech Lead | | | |
| QA Lead | | | |

---

*End of Business Requirements Document — Cha Fund v1.0*
