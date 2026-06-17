# CHF-17 · `databaseModule` (Koin) — DB + DAO providers

| Field | Value |
|---|---|
| Type | Task |
| Priority | P0 |
| Estimate | 2 SP |
| Epic | CHF-7 Core Infrastructure |
| Blocked By | CHF-13, CHF-14, CHF-15, CHF-16 |
| Blocks | CHF-29, CHF-36, CHF-43 |

---

## Goal
Expose `ChaFundDb` and all four DAOs through Koin as `single` instances. Attach `SeedCallback` to the DB builder.

---

## Files to add / modify

| Path | Action |
|---|---|
| `core/di/databaseModule.kt` | Create |
| `core/di/appModules.kt` | Modify — add `databaseModule` |

---

## Implementation

### `databaseModule.kt`
```kotlin
package com.chafund.core.di

import androidx.room.Room
import com.chafund.core.data.database.ChaFundDb
import com.chafund.core.data.database.seed.SeedCallback
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            ChaFundDb::class.java,
            "chafund.db",
        )
            .addCallback(SeedCallback())
            // .addMigrations(MIGRATION_1_2) // future
            .build()
    }
    single { get<ChaFundDb>().monthDao() }
    single { get<ChaFundDb>().timeCategoryDao() }
    single { get<ChaFundDb>().entryDao() }
    single { get<ChaFundDb>().expenseDao() }
}
```

### `appModules.kt`
```kotlin
fun appModules(): List<Module> = listOf(
    databaseModule,
    // storageModule,   // CHF-18
    // sessionModule,   // CHF-19
    // utilsModule,     // CHF-21
    // ...features
)
```

---

## Acceptance Criteria
- [ ] `ChaFundDb` is a Koin `single`.
- [ ] All four DAOs resolvable from any module via `get<MonthDao>()`, etc.
- [ ] `SeedCallback` attached → default Time Categories present on first launch.
- [ ] **No `fallbackToDestructiveMigration()` call** anywhere.

---

## Testing
- Koin module sanity test: `verify { module<MonthDao>() }` (Koin's module test helper) resolves all DAOs.
- Manual: install app, observe seeded categories appear in expense category picker (after CHF-33).

---

## Notes
- If migration objects are added later, list them via `.addMigrations(MIGRATION_1_2, MIGRATION_2_3, ...)` — never enable destructive fallback in release.
