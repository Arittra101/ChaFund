# CHF-54 · Manifest + R8 + accessibility hardening

| Field | Value |
|---|---|
| Type | Task · P0 · 3 SP |
| Blocked By | CHF-47 |
| Blocks | CHF-55 |

## Goal
Lock privacy posture, set up R8 rules, and audit accessibility before release.

## Hints
- `AndroidManifest.xml`: no `INTERNET` permission; `android:allowBackup="false"`; no `requestLegacyExternalStorage`.
- R8 / ProGuard:
  - Keep Room `@Entity` classes (generated DAO references).
  - Keep `@Serializable` classes (kotlinx.serialization needs them).
  - Keep Koin reflection-targeted classes if any.
- Accessibility audit:
  - `contentDescription` on all icon-only buttons.
  - Min touch target 48dp (use `IconButton` or `Modifier.minimumInteractiveComponentSize()`).
  - Color is never the sole indicator (negative balance has `-` sign).
  - Test font scaling at 200%.
  - TalkBack pass through Home → Daily → Day Detail → Settings.

## Acceptance Criteria
- [ ] Zero non-default permissions in manifest.
- [ ] Release APK builds + installs.
- [ ] Lint accessibility checks pass.
- [ ] Screen reader pass documented.
