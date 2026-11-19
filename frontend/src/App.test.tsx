import { render, screen } from '@testing-library/react';
import { ThemeProvider } from '@mui/material';
import { describe, expect, it } from 'vitest';
import { App } from './App';
import { theme } from './theme/theme';

describe('App', () => {
  it('renders the platform title inside the SIGL theme', () => {
    render(
      <ThemeProvider theme={theme}>
        <App />
      </ThemeProvider>,
    );

    expect(screen.getByRole('heading', { level: 1, name: 'Plateforme SIGL' })).toBeInTheDocument();
  });
});
