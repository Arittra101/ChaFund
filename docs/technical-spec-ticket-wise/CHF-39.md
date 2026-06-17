# CHF-39 · Edit Entry / Expense sheets

| Field | Value |
|---|---|
| Type | Story · P0 · 5 SP |
| Blocked By | CHF-38 |
| Blocks | CHF-40 |

## Goal
Inline bottom sheets prefilled with current values; same validation as add flows.

## Hints
- Packages: `feature/history/presentation/daydetail/edit{entry,expense}/`.
- `EditEntryViewModel`, `EditExpenseViewModel` — load via id at init.
- UiState mirrors add-flow state + `recordId`.
- Date/time editable here (only place).
- On save → `repo.updateEntry` / `updateExpense`.
- Block if record's monthId ≠ current — surface error toast.

## Acceptance Criteria
- [ ] Prefill correct.
- [ ] Save updates record + recomputes day/month totals via Flow.
- [ ] Past-month edit blocked by repository.
- [ ] Maps to FR-ED1…FR-ED4.
