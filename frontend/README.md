# SIGL platform — React client

Single-page application of the SIGL platform: React 18, TypeScript (strict), Vite and MUI.
Folder organization follows Part 2 §3 of the specification document; contribution rules are in
`../CONTRIBUTING.md` and `../.claude/rules/frontend.md`.

## Commands

| Command | Purpose |
| --- | --- |
| `npm install` | Install dependencies |
| `npm run dev` | Start the development server |
| `npm run build` | Type-check and build for production |
| `npm run lint` | Run ESLint |
| `npm test` | Run component tests (Vitest + React Testing Library) |
| `npm run check:contrast` | Check that every theme colour pair meets WCAG 2.1 AA |

## Theme

`src/theme/theme.ts` holds the MUI theme (light and dark colour schemes, visibility levels,
calendar event colours). Run `npm run check:contrast` after changing any colour value.
