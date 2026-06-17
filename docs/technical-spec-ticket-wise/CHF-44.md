# CHF-44 · `SettingsScreenRoot/Screen` with locked current-month row

| Field | Value |
|---|---|
| Type | Story · P1 · 3 SP |
| Blocked By | CHF-26, CHF-43 |
| Blocks | CHF-45, CHF-46, CHF-47 |

## Goal
Settings landing screen with locked current-month row + entry points.

## Hints
- Package: `feature/settings/presentation/settings/`.
- `SettingsUiState`: `currentMonthLabel`, `themeMode`, `versionName`.
- Layout: `LockedMonthBadge` at top, then rows:
  - "Delete Month" → navigate to delete-month sub-screen
  - "Time Categories" → navigate to categories sub-screen
  - "Theme" → navigate to theme sub-screen (or inline radio dialog)
  - "About" — version + "100% offline" tagline
- Use `ListItem` from Material 3.

## Acceptance Criteria
- [ ] Current month row non-interactive with lock icon.
- [ ] About shows `BuildConfig.VERSION_NAME`.
- [ ] Maps to FR-S1.
