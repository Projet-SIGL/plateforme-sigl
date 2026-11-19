import { createTheme, type ColorSystemOptions } from '@mui/material/styles';

/**
 * SIGL platform theme (MUI 9, CSS variables, light and dark colour schemes).
 *
 * Every foreground/background pair used for text reaches WCAG 2.1 AA (>= 4.5:1),
 * and every non-text indicator reaches 3:1 against its surface. The pairs are
 * verified by `scripts/check-contrast.ts`; run it after changing any value here.
 *
 * Hue allocation, so that no two meanings share a colour:
 *   primary indigo · secondary teal · info blue · success green · warning amber · error red
 *   visibility: slate (internal public) -> violet (restricted) -> magenta (confidential)
 */

/** Solid colour with its text colour, plus a soft "container" pair for chips and banners. */
export interface TonalColor {
  main: string;
  contrastText: string;
  container: string;
  onContainer: string;
}

/** Calendar event colours: `accent` is the left bar / dot, the pair is the event body. */
export interface CalendarEventColor {
  accent: string;
  container: string;
  onContainer: string;
}

export interface VisibilityPalette {
  internalPublic: TonalColor;
  restricted: TonalColor;
  confidential: TonalColor;
}

export interface CalendarPalette {
  class: CalendarEventColor;
  exam: CalendarEventColor;
  companyPeriod: CalendarEventColor;
  unavailability: CalendarEventColor;
  meeting: CalendarEventColor;
  milestone: CalendarEventColor;
}

declare module '@mui/material/styles' {
  interface Palette {
    visibility: VisibilityPalette;
    calendar: CalendarPalette;
  }
  interface PaletteOptions {
    visibility?: VisibilityPalette;
    calendar?: CalendarPalette;
  }
}

/** Values of the `VisibilityLevel` enumeration defined in the specification glossary. */
export type VisibilityLevel = 'INTERNAL_PUBLIC' | 'RESTRICTED' | 'CONFIDENTIAL';

/** Values of the calendar event types, extended with the two project-side event kinds. */
export type CalendarEventKind =
  | 'CLASS'
  | 'EXAM'
  | 'COMPANY_PERIOD'
  | 'UNAVAILABILITY'
  | 'MEETING'
  | 'MILESTONE';

export const VISIBILITY_PALETTE_KEY: Record<VisibilityLevel, keyof VisibilityPalette> = {
  INTERNAL_PUBLIC: 'internalPublic',
  RESTRICTED: 'restricted',
  CONFIDENTIAL: 'confidential',
};

export const CALENDAR_PALETTE_KEY: Record<CalendarEventKind, keyof CalendarPalette> = {
  CLASS: 'class',
  EXAM: 'exam',
  COMPANY_PERIOD: 'companyPeriod',
  UNAVAILABILITY: 'unavailability',
  MEETING: 'meeting',
  MILESTONE: 'milestone',
};

/** Cool grey scale shared by both schemes (slightly blue so it sits well next to the indigo primary). */
const grey = {
  50: '#F5F6F9',
  100: '#ECEEF3',
  200: '#DDE1E9',
  300: '#C5CBD6',
  400: '#9BA4B4',
  500: '#7A8496',
  600: '#525C6E',
  700: '#3B4454',
  800: '#262D3A',
  900: '#1A2130',
} as const;

type SchemePalette = NonNullable<ColorSystemOptions['palette']>;

