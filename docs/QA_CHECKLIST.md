# Cha Fund — Final QA Checklist (CHF-55)

Run before every release build. All items must pass before signing.

## Functional

- [ ] **Airplane mode** — enable airplane mode, launch app → all features work, no crash
- [ ] **Add entry** — enter amount, tap Save entry → Balance increases, snackbar confirms
- [ ] **Add expense** — select category, enter amount, tap Save expense → Spent increases, Balance decreases
- [ ] **Negative balance** — spend more than entered → Balance shows in red with `-` sign
- [ ] **Segmented toggle** — switch Entry ↔ Expense → form fields change, label changes
- [ ] **Category chips** — expense mode shows seeded Morning/Noon/Afternoon/Evening chips
- [ ] **Daily History** — bottom nav → Daily tab → shows current month, date list desc, Spent + Balance top row
- [ ] **Day Detail** — tap a date → entries + expenses grouped by category + day-spent footer
- [ ] **Edit record** — tap Edit icon → prefilled sheet → save → totals update immediately
- [ ] **Delete record** — long-press (or trash icon) → confirm dialog → record removed → totals update
- [ ] **Monthly History** — bottom nav Months → list newest first, current month has blue "current" chip
- [ ] **Tap month** — Monthly History → tap row → navigates to that month's Daily History (read-only)
- [ ] **Read-only past month** — edit/delete affordances hidden in Day Detail for past months

## Month Lifecycle

- [ ] **New month on first launch** — fresh install → current month created at Tk 0
- [ ] **Month boundary** — change device date to next month → resume app → new month shown at Tk 0, previous month in Monthly History
- [ ] **App left open overnight** — background app across midnight month change → on resume, correct month shown
- [ ] **No carryover** — new month starts at Tk 0, not previous balance

## Settings

- [ ] **Locked current month row** — Settings → current month row non-interactive with lock icon
- [ ] **Delete past month** — Settings → Delete month → lists only past months → confirm → month removed from history
- [ ] **Current month hidden** — current month never appears in delete list
- [ ] **Add Time Category** — tap `+ Add` chip → enter name → Save → appears in list and expense picker
- [ ] **Rename Time Category** — tap category chip → rename sheet → save → name updated everywhere
- [ ] **Delete in-use category** — delete a category used by expenses → error snackbar, category retained
- [ ] **Delete unused category** — delete category not in use → removed from list
- [ ] **Theme Light** — Settings → Light → app switches to light theme
- [ ] **Theme Dark** — Settings → Dark → app switches to dark theme
- [ ] **Theme System** — Settings → System → app follows OS dark/light setting
- [ ] **Theme persists** — select theme → kill app → relaunch → theme preserved

## Data Integrity

- [ ] **Cascade delete** — delete a past month → all its entries and expenses also gone
- [ ] **Idempotent month creation** — re-open app same day → no duplicate month rows

## Accessibility

- [ ] **TalkBack** — enable TalkBack, navigate Home → Daily → Day Detail → Settings → all interactive elements announced
- [ ] **Touch targets** — all buttons/chips ≥ 48dp
- [ ] **Font scaling 200%** — enable large text → UI still readable, no overflow clipping
- [ ] **Color-not-only** — negative balance shows both red color AND explicit `-` sign

## Performance

- [ ] **Home render ≤ 300ms** — cold start, observe home screen renders within 300ms
- [ ] **Add → update ≤ 200ms** — tap Save → Balance/Spent update within 200ms

## Build

- [ ] **Release APK builds** — `./gradlew assembleRelease` completes without error
- [ ] **No INTERNET permission** — `aapt dump permissions app-release.apk` shows no INTERNET
- [ ] **Unit tests pass** — `./gradlew test` — BUILD SUCCESSFUL
- [ ] **Lint passes** — `./gradlew lint` — no errors

---

*Signed off by: _________________ Date: _________________*
