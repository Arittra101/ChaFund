# CHF-41 · `MonthlyHistoryViewModel` + Screen

| Field | Value |
|---|---|
| Type | Story · P0 · 3 SP |
| Blocked By | CHF-36, CHF-26 |
| Blocks | CHF-49, CHF-51 |

## Goal
List of all months with totals; tap → Daily History for that month.

## Hints
- Package: `feature/history/presentation/monthly/`.
- `MonthlyHistoryUiState`: `months: List<MonthSummary>`, `isEmpty`.
- VM reads `repo.observeMonthSummaries()`.
- Each row: `monthLabel` · entries total · Spent · Balance — all via `MoneyText`.
- Tap → `Navigator.navigateTo(Route.DailyHistory(monthId))`.

## Acceptance Criteria
- [ ] Sorted year desc, month desc.
- [ ] Empty state when only one (current) month exists.
- [ ] Maps to FR-M1, FR-M2.
