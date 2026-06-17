# CHF-51 · Compose UI tests for golden paths

| Field | Value |
|---|---|
| Type | Task · P2 · 5 SP |
| Blocked By | CHF-33, CHF-41 |
| Blocks | CHF-55 |

## Goal
Compose semantics tests for critical user flows.

## Hints
- Use `createAndroidComposeRule<MainActivity>()` with Koin module overrides (fake repos).
- Flows to cover:
  - Add Entry → Balance updates on Home
  - Add Expense → Spent updates; category required validation
  - Home → Daily History → Day Detail navigation
  - Past month → edit/delete affordances hidden
  - Theme switch → theme applied
- Use `onNodeWithText`, `onNodeWithTag`, `assertIsDisplayed`, `performClick`.
- Set `testTag` on key composables.

## Acceptance Criteria
- [ ] All golden paths green.
- [ ] Tests run in CI.
