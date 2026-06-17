# CHF-11 · `DateTimeFormat` utils

| Field | Value |
|---|---|
| Type | Task |
| Priority | P1 |
| Estimate | 1 SP |
| Epic | CHF-7 Core Infrastructure |
| Blocked By | CHF-2 |
| Blocks | CHF-26 |

---

## Goal
Centralize all date/time formatting. Single source of truth for `dd MMMM yy`, day-of-week name, and `HH:mm`. Built on desugared `java.time` (min SDK 24 compatible via CHF-2 setup).

---

## Files to add

| Path | Action |
|---|---|
| `core/utils/DateTimeFormat.kt` | Create |

---

## Implementation

### `DateTimeFormat.kt`
```kotlin
package com.chafund.core.utils

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateTimeFormat {

    private val DATE_DISPLAY = DateTimeFormatter.ofPattern("dd MMMM yy", Locale.ENGLISH)
    private val DAY_NAME     = DateTimeFormatter.ofPattern("EEEE",        Locale.ENGLISH)
    private val TIME_DISPLAY = DateTimeFormatter.ofPattern("HH:mm",       Locale.ENGLISH)
    private val MONTH_LABEL  = DateTimeFormatter.ofPattern("LLLL yyyy",   Locale.ENGLISH)

    /** "16 June 26" */
    fun formatDate(date: LocalDate): String = date.format(DATE_DISPLAY)
    fun formatDate(epochDay: Long): String  = formatDate(LocalDate.ofEpochDay(epochDay))

    /** "Tuesday" */
    fun dayName(date: LocalDate): String   = date.format(DAY_NAME)
    fun dayName(epochDay: Long): String    = dayName(LocalDate.ofEpochDay(epochDay))

    /** "14:30" */
    fun formatTime(time: LocalTime): String = time.format(TIME_DISPLAY)

    /** "June 2026" */
    fun monthLabel(year: Int, month: Int): String =
        LocalDate.of(year, month, 1).format(MONTH_LABEL)
}
```

---

## Acceptance Criteria
- [ ] `formatDate(LocalDate.of(2026, 6, 16))` returns `"16 June 26"`.
- [ ] `dayName(LocalDate.of(2026, 6, 16))` returns `"Tuesday"`.
- [ ] `formatTime(LocalTime.of(14, 30))` returns `"14:30"`.
- [ ] `monthLabel(2026, 6)` returns `"June 2026"`.

---

## Testing
- Unit tests for each formatter — golden values.
- Test boundary: epochDay 0 → `"01 January 70"`.

---

## Notes
- Use `Locale.ENGLISH` explicitly. Don't rely on device default — PRD prescribes a fixed display format.
- Storage of `day` in the DB was rejected in TECH_SPEC §6.4. Always derive from `date`.
