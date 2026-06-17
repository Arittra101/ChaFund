# Cha Fund App — UI Specification

> Companion to `cha_fund_app_requirements.md`. This document defines the **exact visual design and interaction behavior** of every screen, so an AI (or developer) can reproduce the agreed prototype faithfully in Jetpack Compose + Material 3.
>
> Currency is **Tk** everywhere. The app is offline, Room-backed, and the current month is auto-detected (see requirements §3.5, §9).

---

## 1. Design Principles

- **Simple and useful first.** Minimal chrome, generous spacing, one primary action per screen.
- **Two summary numbers are king**: `Balance` (money we have) and `Spent` (total cost). They appear, paired, on Home and on Daily History.
- **Color encodes money direction**, consistently across the whole app:
  - **Blue** = balance / money held / money-in.
  - **Coral/red** = spent / money-out. Negative balance also turns red.
  - **Green** = a positive entry delta (`+Tk`).
- **Time categories are color-coded chips** so they're scannable at a glance.
- **Flat surfaces**: cards are a plain surface with a 0.5px border and rounded corners. No shadows, no gradients.
- **Light & dark mode are both first-class** (see §3 tokens).

---

## 2. Layout Foundations

| Token | Value |
|---|---|
| Screen frame | Status bar (time + wifi/battery) · top app bar · scrollable body · bottom nav |
| Card corner radius | 14dp (`large`) |
| Inner chip / metric radius | 8dp (`medium`) |
| Card border | 0.5dp, border-tertiary color |
| Card padding | 14dp |
| Body horizontal padding | 14dp |
| Metric card padding | 12dp |
| Row vertical padding | 12dp, with 0.5dp divider between rows |
| Primary button height | ~44dp, full width, filled blue, white text, weight 500 |
| Text input height | 38dp, 0.5dp border, 8dp radius |
| Section gap | 12dp between stacked cards |

Font weights: **400 regular** and **500 medium** only. No bold/700.

Type scale (sp): big number 21 · medium number 17–18 · screen title 15 · body 13–14 · label 11–12 · hint 10.

---

## 3. Color Tokens (Light / Dark)

Map these to your Material 3 theme. Hardcoded hex values below match the agreed prototype.

### Semantic / surface

| Role | Light | Dark |
|---|---|---|
| Text primary | theme default | `#F2F0EA` |
| Text secondary (muted) | theme secondary | `#B4B2A9` |
| Text hint | theme tertiary | `#888780` |
| Card / surface | white | `#201F18` |
| Page background | light gray | `#15140F` |
| Border (tertiary) | ~0.15α | ~0.14α white |

### Money colors

| Role | Fill | Text |
|---|---|---|
| Balance / blue | `#E6F1FB` (light) · `#0C447C` (dark) | `#185FA5` light · `#85B7EB` dark |
| Spent / coral | `#FAECE7` | `#993C1D` |
| Negative balance | — | `#E24B4A` |
| Positive entry delta | — | success green (theme) |

### Time-category chip colors (fill → text)

| Category | Fill | Text |
|---|---|---|
| Morning | `#E1F5EE` | `#0F6E56` |
| Noon | `#E6F1FB` | `#185FA5` |
| Afternoon | `#FAEEDA` | `#854F0B` |
| Evening | `#EEEDFE` | `#3C3489` |

> New user-created categories cycle through this same palette. Chip text uses the dark stop of its own color family — never plain black.

---

## 4. Navigation Shell

**Bottom navigation bar** — 4 destinations, always visible, active item tinted blue:

| Order | Label | Icon (Material) | Route |
|---|---|---|---|
| 1 | Home | `home` | `Route.Home` |
| 2 | Daily | `calendar` | `Route.DailyHistory(currentMonthId)` |
| 3 | Months | `chart-bar` / `bar_chart` | `Route.MonthlyHistory` |
| 4 | Settings | `settings` | `Route.Settings` |

Day Detail is **not** a tab — it's pushed from Daily, and keeps the Daily tab highlighted while open.

Top app bar: left = screen title (or back arrow + context on detail screens), right = a contextual control (lock badge on Home, month badge on Daily, Edit on Day Detail).

---

## 5. Screen Specifications

### 5.1 Home (`fund` feature)

**Purpose:** show the two summary numbers and let the user add an entry or expense in one tap.

**Top app bar**
- Left, stacked: tiny hint "Current month" (10sp) above the month label, e.g. `June 2026` (15sp, weight 500).
- Right: a small locked indicator — lock icon + the word "auto" (10–11sp, muted). The month is **fixed**; there is no dropdown, picker, or toggle (requirements FR-L5).

