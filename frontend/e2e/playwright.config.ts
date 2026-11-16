import { defineConfig } from '@playwright/test';

// End-to-end tests run against an assembled platform (Docker Compose locally,
// Azure staging in CI). The URL is never hard-coded: set E2E_BASE_URL.
export default defineConfig({
  testDir: './tests',
  timeout: 60_000,
  retries: process.env.CI ? 1 : 0,
  reporter: [['list'], ['html', { open: 'never' }]],
  use: {
    baseURL: process.env.E2E_BASE_URL ?? 'http://localhost:3000',
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
});
