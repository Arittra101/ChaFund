# CHF-25 · `ChaFundBottomBar`

| Field | Value |
|---|---|
| Type | Task · P1 · 2 SP |
| Blocked By | CHF-24 |
| Blocks | (Home/Daily/Monthly/Settings screens) |

## Goal
Material 3 `NavigationBar` with 4 destinations synced to backstack.

## Hints
- File: `bottombar/ChaFundBottomBar.kt`.
- Read `currentBackStackEntryAsState()`; compare destination route to each `TopLevelDestination`.
- On click → `Navigator.navigateToTopLevel(dest)`.
- Use Material 3 icons (Home, List, CalendarMonth, Settings).

## Acceptance Criteria
- [ ] Correct icon highlighted for current top-level route.
- [ ] Touch targets ≥ 48dp.
- [ ] `contentDescription` set per item.
