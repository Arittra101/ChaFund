# CHF-13 · `MonthEntity` + `MonthDao`

| Field | Value |
|---|---|
| Type | Task |
| Priority | P0 |
| Estimate | 3 SP |
| Epic | CHF-7 Core Infrastructure |
| Blocked By | CHF-12 |
| Blocks | CHF-15, CHF-16, CHF-17, CHF-20 |

---

## Goal
First-class Month table with unique `(year, month)`, single-current invariant enforced by a transactional `promoteToCurrent`, and a guarded delete that blocks the current month.

---

## Files to add

| Path | Action |
|---|---|
| `core/data/database/entity/MonthEntity.kt` | Create |
| `core/data/database/dao/MonthDao.kt` | Create |
| `core/data/database/projection/MonthSummaryProjection.kt` | Create |
| `core/data/database/ChaFundDb.kt` | Modify — register entity + DAO |

---

## Implementation

### `MonthEntity.kt`
```kotlin
package com.chafund.core.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Month",
    indices = [
        Index(value = ["year", "month"], unique = true),
        Index(value = ["isCurrent"]),
    ],
)
data class MonthEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val year: Int,
    val month: Int,                // 1..12
    val label: String,             // "June 2026"
    val isCurrent: Boolean,
    val createdAt: Long,           // epoch millis
)
```

### `MonthSummaryProjection.kt`
```kotlin
package com.chafund.core.data.database.projection

data class MonthSummaryProjection(
    val monthId: Long,
    val year: Int,
    val month: Int,
    val label: String,
    val isCurrent: Boolean,
    val totalEntriesPaisa: Long,
    val totalSpentPaisa: Long,
)
```

### `MonthDao.kt`
```kotlin
package com.chafund.core.data.database.dao

import androidx.room.*
import com.chafund.core.data.database.entity.MonthEntity
import com.chafund.core.data.database.projection.MonthSummaryProjection
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthDao {

    @Query("SELECT * FROM Month WHERE isCurrent = 1 LIMIT 1")
    fun observeCurrent(): Flow<MonthEntity?>

    @Query("SELECT * FROM Month WHERE year = :year AND month = :month LIMIT 1")
    suspend fun findByYearMonth(year: Int, month: Int): MonthEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(month: MonthEntity): Long

    /**
     * Idempotent upsert keyed on (year, month). Returns the id (existing or new).
     */
    @Transaction
    suspend fun upsertByYearMonth(month: MonthEntity): Long {
        val existing = findByYearMonth(month.year, month.month)
        return existing?.id ?: insertOrIgnore(month).takeIf { it != -1L }
            ?: findByYearMonth(month.year, month.month)!!.id
    }

    @Query("UPDATE Month SET isCurrent = 0 WHERE isCurrent = 1")
    suspend fun unflagAllCurrent(): Int

    @Query("UPDATE Month SET isCurrent = 1 WHERE id = :id")
    suspend fun flagCurrent(id: Long): Int

    @Transaction
    suspend fun promoteToCurrent(id: Long) {
        unflagAllCurrent()
        flagCurrent(id)
    }

    @Query("SELECT * FROM Month ORDER BY year DESC, month DESC")
    fun observeAll(): Flow<List<MonthEntity>>

    @Query("SELECT * FROM Month WHERE isCurrent = 0 ORDER BY year DESC, month DESC")
    fun observePast(): Flow<List<MonthEntity>>

    /** Guard: deletes only past months. Returns rows affected (0 if current). */
    @Query("DELETE FROM Month WHERE id = :id AND isCurrent = 0")
    suspend fun deletePastById(id: Long): Int

    @Query("""
        SELECT m.id AS monthId, m.year, m.month, m.label, m.isCurrent,
          IFNULL((SELECT SUM(amountPaisa) FROM Entry   WHERE monthId = m.id), 0) AS totalEntriesPaisa,
          IFNULL((SELECT SUM(amountPaisa) FROM Expense WHERE monthId = m.id), 0) AS totalSpentPaisa
        FROM Month m
        ORDER BY m.year DESC, m.month DESC
    """)
    fun observeMonthSummaries(): Flow<List<MonthSummaryProjection>>
}
```

> The `observeMonthSummaries` SQL references `Entry` and `Expense` tables added in CHF-15/16. Add the query only after those entities are merged, or stage the file behind a feature flag during development.

### `ChaFundDb.kt`
```kotlin
@Database(
    entities = [MonthEntity::class /* , others later */ ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(ChaFundTypeConverters::class)
abstract class ChaFundDb : RoomDatabase() {
    abstract fun monthDao(): MonthDao
}
```

---

## Acceptance Criteria
- [ ] Unique index on `(year, month)` prevents duplicates.
- [ ] `promoteToCurrent` runs as a single `@Transaction` (never leaves DB with two current rows).
- [ ] `deletePastById` returns `0` when called with the current month's id.
- [ ] `upsertByYearMonth` is idempotent — calling twice with the same `(year, month)` yields the same id.
- [ ] `observeMonthSummaries` projects entries/spent correctly (requires CHF-15/16 merged for the query to compile).

---

## Testing
- DAO test (in-memory Room):
  - Insert duplicate `(year, month)` → second insert ignored, id stable.
  - `promoteToCurrent(B)` after `promoteToCurrent(A)` → exactly one row has `isCurrent = 1`.
  - `deletePastById(currentId)` → returns 0; row still present.
  - `deletePastById(pastId)` → returns 1; cascades into related rows (after CHF-15/16).

---

## Notes
- Foreign keys from `Entry`/`Expense` to `Month` are declared in their own entities (CHF-15/16) with `onDelete = CASCADE`.
