import { defineConfig } from '@playwright/test';

// API-only tests: no browser is started (no test uses the `page` fixture), so `npx playwright install` isn't needed.
export default defineConfig({
  testDir: './tests',
  fullyParallel: true,
  reporter: 'list',
});
