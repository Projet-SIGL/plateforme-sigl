# Contributing

This file is the **single source of truth** for the team's Git and GitHub conventions.
It applies to humans and AI assistants alike (see `AGENTS.md`).
Any change to these conventions goes through a pull request reviewed by the team.

The workflow follows the **GitHub Flow** required by the project brief:
one short-lived branch per issue, a reviewed pull request, a merge into the protected `main`.

---

## 1. Repository layout

A **monorepo** organized by folders:

```
.
├── frontend/              # React client (TypeScript, Vite)
├── services/
│   └── <service-name>/    # One folder per Spring Boot microservice (gateway included)
├── infra/                 # Docker Compose, Azure infrastructure files
├── docs/
│   └── uml/               # UML diagram sources (editable)
├── .github/               # Issue and PR templates, GitHub Actions workflows
├── .claude/               # Claude Code configuration
├── AGENTS.md              # Instructions for AI assistants
└── CONTRIBUTING.md        # This file
```

Folder names under `services/` match the microservice breakdown in the
specification document (Part 2 §1).

**Why a monorepo**: with four people, a single GitHub Projects board, a single set of
conventions and a single release per sprint (v1, v2, v3) are easier to manage.
A change spanning several services (e.g. a new event) stays a single pull request.
**Trade-off**: CI must filter by folder (`paths:` in workflows) so that only the
modified services are built and tested.

---

## 2. Issues

- **No issue, no work**: every branch starts from an assigned issue.
- Every issue is added to the GitHub Projects board and to the current sprint.
- Board columns: `To do` → `In progress` → `In review` → `Done`.
- Use a template: **User story**, **Bug** or **Specification section**.
- Issue titles are written in English.

---

## 3. Branch naming

Format: `<type>/<issue-number>-<short-description>`

| Type       | Usage                                       |
|------------|---------------------------------------------|
| `feature`  | New feature (user story)                    |
| `fix`      | Bug fix                                     |
| `hotfix`   | Urgent fix of a released version            |
| `docs`     | Documentation, specification, UML diagrams  |
| `refactor` | Restructuring without behavior change       |
| `test`     | Adding or fixing tests only                 |
| `ci`       | GitHub Actions, Docker, deployment          |
| `chore`    | Maintenance (dependencies, configuration)   |

Rules:
- description in **kebab-case**, lowercase, 2 to 5 English words;
- business terms use the English translation defined in the **glossary**
  (one term, one translation, everywhere);
- a branch lives **a few days at most**; it is deleted automatically after merge.

Examples:
```
feature/42-apprentice-assignment
fix/57-assignment-rate-check
docs/12-sequence-diagrams
ci/8-hello-world-pipeline
```

Branch names are **checked automatically** on every pull request
(`.github/workflows/conventions.yml`).

---

## 4. Commit messages

**Conventional Commits** format:

```
<type>(<scope>): <description>

[optional body: the why, not the how]

[optional footer: Refs #42, BREAKING CHANGE: ...]
```

| Type       | Usage                                       |
|------------|---------------------------------------------|
| `feat`     | New feature                                 |
| `fix`      | Bug fix                                     |
| `docs`     | Documentation only                          |
| `style`    | Formatting, no logic change                 |
| `refactor` | Restructuring without functional change     |
| `perf`     | Performance improvement                     |
| `test`     | Tests                                       |
| `build`    | Build, dependencies, Dockerfile             |
| `ci`       | GitHub Actions workflows                    |
| `chore`    | Miscellaneous maintenance                   |
| `revert`   | Reverts a previous commit                   |

Rules:
- **scope** = main folder touched: `frontend`, `infra`, `docs`,
  or the service name (folder under `services/`); omit it for root files;
- **description** in English, imperative mood, lowercase, no trailing period,
  72 characters max (`add apprentice assignment endpoint`, not `Added...`);
- one commit = **one coherent change**; commit often;
- `!` after the scope flags a breaking change (e.g. `feat(projects)!: ...`)
  and the footer contains `BREAKING CHANGE: <explanation>`.

Examples:
```
feat(frontend): add project timeline page
fix(<service>): reject assignment rate above 100%
docs(docs): add apprentice assignment sequence diagram
ci(infra): build docker images on pull request
```

---

## 5. Pull requests

- One PR = **one issue**. The body contains `Closes #<number>`.
- The **PR title follows the commit format** (section 4): since we squash-merge,
  it becomes the commit message on `main`. It is limited to 65 characters
  (GitHub appends ` (#123)`) and checked automatically.
- Fill in the template (`.github/pull_request_template.md`), including the
  Definition of Done checklist.
- Keep PRs small (ideally under 400 changed lines): they are easier to review.
- Open the PR as a **draft** early if feedback is useful before it is finished.
- The PR author merges it once approved and CI is green.

---

## 6. `main` protection

Ruleset `protect-main` (*Settings → Rules → Rulesets*) applied to `main`:

| Rule                                          | Value                        |
|-----------------------------------------------|------------------------------|
| Pull request required before merging          | Yes                          |
| Required approvals                            | 1 (someone other than the author) |
| Dismiss stale approvals on new commits        | Yes                          |
| Conversations resolved before merging         | Yes                          |
| Required status checks                        | `branch-name`, `pr-title`, then build, tests and SonarQube Quality Gate |
| Branch up to date with `main` before merging  | Yes                          |
| Linear history, squash merge only             | Yes                          |
| Force push / deletion of `main`               | Blocked                      |
| Bypass list                                   | Empty (admins included)      |

Repository settings (*Settings → General*):
- **squash merge only**, default commit message = PR title;
- automatic deletion of branches after merge.

> ⚠️ On a **free** GitHub organization, these protections only work on
> **public** repositories. A private repository requires a paid plan (GitHub Team).

---

## 7. Versions and releases

- At the **end of each sprint**, `main` is tagged and a GitHub release is published:
  `v1.0.0` (sprint 1), `v2.0.0` (sprint 2), `v3.0.0` (sprint 3).
- Urgent fix after a release: branch `hotfix/<issue>-<description>`,
  then tag `vX.Y.Z` with Z incremented (e.g. `v1.0.1`).
- Release notes are generated by GitHub from PR titles
  (hence the importance of section 4), then reviewed by the PPO.
- Publishing a release triggers the production deployment on Azure;
  every merge into `main` deploys the staging environment.

---

## 8. Never commit

- secrets, passwords, tokens, connection strings, `.env` files;
- personal AI tool files: `CLAUDE.local.md`, `.claude/settings.local.json`;
- build outputs (`target/`, `dist/`, `node_modules/`).

A secret committed by mistake must be **revoked** immediately:
removing it from the history is not enough.
