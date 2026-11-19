import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { CssBaseline, ThemeProvider } from '@mui/material';
import { App } from './App';
import { theme } from './theme/theme';

const rootElement = document.getElementById('root');
if (!rootElement) {
  throw new Error('Missing #root element in index.html');
}

createRoot(rootElement).render(
  <StrictMode>
    {/* defaultMode="system" follows the operating system's light or dark preference. */}
    <ThemeProvider theme={theme} defaultMode="system">
      <CssBaseline />
      <App />
    </ThemeProvider>
  </StrictMode>,
);
