# CHF-19 · `Session` + `sessionModule`

| Field | Value |
|---|---|
| Type | Task |
| Priority | P0 |
| Estimate | 2 SP |
| Epic | CHF-7 Core Infrastructure |
| Blocked By | CHF-2 |
| Blocks | CHF-20, CHF-29, CHF-36 |

---

## Goal
Single cross-feature reactive state holder for the current month id, plus a `monthChanged` trigger features can listen on. Features observe `Session` instead of importing each other.

---

## Files to add

| Path | Action |
|---|---|
| `core/data/session/Session.kt` | Create |
| `core/di/sessionModule.kt` | Create |
| `core/di/appModules.kt` | Modify |

---

## Implementation

### `Session.kt`
```kotlin
package com.chafund.core.data.session

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class Session {
    private val _currentMonthId = MutableStateFlow(0L)
    val currentMonthId: StateFlow<Long> = _currentMonthId.asStateFlow()

    private val _monthChanged = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val monthChanged: SharedFlow<Unit> = _monthChanged.asSharedFlow()

    /** Returns true if value changed. */
    fun setCurrentMonth(id: Long): Boolean {
        if (_currentMonthId.value == id) return false
        _currentMonthId.value = id
        _monthChanged.tryEmit(Unit)
        return true
    }
}
```

### `sessionModule.kt`
```kotlin
package com.chafund.core.di

import com.chafund.core.data.session.Session
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val sessionModule = module {
    singleOf(::Session)
}
```

### `appModules.kt`
```kotlin
fun appModules(): List<Module> = listOf(
    databaseModule,
    storageModule,
    sessionModule,
    // utilsModule, features...
)
```

---

## Acceptance Criteria
- [ ] `Session` is a Koin `single` — every feature gets the same instance.
- [ ] `setCurrentMonth(sameId)` does **not** re-emit `monthChanged`.
- [ ] `currentMonthId` initial value is `0L` (sentinel — replaced by `MonthManager` on first detection).
- [ ] `monthChanged` is a `SharedFlow` with `extraBufferCapacity = 1` so late subscribers don't miss the first emission within a tick.

---

## Testing
- Unit (Turbine):
  - `setCurrentMonth(1)` → `monthChanged` emits once; `currentMonthId` is `1`.
  - `setCurrentMonth(1)` again → no new emission on `monthChanged`.
  - `setCurrentMonth(2)` → another `monthChanged` emission; `currentMonthId` is `2`.

---

## Notes
- Keep `Session` minimal. Don't add unrelated state here (e.g., feature flags) — make a dedicated holder if needed.
- Do **not** persist `currentMonthId` — it is derived from the device calendar via `MonthManager` (CHF-20).
