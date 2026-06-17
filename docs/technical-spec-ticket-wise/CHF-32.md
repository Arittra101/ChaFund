# CHF-32 · Add Entry sheet

| Field | Value |
|---|---|
| Type | Story · P0 · 3 SP |
| Blocked By | CHF-31 |
| Blocks | CHF-33 |

## Goal
Modal bottom sheet to add a money entry to the current month.

## Hints
- Package: `feature/fund/presentation/addentry/`.
- `AddEntryUiState`: `amountInput`, `refInput`, `amountError`, `isSaving`, `saveEnabled`.
- `AddEntryUiEvent`: `OnAmountChange(String)`, `OnRefChange(String)`, `OnSave`, `OnDismiss`.
- Validation: amount > 0 (parse via `Money.fromTkString`); ref ≤ 80 chars.
- On `OnSave` → `repo.addEntry(...)` → emit `UiEffect.Dismiss` + snackbar `"Entry added"`.
- Use `ModalBottomSheet` (Material 3).

## Acceptance Criteria
- [ ] Save disabled until amount valid.
- [ ] Inline error for amount ≤ 0.
- [ ] Home Balance updates within 200 ms after save.
