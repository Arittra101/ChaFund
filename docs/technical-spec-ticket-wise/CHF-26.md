# CHF-26 · Shared composables

| Field | Value |
|---|---|
| Type | Task · P1 · 5 SP |
| Blocked By | CHF-3, CHF-10 |
| Blocks | CHF-31, CHF-37, CHF-41, CHF-44 |

## Goal
Reusable UI primitives under `core/presentation/components/`.

## Components
- **PrimaryButton** — Material 3 `Button` with consistent padding.
- **EmptyView** — icon + message text.
- **ConfirmationBottomSheet** — title + body + Confirm/Cancel actions.
- **PillTag** — small rounded label (e.g., time category).
- **MoneyText** — renders `Money` with `Tk` prefix; auto-red on negative via `colorScheme.error`.
- **SummaryCard** — Balance + Spent layout used on Home and Daily top bar.
- **LockedMonthBadge** — month label + lock icon, non-interactive.

## Hints
- Each component: `@Composable` + `@Preview` (Light & Dark).
- `MoneyText`: takes `Money`, `style`, optional `negativeStyle` override.
- Pass `modifier: Modifier = Modifier` first param.

## Acceptance Criteria
- [ ] Each component has a `@Preview`.
- [ ] `MoneyText` shows `-` and red on negative.
- [ ] Components don't depend on any feature module.
