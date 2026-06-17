# CHF-12 · Room `ChaFundDb` skeleton + `TypeConverter`s

| Field | Value |
|---|---|
| Type | Task |
| Priority | P0 |
| Estimate | 3 SP |
| Epic | CHF-7 Core Infrastructure |
| Blocked By | CHF-2 |
| Blocks | CHF-13, CHF-14, CHF-15, CHF-16 |

---

## Goal
Skeleton `RoomDatabase` with version 1, schema export, and a placeholder `TypeConverters` class. Entities/DAOs are added in subsequent tickets, not here.

---

## Files to add

| Path | Action |
|---|---|
| `core/data/database/ChaFundDb.kt` | Create |
| `core/data/database/converter/ChaFundTypeConverters.kt` | Create |
| `app/schemas/` (Gradle config) | Already set in CHF-2 (`ksp { arg("room.schemaLocation", ...) }`) |

---

## Implementation

### `ChaFundTypeConverters.kt`
```kotlin
package com.chafund.core.data.database.converter

import androidx.room.TypeConverter
import java.time.LocalTime

class ChaFundTypeConverters {

    // LocalTime ↔ String "HH:mm"
    @TypeConverter
    fun localTimeToString(t: LocalTime?): String? = t?.toString()  // "HH:mm:ss" — fine

    @TypeConverter
    fun stringToLocalTime(s: String?): LocalTime? = s?.let { LocalTime.parse(it) }
}
```
> `date` is stored as `Long` (epoch-day) directly — no converter needed.
> `createdAt`/`updatedAt` are `Long` epoch millis — no converter needed.

### `ChaFundDb.kt` (skeleton)
```kotlin
package com.chafund.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.chafund.core.data.database.converter.ChaFundTypeConverters

@Database(
    entities = [
        // MonthEntity::class,         // CHF-13
        // TimeCategoryEntity::class,  // CHF-14
        // EntryEntity::class,         // CHF-15
        // ExpenseEntity::class,       // CHF-16
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(ChaFundTypeConverters::class)
abstract class ChaFundDb : RoomDatabase() {
    // abstract fun monthDao(): MonthDao         // CHF-13
    // abstract fun timeCategoryDao(): TimeCategoryDao  // CHF-14
    // abstract fun entryDao(): EntryDao         // CHF-15
    // abstract fun expenseDao(): ExpenseDao     // CHF-16
}
```

> The skeleton intentionally does not compile until at least one entity is added (CHF-13). That keeps the ticket small but enforces sequential merging.

---

## Acceptance Criteria
- [ ] `ChaFundDb` class file exists with version 1 and `exportSchema = true`.
- [ ] `ChaFundTypeConverters` registered via `@TypeConverters`.
- [ ] `app/schemas/` directory created by KSP on first build (after CHF-13 adds an entity).
- [ ] No `fallbackToDestructiveMigration()` anywhere.

---

## Testing
- N/A in isolation — DAO tests in CHF-13/14/15/16 exercise the DB.

---

## Notes
- Schema export is critical for migration tests in CHF-50. Confirm `schemas/` is checked into VCS so migration tests can read previous versions.
