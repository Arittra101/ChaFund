# CHF-45 · Delete Past Month flow

| Field | Value |
|---|---|
| Type | Story · P1 · 3 SP |
| Blocked By | CHF-44 |
| Blocks | — |

## Goal
List past months (current excluded) with destructive delete + confirm.

## Hints
- Package: `feature/settings/presentation/deletemonth/`.
- `DeleteMonthUiState`: `months: List<MonthSummary>`, `pendingDeleteId: Long?`, `isEmpty`.
- VM reads `repo.observePastMonths()` joined with summaries.
- Row icon: trash; tap → confirm sheet ("Delete June 2026? Permanent.")
- On confirm → `repo.deletePastMonth(id)` → cascade removes entries/expenses.

## Acceptance Criteria
- [ ] Current month never appears.
- [ ] Confirm dialog required.
- [ ] Cascade verified via integration test.
- [ ] Maps to FR-S2, US-E1.
