/**
 * Verifies that every colour pair of the SIGL theme meets WCAG 2.1 AA.
 * Text pairs need 4.5:1; non-text indicators (borders, calendar accents) need 3:1.
 * Run with: npx tsx scripts/check-contrast.ts  (exits with code 1 on any failure)
 */
import { darkPalette, lightPalette } from '../src/theme/theme';

const TEXT_RATIO = 4.5;
const NON_TEXT_RATIO = 3;

function relativeLuminance(hex: string): number {
  const channels = [1, 3, 5].map((start) => parseInt(hex.slice(start, start + 2), 16) / 255);
  const [r, g, b] = channels.map((c) => (c <= 0.04045 ? c / 12.92 : ((c + 0.055) / 1.055) ** 2.4));
  return 0.2126 * r + 0.7152 * g + 0.0722 * b;
}

function contrastRatio(foreground: string, background: string): number {
  const [lighter, darker] = [relativeLuminance(foreground), relativeLuminance(background)].sort(
    (a, b) => b - a,
  );
  return (lighter + 0.05) / (darker + 0.05);
}

type Pair = [label: string, foreground: string, background: string, minimum: number];

let failureCount = 0;

const schemes = { light: lightPalette, dark: darkPalette };

for (const [schemeName, palette] of Object.entries(schemes)) {
  const surfaces = [palette.background.default, palette.background.paper];
  const pairs: Pair[] = [];

  for (const surface of surfaces) {
    pairs.push(['text.primary', palette.text.primary, surface, TEXT_RATIO]);
    pairs.push(['text.secondary', palette.text.secondary, surface, TEXT_RATIO]);
  }

  for (const name of ['primary', 'secondary', 'success', 'warning', 'error', 'info'] as const) {
    const color = palette[name];
    // `main` is used as text/link colour on paper; `main` and `dark` (hover) carry contrastText.
    pairs.push([`${name}.main on paper`, color.main, palette.background.paper, TEXT_RATIO]);
    pairs.push([`${name}.contrastText on main`, color.contrastText, color.main, TEXT_RATIO]);
    pairs.push([`${name}.contrastText on dark`, color.contrastText, color.dark, TEXT_RATIO]);
  }

  for (const name of ['success', 'warning', 'error', 'info'] as const) {
    pairs.push([
      `Alert ${name}`,
      palette.Alert[`${name}Color`],
      palette.Alert[`${name}StandardBg`],
      TEXT_RATIO,
    ]);
  }

  for (const [name, color] of Object.entries(palette.visibility)) {
    pairs.push([`visibility.${name}.main on paper`, color.main, palette.background.paper, TEXT_RATIO]);
    pairs.push([`visibility.${name} solid`, color.contrastText, color.main, TEXT_RATIO]);
    pairs.push([`visibility.${name} container`, color.onContainer, color.container, TEXT_RATIO]);
  }

  for (const [name, color] of Object.entries(palette.calendar)) {
    pairs.push([`calendar.${name} container`, color.onContainer, color.container, TEXT_RATIO]);
    pairs.push([`calendar.${name} accent on paper`, color.accent, palette.background.paper, NON_TEXT_RATIO]);
  }

  console.log(`== ${schemeName}`);
  for (const [label, foreground, background, minimum] of pairs) {
    const ratio = contrastRatio(foreground, background);
    const passed = ratio >= minimum;
    if (!passed) failureCount += 1;
    console.log(
      `${passed ? 'ok  ' : 'FAIL'} ${ratio.toFixed(2).padStart(5)} (>= ${minimum}) ${label}: ${foreground} on ${background}`,
    );
  }
}

console.log(`\n${failureCount} failure(s)`);
process.exit(failureCount === 0 ? 0 : 1);
