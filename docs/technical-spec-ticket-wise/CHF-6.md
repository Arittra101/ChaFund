# CHF-6 · Single `MainActivity` + empty `AppNavHost`

| Field | Value |
|---|---|
| Type | Task |
| Priority | P0 |
| Estimate | 2 SP |
| Epic | CHF-1 Foundation & Setup |
| Blocked By | CHF-3, CHF-5 |
| Blocks | CHF-24 (will be populated) |

---

## Goal
A single `ComponentActivity` hosting an `AppNavHost` placeholder. Confirms the foundation builds end-to-end before navigation routes (CHF-23) and shared UI (CHF-26) arrive.

---

## Files to add

| Path | Action |
|---|---|
| `MainActivity.kt` | Create |
| `navigation/AppNavHost.kt` | Create (placeholder) |

---

## Implementation

### `MainActivity.kt`
```kotlin
package com.chafund

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.chafund.navigation.AppNavHost
import com.chafund.theme.ChaFundTheme
import com.chafund.theme.ThemeMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChaFundTheme(themeMode = ThemeMode.SYSTEM) {
                AppNavHost()
            }
        }
    }
}
```

> Theme will be driven by `LocalStorage` flow once CHF-18 + CHF-47 land — for now hard-code `SYSTEM`.

### `navigation/AppNavHost.kt` (placeholder)
```kotlin
package com.chafund.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun AppNavHost() {
    Surface(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Cha Fund")
        }
    }
}
```

### `AndroidManifest.xml`
```xml
<activity
    android:name=".MainActivity"
    android:exported="true"
    android:theme="@style/Theme.ChaFund">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

---

## Acceptance Criteria
- [ ] App launches to a blank "Cha Fund" Compose surface.
- [ ] No Fragment used (single-Activity rule).
- [ ] `AndroidManifest.xml` does **not** declare `<uses-permission android:name="android.permission.INTERNET"/>`.
- [ ] No crash on cold start; no Koin resolution errors.

---

## Testing
- Manual: launch in emulator + airplane mode → still launches.
- UI smoke: `composeTestRule.onNodeWithText("Cha Fund").assertExists()`.

---

## Notes
- `AppNavHost` will be replaced by the real typed navigation graph in CHF-24.
- No `WindowCompat.setDecorFitsSystemWindows(...)` polishing in this ticket — defer to release-prep epic if needed.
