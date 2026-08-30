# ChaFund — Database Schema

**Engine:** Room (SQLite) · `ChaFundDb` · Version 3

---

## Tables

### `Month`

Represents a single calendar month (the active budget period).

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `id` | `INTEGER` | **PK**, auto-increment | |
| `year` | `INTEGER` | NOT NULL | e.g. `2026` |
| `month` | `INTEGER` | NOT NULL | 1–12 |
| `label` | `TEXT` | NOT NULL | Human-readable label (e.g. `"July 2026"`) |
| `isCurrent` | `INTEGER` | NOT NULL | Boolean flag; `1` = active month |
| `createdAt` | `INTEGER` | NOT NULL | Epoch millis |
| `cycleStartEpochDay` | `INTEGER` | NULLABLE | Optional custom cycle start (epoch-day) in the *previous* calendar month. Null = plain calendar month |
| `includePrevTail` | `INTEGER` | NOT NULL, default `0` | Boolean; whether the previous-month tail from `cycleStartEpochDay` is currently counted |

**Indexes:**
- `UNIQUE (year, month)`
- `(isCurrent)`

**Custom cycle:** when `cycleStartEpochDay` is set and `includePrevTail = 1`, this month's history
and totals also include Entry/Expense rows dated between `cycleStartEpochDay` and the last day of the
previous month — a **non-destructive overlay** (those rows still belong to the previous month via
`monthId`, so they appear in both months). Aggregations use
`monthId = :id OR (date BETWEEN cycleStart AND prevMonthEnd)`.

---

### `TimeCategory`

Lookup table for time-of-day categories. Seeded on first install.

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `id` | `INTEGER` | **PK**, auto-increment | |
| `name` | `TEXT` | NOT NULL, NOCASE collation | `"Morning"`, `"Noon"`, `"Afternoon"`, `"Evening"` |
| `sortOrder` | `INTEGER` | NOT NULL | Display order (1–4) |
| `createdAt` | `INTEGER` | NOT NULL | Epoch millis |

**Indexes:**
- `UNIQUE (name)`

**Seed data (inserted on DB creation):**

| name | sortOrder |
|---|---|
| Morning | 1 |
| Noon | 2 |
| Afternoon | 3 |
| Evening | 4 |

---

### `PersonGroup`

A named group that people/names are organized under (e.g. `"Android"`). Created in Settings.

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `id` | `INTEGER` | **PK**, auto-increment | |
| `name` | `TEXT` | NOT NULL, NOCASE collation | Group label |
| `createdAt` | `INTEGER` | NOT NULL | Epoch millis |

**Indexes:**
- `UNIQUE (name)`

---

### `Person`

A name that entries can be attributed to. Belongs to exactly one `PersonGroup`.

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `id` | `INTEGER` | **PK**, auto-increment | |
| `name` | `TEXT` | NOT NULL, NOCASE collation | Person's name |
| `groupId` | `INTEGER` | NOT NULL, FK → `PersonGroup.id` ON DELETE RESTRICT | |
| `createdAt` | `INTEGER` | NOT NULL | Epoch millis |

**Indexes:**
- `(groupId)`
- `UNIQUE (groupId, name)` (same name allowed in different groups)

Displayed as `"<name> ~ <groupName>"` (e.g. `Arittra ~ Android`).

---

### `Entry`

A fund collection entry (money received / contributed).

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `id` | `INTEGER` | **PK**, auto-increment | |
| `monthId` | `INTEGER` | NOT NULL, FK → `Month.id` ON DELETE CASCADE | |
| `amountPaisa` | `INTEGER` | NOT NULL | Amount stored in paisa (₹ × 100) |
| `ref` | `TEXT` | NULLABLE | Legacy reference note (pre-v2 entries) |
| `personId` | `INTEGER` | NULLABLE | Attributed name → `Person.id`. No SQL FK; nulled in-app when the person is deleted |
| `date` | `INTEGER` | NOT NULL | Epoch-day (days since Unix epoch) |
| `time` | `TEXT` | NOT NULL | Wall-clock time `"HH:mm"` |
| `createdAt` | `INTEGER` | NOT NULL | Epoch millis |
| `updatedAt` | `INTEGER` | NOT NULL | Epoch millis |

**Indexes:**
- `(monthId)`
- `(date)`
- `(personId)`

New entries store `personId` (name required) and leave `ref` null; pre-v2 entries keep their
`ref` and display it as a fallback.

---

### `Expense`

An expense / withdrawal against a month's fund.

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `id` | `INTEGER` | **PK**, auto-increment | |
| `monthId` | `INTEGER` | NOT NULL, FK → `Month.id` ON DELETE CASCADE | |
| `timeCategoryId` | `INTEGER` | NOT NULL, FK → `TimeCategory.id` ON DELETE RESTRICT | |
| `amountPaisa` | `INTEGER` | NOT NULL | Amount stored in paisa (₹ × 100) |
| `ref` | `TEXT` | NULLABLE | Optional reference note |
| `date` | `INTEGER` | NOT NULL | Epoch-day (days since Unix epoch) |
| `time` | `TEXT` | NOT NULL | Wall-clock time `"HH:mm"` |
| `createdAt` | `INTEGER` | NOT NULL | Epoch millis |
| `updatedAt` | `INTEGER` | NOT NULL | Epoch millis |

**Indexes:**
- `(monthId)`
- `(date)`
- `(timeCategoryId)`

---

## Relationships

```
Month ──< Entry            (one-to-many, CASCADE delete)
Month ──< Expense          (one-to-many, CASCADE delete)
TimeCategory ──< Expense   (one-to-many, RESTRICT delete)
PersonGroup ──< Person     (one-to-many, RESTRICT delete)
Person ──< Entry           (soft link via nullable Entry.personId; no SQL FK)
```

---

## DataStore Preferences (`chafund_prefs`)

Separate key-value store (Jetpack DataStore), not Room.

| Key | Type | Values | Default |
|---|---|---|---|
| `theme_mode` | `STRING` | `LIGHT` · `DARK` · `SYSTEM` | `SYSTEM` |

---

## Notes

- All monetary values are stored as **paisa** (integer) to avoid floating-point precision issues. Divide by 100 to get rupees.
- Dates are split into an **epoch-day** integer (`date`) and an `"HH:mm"` string (`time`) rather than a single timestamp.
- Deleting a `Month` cascades to all its `Entry` and `Expense` rows.
- `TimeCategory` rows cannot be deleted while any `Expense` references them (`RESTRICT`).
