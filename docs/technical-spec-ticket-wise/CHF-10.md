# CHF-10 · `Money` value class + `Tk` formatter

| Field | Value |
|---|---|
| Type | Task |
| Priority | P0 |
| Estimate | 2 SP |
| Epic | CHF-7 Core Infrastructure |
| Blocked By | CHF-2 |
| Blocks | CHF-26 |

---

## Goal
Represent monetary amounts as integer **paisa** (Tk × 100) to avoid floating-point drift. Provide consistent `Tk` formatting used by every screen.

---

## Files to add

| Path | Action |
|---|---|
| `core/utils/Money.kt` | Create |

---

## Implementation

### `Money.kt`
```kotlin
package com.chafund.core.utils

import java.text.NumberFormat
import java.util.Locale

@JvmInline
value class Money(val paisa: Long) {

    val isNegative: Boolean get() = paisa < 0
    val isZero: Boolean     get() = paisa == 0L

    operator fun plus(other: Money)  = Money(paisa + other.paisa)
    operator fun minus(other: Money) = Money(paisa - other.paisa)
    operator fun unaryMinus()        = Money(-paisa)

    /** "Tk 1,250.50" or "Tk -1,250.50" */
    fun formatTk(locale: Locale = Locale.ENGLISH): String {
        val nf = NumberFormat.getNumberInstance(locale).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        val taka = paisa / 100.0
        return "Tk ${nf.format(taka)}"
    }

    companion object {
        val Zero = Money(0L)
        fun fromTk(tk: Double): Money = Money(Math.round(tk * 100.0))
        fun fromTkString(text: String): Money? =
            text.trim().toDoubleOrNull()?.let { fromTk(it) }
    }
}
```

---

## Acceptance Criteria
- [ ] No floating-point arithmetic on `paisa` (storage is `Long`).
- [ ] `Money.Zero.formatTk()` returns `"Tk 0.00"`.
- [ ] `Money(125050).formatTk()` returns `"Tk 1,250.50"`.
- [ ] `Money(-125050).formatTk()` returns `"Tk -1,250.50"`.
- [ ] Addition / subtraction produce correct paisa sums.

---

## Testing
- Unit tests for:
  - Zero, positive, negative formatting
  - Grouping (1k, 10k, 100k, 1M)
  - Two-decimal precision (`Money(99).formatTk()` → `"Tk 0.99"`)
  - `fromTk(12.345)` → `1234` paisa (rounded half-up)
  - `fromTkString("abc")` → `null`

---

## Notes
- Color rendering for negative values lives in `MoneyText` (CHF-26), not here. `Money` is purely numeric.
- If localization expands, parameterize locale on the formatter — do not change the storage unit.