**Summary row** (directly below app bar, 14dp padding)
- Two equal metric cards, side by side, 10dp gap.
- Left card — **Balance**: fill blue (`#E6F1FB`), label "Balance" (11sp, blue text), value `Tk 1,240` (21sp, weight 500, blue). If balance < 0, value text turns red (`#E24B4A`).
- Right card — **Spent**: fill coral (`#FAECE7`), label "Spent" (11sp, `#993C1D`), value `Tk 760` (21sp, weight 500, `#993C1D`).

**Add card** (one surface card, 14dp padding)
1. **Segmented toggle** at top: two segments `Add entry` | `Add expense`. Selected segment = white pill on a tertiary track, weight 500; unselected = muted text. Default selected = `Add entry`.
2. **Amount field** — label "Amount (Tk)" (11sp muted), numeric input, 38dp, placeholder `0`.
3. **Mode-dependent field:**
   - *Entry mode:* label "Ref note · optional", text input, placeholder e.g. `office collection`.
   - *Expense mode:* label "Time category", then a **horizontal wrap of category chips**. Tapping selects one (selected = 2dp outline in the chip's own dark color). Required.
4. **Auto-track hint** (10sp, muted, clock icon): e.g. `16 Jun 26 · Tue · auto-tracked`. Read-only; the system captures date/day/time.
5. **Primary button** — full-width filled blue. Label switches with mode: `Save entry` / `Save expense`.

**Behavior**
- Validation: amount must be > 0, else inline toast "Enter a valid amount", no save.
- Expense mode: a category must be selected (defaults to first chip).
- On save: write to Room (bound to current month), totals re-emit and update live, show a confirmation toast (`Entry saved · +Tk 500` / `Expense saved · Tk 120`), reset the form.

---

### 5.2 Daily History (`history` feature)

**Purpose:** browse the current month's activity by date, newest first.

**Top app bar**
- Left: title "Daily history" (15sp).
- Right: current-month badge — a small blue pill, e.g. `June 2026` (11sp). This screen **auto-lands on the current month** (FR-H0); no picker. (When reached from Monthly History, the badge shows that month instead.)

**Sticky summary row** (below app bar, divider beneath)
- Two metric cards, 10dp gap: **Spent** (coral) on the left, **Balance** (blue) on the right — both 17sp values. Same negative-red rule for balance.

**Date list** (one surface card containing rows; rows divided by 0.5dp lines)
- Sorted **descending by date** (FR-H1). Each row:
  - Left: date `16 June 26` (14sp, weight 500) with a sub-line `Spent Tk 220` (11sp, coral).
  - Right: a small stack — tiny label `today's entry` (10sp hint) over `+Tk 500` (13sp, green) — followed by a chevron-right.
  - Whole row is tappable → opens **Day Detail** for that date.

> Note: the right-side figure shown is the **day's entry total** (`+Tk`). The running balance snapshot per the requirements (FR-H2) can alternatively be shown here; prototype displays the day's entry delta. Pick one and keep it consistent.

---

### 5.3 Day Detail (`history` feature)

**Purpose:** see one day's entries and category-grouped expenses; entry point to editing.

**Top app bar**
- Left: back arrow + day context, e.g. `Tue, 16 June` (15sp).
- Right: **Edit** affordance — edit icon + "Edit" (12sp, blue). Opens the edit flow (inline editing of amounts — see §6.4).

**Body** (one surface card)
1. **Entries added** section: small muted heading "Entries added", then each entry row: amount `Tk 500` (13sp) with sub-line ref + time (`office collection`), and a right-aligned green `+Tk 500`. If none: "No entries".
2. Divider.
3. **Expenses by time** section: heading "Expenses by time". For each time category present:
   - the **category chip** (colored), then under it each expense as a row: note + time on the left (12sp muted), amount on the right (`Tk 120`, 13sp, coral).
4. **Day total footer** (tertiary-background strip, bottom of card, rounded bottom corners): "Day spent" on the left, `Tk 220` (14sp, weight 500, coral) on the right.

---

### 5.4 Monthly History (`history` feature)

**Purpose:** list all months; tap to drill into that month's Daily History.

**Top app bar:** title "Monthly history".

**Month list** (one surface card, divided rows)
- Each row:
  - Left: month name `June 2026` (14sp, weight 500). The current month gets a small blue `current` chip beside it. Sub-line: `Entry Tk 700 · Spent Tk 660` (11sp, muted).
  - Right: the month's balance value (13sp, blue) + chevron-right.
  - Tap → navigates to `DailyHistory(monthId)` for that month (FR-M2).
- Order: newest month first; past months can render slightly dimmed (optional).

---

### 5.5 Settings (`settings` feature)

**Purpose:** month info (read-only), delete past months, manage categories, theme.

**Top app bar:** title "Settings".

**Card 1 — Month**
- Row "Current month" — calendar icon, label, and a sub-line `Auto-set from calendar · June 2026` (10sp hint), with a **lock** icon on the right. Read-only (no create action — months are automatic).
- Divider.
- Row "Delete month" — minus-calendar icon (danger color), label, chevron. Opens the **Delete Month sheet** (§6.3).

**Card 2 — Time categories**
- Heading row: clock icon + "Time categories".
- A wrap of existing category chips (colored), plus a dashed **"+ Add"** chip at the end to create a new one.

**Card 3 — Theme**
- Moon icon + "Theme" + a small segmented toggle `Light` | `Dark` on the right. Selecting re-themes the whole app live; persist in DataStore.

**Footnote** (centered, 10sp hint): "New months are created automatically each month. Past months stay in history."

---

## 6. Interaction Patterns

### 6.1 Toasts / snackbars
Short pill-shaped confirmations near the bottom: save confirmations, validation errors, deletion confirmations. ~1.5s auto-dismiss.

### 6.2 Segmented toggles
Used for Add entry/expense and Light/Dark. Track = tertiary background, selected segment = white pill + weight-500 text. 2–3dp padding inside the track.

### 6.3 Delete Month flow (bottom sheet → confirm dialog)
1. **Sheet** slides up: grab handle, title "Delete a month", close (×). Subtitle: "Past months only. The current month (June 2026) can't be deleted."
2. Lists **past months only** — the current month is **hidden entirely** (decision locked). Each row shows the month, its `Entry · Spent` totals, and a trash icon on the right.
3. Tap trash → **confirm dialog**: warning triangle + "Delete <Month>?", body "All entries and expenses in this month will be permanently removed. This can't be undone.", actions `Cancel` (outline) | `Delete` (filled red).
4. Confirm → row removed, toast `<Month> deleted`, sheet stays open for more deletes. Empty state when no past months: "No past months to delete yet."

> Deletion cascades at the DB level (Room `onDelete = CASCADE`); the dialog is the only safety net.

### 6.4 Edit flow (Day Detail)
Tapping **Edit** makes the day's entry and expense amounts editable inline (amount fields appear in place). Saving recalculates Balance and Spent and re-derives day/month summaries (requirements FR-ED1–ED4). Editing is scoped to that month. *(Inline-vs-separate-screen is still an open question in requirements §11; prototype assumes inline.)*

### 6.5 Empty states
- Home with no current-month data: still shows Tk 0 in both cards; form is usable.
- Daily with no dates: "No activity yet this month".
- Day Detail with no entries/expenses: per-section "No entries" / "No expenses".

---

## 7. Component Inventory (reuse from `core/presentation/components`)

| Component | Used by |
|---|---|
| `MetricCard` (label + value, color variant) | Home, Daily |
| `SegmentedToggle` (2-option) | Home (entry/expense), Settings (theme) |
| `CategoryChip` (color from category) | Home expense, Day Detail, Settings |
| `PrimaryButton` (filled blue) | Home save |
| `AmountField` (numeric, Tk) | Home, edit flow |
| `ListRowCard` / divided rows | Daily, Monthly, Settings |
| `DayDetailSection` | Day Detail |
| `ConfirmationBottomSheet` + danger dialog | Delete month |
| `Toast` / snackbar host | global |
| Bottom nav bar (4 items) | shell |

Building a new button or chip style inside a feature is a review block (per team spec rule #12).

---

## 8. Screen → Route → Feature Map

| Screen | Route | Feature |
|---|---|---|
| Home | `Route.Home` | `fund` |
| Daily History | `Route.DailyHistory(monthId)` | `history` |
| Day Detail | `Route.DayDetail(monthId, dateEpoch)` | `history` |
| Monthly History | `Route.MonthlyHistory` | `history` |
| Settings | `Route.Settings` | `settings` |

---

## 9. Visual Quick-Reference (per screen)

**Home:** locked month label · [Balance blue | Spent coral] · entry/expense toggle · amount + (ref / category chips) · auto-track hint · blue save button.

**Daily:** title + month badge · sticky [Spent coral | Balance blue] · descending date rows (date + day-spent + entry delta + chevron).

**Day Detail:** back + day · Edit · entries list · category-grouped expenses · day-spent footer.

**Monthly:** month rows (name + current chip + Entry·Spent + balance + chevron).

**Settings:** current-month (locked) + delete month · category chips + add · theme toggle · auto-month footnote.
