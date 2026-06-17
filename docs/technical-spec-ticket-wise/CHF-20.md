# CHF-20 · `MonthManager` + `ProcessLifecycleOwner` hook

| Field | Value |
|---|---|
| Type | Task |
| Priority | P0 |
| Estimate | 5 SP |
| Epic | CHF-7 Core Infrastructure |
| Blocked By | CHF-13, CHF-19, CHF-5 |
| Blocks | CHF-21, CHF-29, CHF-36, CHF-43 |

---

## Goal
Detect today's calendar month on app start and on every `ON_RESUME`. Upsert `MonthEntity`, promote it as `isCurrent`, and publish the id to `Session`. Implements BR-L1…BR-L7.

---

## Files to add / modify

| Path | Action |
|---|---|
| `core/utils/MonthManager.kt` | Create |
| `App.kt` | Modify — register observer + first detection |

---

## Implementation

### `MonthManager.kt`
```kotlin
package com.chafund.core.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.chafund.core.data.database.dao.MonthDao
import com.chafund.core.data.database.entity.MonthEntity
import com.chafund.core.data.session.Session
import com.chafund.core.domain.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate

class MonthManager(
    private val monthDao: MonthDao,
    private val session: Session,
    private val dispatchers: DispatcherProvider,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + dispatchers.io),
) : LifecycleEventObserver {

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_RESUME) detectAndPromote()
    }

    fun detectAndPromote() {
        scope.launch {
            val today = LocalDate.now()
            val candidate = MonthEntity(
                year      = today.year,
                month     = today.monthValue,
                label     = DateTimeFormat.monthLabel(today.year, today.monthValue),
                isCurrent = true,
                createdAt = System.currentTimeMillis(),
            )
            val id = monthDao.upsertByYearMonth(candidate)
            monthDao.promoteToCurrent(id)
            session.setCurrentMonth(id)
        }
    }
}
```

### `App.kt`
```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        AppLogger.init(BuildConfig.DEBUG)
        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(appModules())
        }
        // Resolve and register the lifecycle observer.
        val monthManager: MonthManager = GlobalContext.get().get()
        ProcessLifecycleOwner.get().lifecycle.addObserver(monthManager)
    }
}
```

---

## Acceptance Criteria
- [ ] First launch (no rows) → creates `MonthEntity` for today and sets `Session.currentMonthId`.
- [ ] Second launch same month → no duplicate row; `currentMonthId` stays.
- [ ] App backgrounded → date advances to next month → on resume, new `MonthEntity` created and promoted; `Session` updates.
- [ ] `upsertByYearMonth` is idempotent via `(year, month)` unique index.
- [ ] `promoteToCurrent` always leaves exactly one row with `isCurrent = 1`.

---

## Testing
- **Integration test** (in-memory Room + fake `Session`):
  - Initial detection creates row, sets `Session.currentMonthId`.
  - Calling `detectAndPromote` twice → still one row, same id.
  - Simulate month boundary: insert a "previous" month flagged current, then call `detectAndPromote` with today in a new month → previous row's `isCurrent` becomes `0`, new row is created and flagged.
- **Unit test**: lifecycle event other than `ON_RESUME` → no detection runs.

---

## Notes
- `MonthManager` does not block `Application.onCreate`. Detection runs on `dispatchers.io` so cold-start time stays low.
- If the user manually changes device date backward to a past month, `MonthManager` promotes that past month back to `isCurrent` — documented as expected (PRD §11 / BRD R-1).
