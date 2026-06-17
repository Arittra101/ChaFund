# CHF-52 · ktlint + detekt + Android Lint + CI

| Field | Value |
|---|---|
| Type | Task · P2 · 3 SP |
| Blocked By | CHF-2 |
| Blocks | CHF-55 |

## Goal
Enforce code quality with three tools and gate via CI.

## Hints
- Add Gradle plugins: `org.jlleitschuh.gradle.ktlint`, `io.gitlab.arturbosch.detekt`.
- `detekt-config.yml` at root — Android-friendly defaults.
- Custom detekt rule (or grep in CI) forbidding `android.util.Log` use outside `AppLogger`.
- Android Lint baseline: only if needed; require comment justifying each.
- CI (GitHub Actions or equivalent): run `./gradlew lint detekt ktlintCheck test` on PR.

## Acceptance Criteria
- [ ] All three pass on `main`.
- [ ] CI workflow committed.
- [ ] No raw `Log.x(...)` in codebase.
