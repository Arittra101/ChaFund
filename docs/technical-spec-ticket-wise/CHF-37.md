# CHF-37 · `DailyHistoryViewModel` + Screen

| Field | Value |
|---|---|
| Type | Story · P0 · 5 SP |
| Blocked By | CHF-26, CHF-36 |
| Blocks | CHF-38 |

## Goal
Daily History screen showing date list desc with `Spent | Balance` top bar.

## Hints
- Package: `feature/history/presentation/daily/`.
- Route arg `monthId`. If absent (from bottom nav) → fall back to `session.currentMonthId.value`.
- `DailyHistoryUiState`: `monthLabel`, `isReadOnly`, `spent`, `balance`, `days: List<DailySummary>`, `isEmpty`.
- VM combines `observeMonth(monthId)` + `observeMonthSummary(monthId)` + `observeDailySummaries(monthId)`.
- Past month → `isReadOnly = true`, show read-only badge.
- Tap row → `Navigator.navigateTo(Route.DayDetail(monthId, dateEpoch))`.

## Acceptance Criteria
- [ ] From bottom nav, lands on current month.
- [ ] From Monthly History, shows selected month.
- [ ] Empty state when no activity.
- [ ] Maps to PRD FR-H0…FR-H4.
