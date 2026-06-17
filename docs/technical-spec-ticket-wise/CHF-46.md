# CHF-46 · Time Categories CRUD screen

| Field | Value |
|---|---|
| Type | Story · P1 · 5 SP |
| Blocked By | CHF-44 |
| Blocks | — |

## Goal
Add / rename / delete time categories with in-use guard.

## Hints
- Package: `feature/settings/presentation/categories/`.
- `CategoriesUiState`: `categories: List<TimeCategory>`, `editing: Editing?`, `nameInput`, `nameError`.
- Events: `OnAddClicked`, `OnRenameClicked(id)`, `OnDeleteClicked(id)`, `OnNameChange`, `OnConfirm`, `OnDismiss`.
- Add via FAB → modal sheet with `OutlinedTextField`.
- Delete in use → repository returns error; surface dialog "Used by N expenses".
- Validation: name non-empty, unique case-insensitive.

## Acceptance Criteria
- [ ] Add/rename/delete each work.
- [ ] In-use delete blocked with count message.
- [ ] Maps to FR-T1…FR-T3, US-D1, US-D2.
