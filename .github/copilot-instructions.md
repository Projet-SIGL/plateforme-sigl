# Instructions for GitHub Copilot

The full conventions are in `AGENTS.md` (AI instructions) and `CONTRIBUTING.md`
(Git and GitHub). Read them before changing anything. Key points:

- Never commit to `main`; one `<type>/<issue>-<description>` branch per issue.
- Commits and PR titles follow Conventional Commits: `type(scope): description`.
- Business vocabulary: the glossary and class diagram terms, with no synonyms.
- Identifiers and comments in English; comments only for the *why*.
- No secrets in code; every new behavior has its tests.
- When information is missing, ask instead of guessing.
