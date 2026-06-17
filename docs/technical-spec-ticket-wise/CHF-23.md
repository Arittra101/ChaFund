# CHF-23 · `Route` sealed interface (typed, `@Serializable`)

| Field | Value |
|---|---|
| Type | Task · P0 · 2 SP |
| Blocked By | CHF-2 |
| Blocks | CHF-24 |

## Goal
Type-safe route definitions for Navigation Compose, no auth.

## Hints
- File: `navigation/Route.kt`.
- `sealed interface Route` with: `Home` (object), `DailyHistory(monthId: Long)`, `DayDetail(monthId: Long, dateEpoch: Long)`, `MonthlyHistory` (object), `Settings` (object).
- Annotate each with `@Serializable`. Ensure `kotlinx-serialization` plugin applied.

## Acceptance Criteria
- [ ] All 5 routes serializable.
- [ ] No auth/guard routes.
