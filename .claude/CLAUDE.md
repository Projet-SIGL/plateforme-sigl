<!--
  Claude Code loads this file at the start of every session.
  The shared rules live in AGENTS.md and CONTRIBUTING.md so that every AI tool
  reads the same conventions; they are imported here rather than copied.
  Imports resolve relative to this file, hence the "../" prefix.
-->
@../AGENTS.md
@../CONTRIBUTING.md

## Claude Code specifics

- For a task that touches more than one service or changes an API or event
  contract, present a plan first and wait for approval before coding.
- Frontend and service rules live in `.claude/rules/`; they load when you open
  a file in the matching folder.
