# CHF-3 · Set up ChaFundTheme (Material 3, Light/Dark/System)

| Field | Value |
|---|---|
| Type | Task |
| Priority | P1 |
| Estimate | 2 SP |
| Epic | CHF-1 Foundation & Setup |
| Blocked By | CHF-2 |
| Blocks | CHF-6, CHF-26 |

---

## Goal
Provide the Material 3 theme wrapper used by every screen. Support **Light**, **Dark**, and **Follow System** modes; **dynamic color disabled** for a consistent brand identity.

---

## Files to add

| Path | Action |
|---|---|
| `theme/ChaFundTheme.kt` | Create |
| `theme/Color.kt` | Create |
| `theme/Type.kt` | Create |

---

## Implementation

### `Color.kt`
```kotlin
package com.chafund.theme

import androidx.compose.ui.graphics.Color

// Brand
val Teal40   = Color(0xFF00897B)
val Teal80   = Color(0xFF4DB6AC)
val OnTeal   = Color(0xFFFFFFFF)

// Neutrals
val Grey10   = Color(0xFF1A1C1E)
val Grey90   = Color(0xFFF5F5F5)

// Semantic
val NegativeRed = Color(0xFFD32F2F)
```

### `Type.kt`
Standard Material 3 typography; override `displaySmall` weight for large money figures if design dictates.

### `ChaFundTheme.kt`
```kotlin
package com.chafund.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

enum class ThemeMode { LIGHT, DARK, SYSTEM }

private val LightColors = lightColorScheme(
    primary = Teal40,
    onPrimary = OnTeal,
    error = NegativeRed,
)

private val DarkColors = darkColorScheme(
    primary = Teal80,
    onPrimary = Grey10,
    error = NegativeRed,
)

@Composable
fun ChaFundTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.LIGHT  -> false
        ThemeMode.DARK   -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography  = AppTypography,
        content     = content,
    )
}
```

---

## Acceptance Criteria
- [ ] `ChaFundTheme(themeMode)` wraps content in `MaterialTheme`.
- [ ] No dynamic color (no `dynamicLightColorScheme`/`dynamicDarkColorScheme`).
- [ ] Dark mode contrast meets WCAG AA for body text.
- [ ] `NegativeRed` available on `colorScheme.error` for the negative-balance render path.

---

## Testing
- `@Preview` for both Light + Dark variants on a sample screen.
- Manual: toggle system dark mode → app follows when `SYSTEM` selected.

---

## Notes
- Final palette is placeholder until design hands off; structure remains the same.
- Brand color suggestion: teal (money / freshness). Confirm with design before locking.