/** Exported separately so that `scripts/check-contrast.ts` can verify the raw values. */
export const lightPalette = {
  primary: { light: '#5B70CC', main: '#3046A5', dark: '#223380', contrastText: '#FFFFFF' },
  secondary: { light: '#2E948B', main: '#0B6E66', dark: '#084F49', contrastText: '#FFFFFF' },
  success: { light: '#3F9A5B', main: '#1E7B3C', dark: '#14572A', contrastText: '#FFFFFF' },
  warning: { light: '#C07A12', main: '#9A5B00', dark: '#6E4100', contrastText: '#FFFFFF' },
  error: { light: '#D9534F', main: '#C0282D', dark: '#8E1B20', contrastText: '#FFFFFF' },
  info: { light: '#3A8FCB', main: '#0B6AA8', dark: '#084C7A', contrastText: '#FFFFFF' },
  grey,
  text: { primary: grey[900], secondary: grey[600], disabled: grey[400] },
  background: { default: grey[50], paper: '#FFFFFF' },
  divider: grey[200],
  Alert: {
    successColor: '#14572A',
    successStandardBg: '#E3F4E8',
    warningColor: '#6E4100',
    warningStandardBg: '#FDF0D5',
    errorColor: '#8E1B20',
    errorStandardBg: '#FCE8E8',
    infoColor: '#084C7A',
    infoStandardBg: '#E1F0FA',
  },
  visibility: {
    internalPublic: {
      main: '#44607A',
      contrastText: '#FFFFFF',
      container: '#E6EDF3',
      onContainer: '#2F4558',
    },
    restricted: {
      main: '#6B3FB8',
      contrastText: '#FFFFFF',
      container: '#EFE8FA',
      onContainer: '#4B2A86',
    },
    confidential: {
      main: '#A81B62',
      contrastText: '#FFFFFF',
      container: '#FBE6F0',
      onContainer: '#7A1146',
    },
  },
  calendar: {
    class: { accent: '#1769C2', container: '#DCEBFA', onContainer: '#0B4F8A' },
    exam: { accent: '#C2410C', container: '#FDE5DC', onContainer: '#8A2A0B' },
    companyPeriod: { accent: '#7A8496', container: '#E6E9EF', onContainer: '#3B4454' },
    unavailability: { accent: '#8D6B3A', container: '#F3E9DA', onContainer: '#5A3E1B' },
    meeting: { accent: '#3046A5', container: '#E4E8F8', onContainer: '#223380' },
    milestone: { accent: '#0B6E66', container: '#DDF2EF', onContainer: '#084F49' },
  },
} satisfies SchemePalette;

export const darkPalette = {
  primary: { light: '#C3CEFF', main: '#9DB0FF', dark: '#7C92F0', contrastText: '#0E1A4D' },
  secondary: { light: '#A2E5DC', main: '#5CD1C4', dark: '#3DB5A8', contrastText: '#00302C' },
  success: { light: '#A5E6B6', main: '#6FD08A', dark: '#4DB56B', contrastText: '#06310F' },
  warning: { light: '#F8D48B', main: '#F2B544', dark: '#D99A26', contrastText: '#3A2500' },
  error: { light: '#FFB6B2', main: '#FF8A85', dark: '#F0696A', contrastText: '#4A0A0C' },
  info: { light: '#A6DAFA', main: '#6CC0F5', dark: '#4AA6E0', contrastText: '#04293F' },
  grey,
  text: { primary: '#E8EBF2', secondary: '#A7B0C0', disabled: '#6F7A8E' },
  background: { default: '#10141C', paper: '#181D28' },
  divider: '#2E3646',
  Alert: {
    successColor: '#A5E6B6',
    successStandardBg: '#15301E',
    warningColor: '#F8D48B',
    warningStandardBg: '#33270F',
    errorColor: '#FFB6B2',
    errorStandardBg: '#3A1A1B',
    infoColor: '#A6DAFA',
    infoStandardBg: '#112B3D',
  },
  visibility: {
    internalPublic: {
      main: '#9FB8CF',
      contrastText: '#14232F',
      container: '#1E2B37',
      onContainer: '#C3D5E4',
    },
    restricted: {
      main: '#C1A3F7',
      contrastText: '#2A1458',
      container: '#2A2044',
      onContainer: '#D8C6FA',
    },
    confidential: {
      main: '#F58BBE',
      contrastText: '#4D0A2B',
      container: '#3D1529',
      onContainer: '#F9B7D6',
    },
  },
  calendar: {
    class: { accent: '#5AA2EE', container: '#12304F', onContainer: '#A9D0FA' },
    exam: { accent: '#F27A45', container: '#47200F', onContainer: '#FFC0A6' },
    companyPeriod: { accent: '#8894A8', container: '#2A3140', onContainer: '#CBD2DE' },
    unavailability: { accent: '#C29A5E', container: '#3A2D18', onContainer: '#E6CFA8' },
    meeting: { accent: '#9DB0FF', container: '#1E2A5C', onContainer: '#C3CEFF' },
    milestone: { accent: '#5CD1C4', container: '#0F3632', onContainer: '#A2E5DC' },
  },
} satisfies SchemePalette;

export const theme = createTheme({
  cssVariables: { colorSchemeSelector: 'data', cssVarPrefix: 'sigl' },
  shape: { borderRadius: 8 },
  colorSchemes: {
    light: { palette: lightPalette },
    dark: { palette: darkPalette },
  },
});
