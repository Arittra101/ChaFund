# CHF-55 · Final QA pass

| Field | Value |
|---|---|
| Type | Task · P0 · 3 SP |
| Blocked By | CHF-49, CHF-50, CHF-51, CHF-52, CHF-54 |
| Blocks | — (release) |

## Goal
Execute the manual QA checklist from PRD §15.4 and sign off for release.

## Manual Checklist
- [ ] Airplane mode → app fully usable
- [ ] Change device date to next month → on resume, new month at Tk 0
- [ ] Negative balance renders red
- [ ] Theme switch persists across cold start
- [ ] Delete category in use → blocked with message
- [ ] Past month entries cannot be edited
- [ ] Cascade delete removes entries + expenses on month delete
- [ ] Add/edit/delete in current month updates Home in ≤ 200 ms
- [ ] Cold start ≤ 1.5s on mid-range device
- [ ] Default Time Categories seeded on fresh install

## Acceptance Criteria
- [ ] All checklist items pass.
- [ ] Release build signed and ready for distribution.
- [ ] No P0/P1 bugs open.
