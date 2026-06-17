# CHF-5 · Create `App` (Application) with `startKoin` bootstrap

| Field | Value |
|---|---|
| Type | Task |
| Priority | P0 |
| Estimate | 2 SP |
| Epic | CHF-1 Foundation & Setup |
| Blocked By | CHF-2, CHF-4 |
| Blocks | CHF-6, CHF-20 |

---

## Goal
Provide a single `Application` subclass that initializes Timber + Koin. `appModules()` is an aggregator (initially empty) so every future Koin module wires in via a single edit.

---

## Files to add / modify

| Path | Action |
|---|---|
| `App.kt` | Create |
| `core/di/appModules.kt` | Create — empty aggregator |
| `AndroidManifest.xml` | Modify — `android:name=".App"` |

---

## Implementation

### `core/di/appModules.kt`
```kotlin
package com.chafund.core.di

import org.koin.core.module.Module

fun appModules(): List<Module> = listOf(
    // databaseModule,   // CHF-17
    // storageModule,    // CHF-18
    // sessionModule,    // CHF-19
    // utilsModule,      // CHF-21
    // fundModule,       // CHF-30
    // historyModule,    // CHF-36
    // settingsModule,   // CHF-43
)
```

### `App.kt`
```kotlin
package com.chafund

import android.app.Application
import com.chafund.BuildConfig
import com.chafund.core.di.appModules
import com.chafund.core.utils.AppLogger
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        AppLogger.init(BuildConfig.DEBUG)
        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(appModules())
        }
        // MonthManager lifecycle observer registered in CHF-20.
    }
}
```

### `AndroidManifest.xml`
```xml
<application
    android:name=".App"
    android:allowBackup="false"       <!-- CHF-54 will lock this -->
    android:label="@string/app_name"
    android:theme="@style/Theme.ChaFund">
    ...
</application>
```

---

## Acceptance Criteria
- [ ] `App` registered in manifest.
- [ ] Koin starts without crash on cold launch.
- [ ] `appModules()` aggregator exists; later tickets uncomment/add entries here.
- [ ] No `INTERNET` permission declared.

---

## Testing
- Manual: launch app; Logcat shows Koin start log.
- Unit: a stub `KoinTest` boots with empty modules (sanity check optional).

---

## Notes
- Keep `Application.onCreate` thin. Heavier init (e.g., `MonthManager.detectAndPromote`) belongs in lifecycle hooks, not here.
