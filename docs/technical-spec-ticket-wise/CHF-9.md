# CHF-9 · `DispatcherProvider` for testable threading

| Field | Value |
|---|---|
| Type | Task |
| Priority | P1 |
| Estimate | 1 SP |
| Epic | CHF-7 Core Infrastructure |
| Blocked By | CHF-2 |
| Blocks | CHF-21 |

---

## Goal
Inject coroutine dispatchers so tests can substitute `StandardTestDispatcher` / `UnconfinedTestDispatcher` instead of `Dispatchers.IO`.

---

## Files to add

| Path | Action |
|---|---|
| `core/domain/DispatcherProvider.kt` | Create |
| `core/domain/DefaultDispatcherProvider.kt` | Create |
| `core/domain/TestDispatcherProvider.kt` | Create (under `src/test`) |

---

## Implementation

### `DispatcherProvider.kt`
```kotlin
package com.chafund.core.domain

import kotlinx.coroutines.CoroutineDispatcher

interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}
```

### `DefaultDispatcherProvider.kt`
```kotlin
package com.chafund.core.domain

import kotlinx.coroutines.Dispatchers

class DefaultDispatcherProvider : DispatcherProvider {
    override val main    = Dispatchers.Main
    override val io      = Dispatchers.IO
    override val default = Dispatchers.Default
}
```

### `TestDispatcherProvider.kt` (test scope)
```kotlin
package com.chafund.core.domain

import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher

class TestDispatcherProvider(
    val test: TestDispatcher = StandardTestDispatcher(),
) : DispatcherProvider {
    override val main    = test
    override val io      = test
    override val default = test
}
```

---

## Acceptance Criteria
- [ ] `DispatcherProvider` interface + `DefaultDispatcherProvider` impl exist.
- [ ] `TestDispatcherProvider` available for tests.
- [ ] Will be registered via Koin in CHF-21.

---

## Testing
- Unit test verifies `DefaultDispatcherProvider` returns `Dispatchers.Main / IO / Default` references.

---

## Notes
- Do **not** capture dispatchers at construction time inside a singleton beyond this provider — always go through the interface so tests can swap.
