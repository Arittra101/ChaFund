# CHF-24 · `Navigator` façade + `TopLevelDestination`

| Field | Value |
|---|---|
| Type | Task · P0 · 3 SP |
| Blocked By | CHF-23 |
| Blocks | CHF-25 |

## Goal
Single navigation façade. `AppNavHost` consumes navigation events as `SharedFlow`.

## Hints
- `navigation/Navigator.kt`: interface with `navEvents: SharedFlow<NavEvent>`, `navigateTo(Route)`, `navigateToTopLevel(TopLevelDestination)`, `navigateBack()`.
- `NavigatorImpl` registered as Koin `single`.
- `navigation/TopLevelDestination.kt`: enum with Home, Daily, Months, Settings (icon + Route).
- Update `AppNavHost` to consume events via `LaunchedEffect` + `NavController`.
- Use typed `composable<Route.Xxx>(...)` from Navigation Compose.

## Acceptance Criteria
- [ ] No `protectedRoutes` map, no `onAuthRequired`.
- [ ] Navigator injectable from any ViewModel.
- [ ] Top-level nav clears intermediate backstack.
