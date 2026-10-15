# Instructions for AI coding assistants

This file is for any coding assistant (Claude Code, GitHub Copilot, Codex, Cursor…).
Read it in full before changing anything in the repository.

## The project

A platform for managing company projects and following apprenticeship engineers
(SIGL project 2026/2027). Microservices architecture:

- **Client**: React + TypeScript + Vite, React Router, TanStack Query (`frontend/`).
- **Services**: Spring Boot (Java), one folder per microservice under `services/`.
  Each service owns its data and exposes a REST API documented with OpenAPI.
- **Gateway**: Spring Cloud Gateway (routing, JWT validation).
- **Identity**: Keycloak (OAuth2 / OpenID Connect).
- **Asynchronous**: message broker between services.
- **Delivery**: Docker, Docker Compose locally, Microsoft Azure for staging and production.

## Sources of truth to read before coding

1. `CONTRIBUTING.md`: branches, commits, pull requests, versions. **Mandatory before
   any Git operation.**
2. The **glossary** and the **class diagram** of the specification document:
   they define the business vocabulary and its English translation. Never invent a
   synonym: one business term has exactly one name in code.
3. `docs/uml/`: sequence, activity and state diagrams. The code must follow them
   (state names, allowed transitions, call order).

If the required information is in none of these sources, **stop and ask** instead of
guessing.

## Hard rules

- Never commit or push directly to `main`.
- Never use `git push --force`, `--no-verify`, or rewrite shared history.
- Always work on a branch linked to an issue: `<type>/<issue>-<description>`.
- Commit messages follow Conventional Commits (details in `CONTRIBUTING.md`).
- Never write secrets in code, tests or versioned configuration files.
  Use environment variables.
- Do not modify `CONTRIBUTING.md`, `AGENTS.md`, `.github/` or `.claude/` unless the
  issue explicitly asks for it.
- Do not add a dependency without mentioning it in the PR description.

## Code quality

- Identifiers (variables, functions, classes, constants) and comments in **English**.
- Comments only when they explain a non-obvious *why*.
- Favor readability and simplicity over cleverness.
- Every new behavior comes with tests; every integrity rule has its own test.
- Business code coverage ≥ 70%; a merge is blocked if a test fails.

## Before opening a pull request

1. Tests of the modified folder(s) pass locally.
2. The PR title follows the commit format; the body contains `Closes #<issue>`.
3. The PR template checklist (Definition of Done) is filled in honestly:
   do not tick what has not been verified.
4. Summarize in the PR the assumptions made and what remains to be validated.
