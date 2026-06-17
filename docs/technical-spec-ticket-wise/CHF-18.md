# CHF-18 · `LocalStorage` + `DataStoreLocalStorage` + `storageModule`

| Field | Value |
|---|---|
| Type | Task |
| Priority | P1 |
| Estimate | 2 SP |
| Epic | CHF-7 Core Infrastructure |
| Blocked By | CHF-2 |
| Blocks | CHF-43, CHF-47 |

---

## Goal
Abstract typed preference reads/writes behind `LocalStorage`. Backed by Jetpack DataStore Preferences. For v1, only **theme mode** is stored.

---

## Files to add

| Path | Action |
|---|---|
| `core/data/storage/LocalStorage.kt` | Create — interface |
| `core/data/storage/DataStoreLocalStorage.kt` | Create — impl |
| `core/di/storageModule.kt` | Create |
| `core/di/appModules.kt` | Modify — add `storageModule` |

---

## Implementation

### `LocalStorage.kt`
```kotlin
package com.chafund.core.data.storage

import com.chafund.theme.ThemeMode
import kotlinx.coroutines.flow.Flow

interface LocalStorage {
    val themeMode: Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
}
```

### `DataStoreLocalStorage.kt`
```kotlin
package com.chafund.core.data.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.chafund.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "chafund_prefs")
private val KEY_THEME = stringPreferencesKey("theme_mode")

class DataStoreLocalStorage(private val ctx: Context) : LocalStorage {

    override val themeMode: Flow<ThemeMode> =
        ctx.dataStore.data.map { prefs ->
            prefs[KEY_THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM
        }

    override suspend fun setThemeMode(mode: ThemeMode) {
        ctx.dataStore.edit { it[KEY_THEME] = mode.name }
    }
}
```

### `storageModule.kt`
```kotlin
package com.chafund.core.di

import com.chafund.core.data.storage.DataStoreLocalStorage
import com.chafund.core.data.storage.LocalStorage
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val storageModule = module {
    single<LocalStorage> { DataStoreLocalStorage(androidContext()) }
}
```

### `appModules.kt`
```kotlin
fun appModules(): List<Module> = listOf(
    databaseModule,
    storageModule,
    // sessionModule, utilsModule, features...
)
```

---

## Acceptance Criteria
- [ ] `LocalStorage` interface exposed; impl is internal to `core/data/storage/`.
- [ ] Default value `ThemeMode.SYSTEM` when no preference set.
- [ ] Invalid stored value falls back to `SYSTEM` (no crash on schema drift).
- [ ] Setting persists across cold start.

---

## Testing
- Instrumentation:
  - `setThemeMode(DARK)` → next `themeMode.first()` returns `DARK`.
  - Clear DataStore → emission falls back to `SYSTEM`.
- Unit (with fake `DataStore`): default and round-trip.

---

## Notes
- Only `theme_mode` is stored in v1. Additional keys (e.g., onboarding-shown) should be added here under explicit typed accessors, not as a generic key-value bag.
