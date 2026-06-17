# CHF-16 · `ExpenseEntity` + `ExpenseDao`

| Field | Value |
|---|---|
| Type | Task |
| Priority | P0 |
| Estimate | 3 SP |
| Epic | CHF-7 Core Infrastructure |
| Blocked By | CHF-13, CHF-14 |
| Blocks | CHF-17 |

---

## Goal
Persist money-out records tagged to a Time Category. FK to Month cascades; FK to TimeCategory **restricts** delete (blocks in-use category removal).

---

## Files to add

| Path | Action |
|---|---|
| `core/data/database/entity/ExpenseEntity.kt` | Create |
| `core/data/database/relation/ExpenseWithCategory.kt` | Create |
| `core/data/database/dao/ExpenseDao.kt` | Create |
| `ChaFundDb.kt` | Modify |

---

## Implementation

### `ExpenseEntity.kt`
```kotlin
package com.chafund.core.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Expense",
    foreignKeys = [
        ForeignKey(
            entity = MonthEntity::class,
            parentColumns = ["id"], childColumns = ["monthId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TimeCategoryEntity::class,
            parentColumns = ["id"], childColumns = ["timeCategoryId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index("monthId"),
        Index("date"),
        Index("timeCategoryId"),
    ],
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val monthId: Long,
    val timeCategoryId: Long,
    val amountPaisa: Long,
    val ref: String?,
    val date: Long,               // epoch-day
    val time: String,             // "HH:mm"
    val createdAt: Long,
    val updatedAt: Long,
)
```

### `ExpenseWithCategory.kt`
```kotlin
package com.chafund.core.data.database.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.chafund.core.data.database.entity.ExpenseEntity
import com.chafund.core.data.database.entity.TimeCategoryEntity

data class ExpenseWithCategory(
    @Embedded val expense: ExpenseEntity,
    @Relation(parentColumn = "timeCategoryId", entityColumn = "id")
    val category: TimeCategoryEntity,
)
```

### `ExpenseDao.kt`
```kotlin
package com.chafund.core.data.database.dao

import androidx.room.*
import com.chafund.core.data.database.entity.ExpenseEntity
import com.chafund.core.data.database.relation.ExpenseWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM Expense WHERE monthId = :monthId ORDER BY date DESC, time DESC, id DESC")
    fun observeByMonth(monthId: Long): Flow<List<ExpenseEntity>>

    @Transaction
    @Query("""
        SELECT * FROM Expense
        WHERE monthId = :monthId AND date = :date
        ORDER BY time DESC, id DESC
    """)
    fun observeByDate(monthId: Long, date: Long): Flow<List<ExpenseWithCategory>>

    @Query("SELECT IFNULL(SUM(amountPaisa), 0) FROM Expense WHERE monthId = :monthId")
    fun sumByMonth(monthId: Long): Flow<Long>

    @Query("SELECT COUNT(*) FROM Expense WHERE timeCategoryId = :categoryId")
    suspend fun countByCategory(categoryId: Long): Int

    @Insert
    suspend fun insert(expense: ExpenseEntity): Long

    @Update
    suspend fun update(expense: ExpenseEntity): Int

    @Query("DELETE FROM Expense WHERE id = :id")
    suspend fun deleteById(id: Long): Int
}
```

### `ChaFundDb.kt`
```kotlin
@Database(
    entities = [
        MonthEntity::class,
        TimeCategoryEntity::class,
        EntryEntity::class,
        ExpenseEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(ChaFundTypeConverters::class)
abstract class ChaFundDb : RoomDatabase() {
    abstract fun monthDao(): MonthDao
    abstract fun timeCategoryDao(): TimeCategoryDao
    abstract fun entryDao(): EntryDao
    abstract fun expenseDao(): ExpenseDao
}
```

---

## Acceptance Criteria
- [ ] FK `monthId → Month CASCADE`, FK `timeCategoryId → TimeCategory RESTRICT`.
- [ ] Indices on `monthId`, `date`, `timeCategoryId`.
- [ ] `countByCategory` returns accurate row count.
- [ ] Deleting Month cascades expenses for that month.
- [ ] Deleting a category referenced by any expense throws (RESTRICT).

---

## Testing
- DAO test:
  - Insert 5 expenses across two categories → `countByCategory(catA)`, `countByCategory(catB)` correct.
  - `observeByDate` returns expenses joined with category name.
  - Delete in-use category → `SQLiteConstraintException`.
  - Delete parent Month → all related expenses gone.
  - `sumByMonth` correct after insert/update/delete.

---

## Notes
- Repository pre-checks `countByCategory` to surface a friendly error before the FK throws (CHF-43).
- After this ticket merges, `MonthDao.observeMonthSummaries` (CHF-13) becomes fully functional since both `Entry` and `Expense` tables now exist.
