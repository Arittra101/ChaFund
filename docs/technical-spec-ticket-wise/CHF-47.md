# CHF-47 · Theme selection (Light / Dark / Follow System)

| Field | Value |
|---|---|
| Type | Story · P2 · 2 SP |
| Blocked By | CHF-44 |
| Blocks | CHF-49, CHF-54 |

## Goal
Three-option theme selector persisted via `LocalStorage`.

## Hints
- Radio group of `ThemeMode` enum.
- VM observes `repo.themeFlow` → `selectedMode`. On select → `repo.setTheme(mode)`.
- Inject the active mode into `ChaFundTheme` at `MainActivity` level (collect from a root `AppThemeViewModel` or read directly via Koin in `MainActivity`).

## Acceptance Criteria
- [ ] Selection survives cold restart.
- [ ] System mode follows OS dark/light live.
- [ ] Maps to FR-S4, US-E2.
