# CHF-14 · `TimeCategoryEntity` + `TimeCategoryDao` + `SeedCallback`

| Field | Value |
|---|---|
| Type | Task |
| Priority | P0 |
| Estimate | 3 SP |
| Epic | CHF-7 Core Infrastructure |
| Blocked By | CHF-12 |
| Blocks | CHF-16, CHF-17 |

---

## Goal
Global Time Category table (no `monthId`), seeded with `Morning`, `Noon`, `Afternoon`, `Evening` on first DB creation.

---

## Files to add

| Path | Action |
|---|---|
| `core/data/database/entity/TimeCategoryEntity.kt` | Create |
| `core/data/database/dao/TimeCategoryDao.kt` | Create |
| `core/data/database/seed/SeedCallback.kt` | Create |
| `ChaFundDb.kt` | Modify — register entity, DAO, attach callback in builder (wiring in CHF-17) |

---

## Implementation

### `TimeCategoryEntity.kt`
```kotlin
package com.chafund.core.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "TimeCategory",
    indices = [Index(value = ["name"], unique = true)],
)
data class TimeCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "name", collate = ColumnInfo.NOCASE) val name: String,
    val sortOrder: Int,
    val createdAt: Long,
)
```

### `TimeCategoryDao.kt`
```kotlin
package com.chafund.core.data.database.dao

import androidx.room.*
import com.chafund.core.data.database.entity.TimeCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeCategoryDao {

    @Query("SELECT * FROM TimeCategory ORDER BY sortOrder ASC, id ASC")
    fun observeAll(): Flow<List<TimeCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(category: TimeCategoryEntity): Long

    @Query("UPDATE TimeCategory SET name = :name WHERE id = :id")
    suspend fun rename(id: Long, name: String): Int

    @Query("DELETE FROM TimeCategory WHERE id = :id")
    suspend fun deleteById(id: Long): Int
}
```

> Deletion-in-use guard is enforced by:
> 1. `ExpenseEntity.timeCategoryId` FK with `onDelete = RESTRICT` (set in CHF-16).
> 2. Repository pre-check via `ExpenseDao.countByCategory(id)` (CHF-43).

### `SeedCallback.kt`
```kotlin
package com.chafund.core.data.database.seed

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

class SeedCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        val now = System.currentTimeMillis()
        val defaults = listOf(
            "Morning"   to 1,
            "Noon"      to 2,
            "Afternoon" to 3,
            "Evening"   to 4,
        )
        db.beginTransaction()
        try {
            defaults.forEach { (name, sort) ->
                db.execSQL(
                    "INSERT INTO TimeCategory (name, sortOrder, createdAt) VALUES (?, ?, ?)",
                    arrayOf<Any>(name, sort, now),
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}
```

### `ChaFundDb.kt`
```kotlin
@Database(
    entities = [MonthEntity::class, TimeCategoryEntity::class /* , … */],
    version = 1, exportSchema = true,
)
@TypeConverters(ChaFundTypeConverters::class)
abstract class ChaFundDb : RoomDatabase() {
    abstract fun monthDao(): MonthDao
    abstract fun timeCategoryDao(): TimeCategoryDao
}
```

---

## Acceptance Criteria
- [ ] `name` column has `COLLATE NOCASE` + unique index — `"morning"` and `"Morning"` conflict.
- [ ] `SeedCallback.onCreate` inserts exactly 4 default rows.
- [ ] Seeding runs **only** on first DB creation (Room contract — not on subsequent launches).
- [ ] Renaming a category does not orphan any expense (FK references id, not name).

---

## Testing
- DAO test:
  - After DB creation, `observeAll()` first emission has 4 rows in order `Morning, Noon, Afternoon, Evening`.
  - Insert duplicate `"morning"` (different case) → `SQLiteConstraintException`.
  - `rename(id, "Late Night")` → next emission reflects the new name with same id.
  - Reopen DB → seeding is not re-applied.

---

## Notes
- `SeedCallback` is attached when the DB is built (`databaseModule` in CHF-17).
- If product later wants to add/remove defaults, schema bumps are not required — adjust seed and ship.
