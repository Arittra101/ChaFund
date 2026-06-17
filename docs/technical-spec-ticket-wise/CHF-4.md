# CHF-4 · Wire AppLogger (Timber)

| Field | Value |
|---|---|
| Type | Task |
| Priority | P2 |
| Estimate | 1 SP |
| Epic | CHF-1 Foundation & Setup |
| Blocked By | CHF-2 |
| Blocks | CHF-5, CHF-21 |

---

## Goal
Provide a single logging façade `AppLogger` over Timber. Debug builds plant `DebugTree`; release builds strip logs.

---

## Files to add

| Path | Action |
|---|---|
| `core/utils/AppLogger.kt` | Create |
| `proguard-rules.pro` | Modify — strip Timber `v/d` in release |

---

## Implementation

### `AppLogger.kt`
```kotlin
package com.chafund.core.utils

import timber.log.Timber

object AppLogger {
    fun init(isDebug: Boolean) {
        if (isDebug && Timber.forest().isEmpty()) {
            Timber.plant(Timber.DebugTree())
        }
    }

    fun d(tag: String, msg: String) = Timber.tag(tag).d(msg)
    fun i(tag: String, msg: String) = Timber.tag(tag).i(msg)
    fun w(tag: String, msg: String, t: Throwable? = null) = Timber.tag(tag).w(t, msg)
    fun e(tag: String, msg: String, t: Throwable? = null) = Timber.tag(tag).e(t, msg)
}
```

### `proguard-rules.pro` snippet
```
-assumenosideeffects class timber.log.Timber {
    public static *** v(...);
    public static *** d(...);
}
```

---

## Acceptance Criteria
- [ ] `AppLogger.init(BuildConfig.DEBUG)` called from `App.onCreate` (wired in CHF-5).
- [ ] Debug build emits Logcat output via Timber.
- [ ] Release build (after R8) does not include `v`/`d` log strings.
- [ ] No `Log.x(...)` calls anywhere else in the codebase (enforce via detekt rule in CHF-52).

---

## Testing
- Manual: Logcat shows tagged log lines on debug.
- Manual: APK size diff or string dump shows debug logs stripped in release.

---

## Notes
- Keep the wrapper minimal — no JSON pretty-printers, no custom formatters. The point is a single dependency boundary.
