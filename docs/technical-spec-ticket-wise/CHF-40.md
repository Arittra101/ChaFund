# CHF-40 · Delete entry/expense with confirmation

| Field | Value |
|---|---|
| Type | Story · P1 · 3 SP |
| Blocked By | CHF-39 |
| Blocks | — |

## Goal
Confirmation flow for deleting a single entry/expense.

## Hints
- Reuse `ConfirmationBottomSheet` (CHF-26).
- Long-press on `DayDetail` row → set `pendingDeleteId` in UiState → show confirm.
- On confirm → `repo.deleteEntry(id)` / `deleteExpense(id)`.
- Optional v1: snackbar with **Undo** action (re-insert from cached record).

## Acceptance Criteria
- [ ] Confirm dialog required.
- [ ] Totals update immediately after delete.
- [ ] Maps to FR-ED3.
