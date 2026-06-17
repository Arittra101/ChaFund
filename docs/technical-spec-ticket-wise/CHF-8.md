# CHF-8 · Domain primitives — `Result`, `RootError`, `DataError.Local`

| Field | Value |
|---|---|
| Type | Task |
| Priority | P0 |
| Estimate | 2 SP |
| Epic | CHF-7 Core Infrastructure |
| Blocked By | CHF-2 |
| Blocks | CHF-28, CHF-35, CHF-43 |

---

## Goal
Establish the no-throw contract. Repositories return `Result<D, E>`; ViewModels handle `Success` / `Error` exhaustively without ever seeing Room exceptions.

---

## Files to add

| Path | Action |
|---|---|
| `core/domain/Result.kt` | Create |
| `core/domain/RootError.kt` | Create |
| `core/domain/DataError.kt` | Create |
| `core/domain/ResultExt.kt` | Create — `map`, `onSuccess`, `onError`, `fold` |

---

## Implementation

### `Result.kt`
```kotlin
package com.chafund.core.domain

sealed interface Result<out D, out E : RootError> {
    data class Success<out D>(val data: D) : Result<D, Nothing>
    data class Error<out E : RootError>(val error: E) : Result<Nothing, E>
}
```

### `RootError.kt`
```kotlin
package com.chafund.core.domain
sealed interface RootError
```

### `DataError.kt`
```kotlin
package com.chafund.core.domain

sealed interface DataError : RootError {
    enum class Local : DataError { NOT_FOUND, DISK_FULL, UNKNOWN }
}
```

> No `DataError.Remote` variant — this app has no remote layer.

### `ResultExt.kt`
```kotlin
package com.chafund.core.domain

inline fun <D, E : RootError, R> Result<D, E>.map(transform: (D) -> R): Result<R, E> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Error   -> this
}

inline fun <D, E : RootError> Result<D, E>.onSuccess(action: (D) -> Unit): Result<D, E> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <D, E : RootError> Result<D, E>.onError(action: (E) -> Unit): Result<D, E> {
    if (this is Result.Error) action(error)
    return this
}

inline fun <D, E : RootError, R> Result<D, E>.fold(
    onSuccess: (D) -> R,
    onError: (E) -> R,
): R = when (this) {
    is Result.Success -> onSuccess(data)
    is Result.Error   -> onError(error)
}
```

---

## Acceptance Criteria
- [ ] `Result` sealed interface with `Success` + `Error`.
- [ ] `DataError.Local` enum has exactly: `NOT_FOUND`, `DISK_FULL`, `UNKNOWN`.
- [ ] No `Remote` variant exists.
- [ ] `map`, `onSuccess`, `onError`, `fold` extensions compile and test green.

---

## Testing
- Unit:
  - `Success(42).map { it * 2 }` → `Success(84)`.
  - `Error(NOT_FOUND).map { it.toString() }` → `Error(NOT_FOUND)`.
  - Exhaustive `when` over `DataError.Local` compiles without `else`.

---

## Notes
- Keep this module pure-Kotlin — no Android imports — so it can be unit-tested without Robolectric.
