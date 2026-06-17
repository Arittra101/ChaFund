# CHF-15 · `EntryEntity` + `EntryDao`

| Field | Value |
|---|---|
| Type | Task |
| Priority | P0 |
| Estimate | 3 SP |
| Epic | CHF-7 Core Infrastructure |
| Blocked By | CHF-13 |
| Blocks | CHF-17 |

---

## Goal
Persist money-in records with `amountPaisa`, optional `ref`, auto-captured date/time, and FK to Month (cascade delete on month removal).

---

## Files to add

| Path | Action |
|---|---|
| `core/data/database/entity/EntryEntity.kt` | Create |
| `core/data/database/dao/EntryDao.kt` | Create |
| `ChaFundDb.kt` | Modify — register entity + DAO |

---

## Implementation

### `EntryEntity.kt`
```kotlin
package com.chafund.core.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Entry",
    foreignKeys = [
        ForeignKey(
            entity = MonthEntity::class,
            parentColumns = ["id"],
            childColumns = ["monthId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("monthId"),
        Index("date"),
    ],
)
data class EntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val monthId: Long,
    val amountPaisa: Long,        // Tk × 100, > 0 (validated at repo)
    val ref: String?,
    val date: Long,               // epoch-day
    val time: String,             // "HH:mm"
    val createdAt: Long,          // epoch millis
    val updatedAt: Long,
)
```

### `EntryDao.kt`
```kotlin
package com.chafund.core.data.database.dao

import androidx.room.*
import com.chafund.core.data.database.entity.EntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {

    @Query("SELECT * FROM Entry WHERE monthId = :monthId ORDER BY date DESC, time DESC, id DESC")
    fun observeByMonth(monthId: Long): Flow<List<EntryEntity>>

    @Query("SELECT * FROM Entry WHERE monthId = :monthId AND date = :date ORDER BY time DESC, id DESC")
    fun observeByDate(monthId: Long, date: Long): Flow<List<EntryEntity>>

    @Query("SELECT IFNULL(SUM(amountPaisa), 0) FROM Entry WHERE monthId = :monthId")
    fun sumByMonth(monthId: Long): Flow<Long>

    @Insert
    suspend fun insert(entry: EntryEntity): Long

    @Update
    suspend fun update(entry: EntryEntity): Int

    @Query("DELETE FROM Entry WHERE id = :id")
    suspend fun deleteById(id: Long): Int
}
```

### `ChaFundDb.kt`
```kotlin
@Database(
    entities = [MonthEntity::class, TimeCategoryEntity::class, EntryEntity::class /* +Expense */],
    version = 1, exportSchema = true,
)
@TypeConverters(ChaFundTypeConverters::class)
abstract class ChaFundDb : RoomDatabase() {
    abstract fun monthDao(): MonthDao
    abstract fun timeCategoryDao(): TimeCategoryDao
    abstract fun entryDao(): EntryDao
}
```

---

## Acceptance Criteria
- [ ] FK `monthId → Month` declared with `CASCADE`.
- [ ] Indices on `monthId`, `date`.
- [ ] `sumByMonth` returns `0` when no rows exist (uses `IFNULL`).
- [ ] Deleting the parent Month cascades — `Entry` rows for that month are gone.

---

## Testing
- DAO test (in-memory Room):
  - Insert 3 entries with amounts 100, 200, 300 paisa → `sumByMonth` Flow emits `600`.
  - Delete parent `Month` → `observeByMonth` flow emits empty list.
  - Update entry's `amountPaisa` → `sumByMonth` re-emits updated total.

---

## Notes
- The `date` column stores **epoch-day** (a `Long`). Day-of-week is derived at display time via `DateTimeFormat.dayName` (CHF-11).
- Validation (`amountPaisa > 0`) is enforced in the repository layer (CHF-29), not the DAO.
