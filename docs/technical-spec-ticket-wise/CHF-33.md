# CHF-33 · Add Expense sheet

| Field | Value |
|---|---|
| Type | Story · P0 · 5 SP |
| Blocked By | CHF-32 |
| Blocks | CHF-49, CHF-51 |

## Goal
Modal bottom sheet to add an expense with required Time Category.

## Hints
- Package: `feature/fund/presentation/addexpense/`.
- `AddExpenseUiState`: `amountInput`, `selectedCategoryId`, `categories: List<TimeCategory>`, `refInput`, errors, `saveEnabled`.
- `AddExpenseUiEvent`: `OnAmountChange`, `OnCategorySelect(Long)`, `OnRefChange`, `OnSave`, `OnDismiss`.
- Category dropdown populated from `repo.observeTimeCategories()`.
- Validation: amount > 0, category selected.
- On save → `repo.addExpense(...)` → snackbar `"Expense added"`.

## Acceptance Criteria
- [ ] Category dropdown shows seeded defaults on first launch.
- [ ] Inline errors for both amount and category.
- [ ] Balance decreases, Spent increases after save.
