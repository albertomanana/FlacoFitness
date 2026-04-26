# Claude Code Instructions For FlacoFitness

## Start Here

Before touching code, read in this order:

1. `docs/agents-memory/claude-code-handoff.md`
2. `docs/agents-memory/agent-working-rules.md`
3. `docs/agents-memory/claude-plan-status.md`
4. `docs/agents-memory/architecture.md`
5. `docs/agents-memory/domain-model.md`
6. `docs/agents-memory/setup-status.md`
7. `docs/agents-memory/module-status.md`
8. `docs/agents-memory/decisions-log.md`
9. `docs/agents-memory/backlog.md`

## Project

FlacoFitness is a premium SaaS-style gym management application built with:

- Spring Boot
- Spring Data JPA
- Thymeleaf
- Bootstrap 5
- JavaScript
- MySQL database `flacofitness`

## Architecture Rules

- Keep MVC architecture.
- Do not convert the app into a SPA.
- Keep Thymeleaf + Bootstrap + lightweight JS.
- Never use H2.
- Always use MySQL database `flacofitness`.
- Avoid large uncontrolled rewrites.
- Prefer safe incremental refactors.
- Reuse existing services, repositories, fragments and JS where possible.
- Always inspect `docs/agents-memory/*` before important changes.
- Always update `docs/agents-memory/*` after important changes.
- Keep the UI premium, modern, elegant and coherent.

## UX Rules

- Prefer click-on-row navigation instead of redundant action buttons.
- Keep visual hierarchy clean.
- Keep spacing consistent.
- Use animations with moderation.
- Keep dashboard readable and not overloaded.
- Keep sidebar organized and scalable.

## Finance Rules

- Payments must always use the current real PC date through `OperationalClockService`.
- Expenses and payrolls must always use the current real PC date through `OperationalClockService`.
- Avoid duplicated recurring payments or expenses.
- Financial logic must stay centralized in financial services.
- Memberships, payments, payrolls and expenses must stay coherent.
- Dashboard financial metrics must match real stored data.

## Testing Rules

- Validate CRUDs.
- Validate charts.
- Validate notifications.
- Validate dark mode.
- Validate filters.
- Validate dashboard.
- Validate MySQL persistence.
- Validate recurring expenses and payments.
- Run `.\mvnw.cmd clean -DskipTests compile` after meaningful backend/template changes.
- Run `.\mvnw.cmd test` before closing a substantial slice.

## Documentation Rules

Always update:

- `docs/agents-memory/project-overview.md`
- `docs/agents-memory/architecture.md`
- `docs/agents-memory/domain-model.md`
- `docs/agents-memory/setup-status.md`
- `docs/agents-memory/backlog.md`
- `docs/agents-memory/changelog-functional.md`
- `docs/agents-memory/decisions-log.md`
- `docs/agents-memory/prompts-history.md`

## Current Product Constraints

- Branch in use: `recovery/restore-core-saas-plan-a`
- PIN access is active.
- Shell visual blank-page bug is already fixed; do not reintroduce hidden-by-default main content.
- `OperationalClockService` no longer simulates time; it delegates to real system time.
- Do not reintroduce clock simulation or H2-based fallback profiles.
