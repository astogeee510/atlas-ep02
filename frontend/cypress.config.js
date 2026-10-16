import { defineConfig } from 'cypress';

// E2E y regresion (CP-REG-01, CP-COMPAT-01). Por defecto contra el stack Docker con TLS.
export default defineConfig({
  e2e: {
    baseUrl: process.env.CYPRESS_BASE_URL || 'https://localhost',
    viewportWidth: 1280,
    viewportHeight: 800,
    video: false,
    screenshotOnRunFailure: true,
    defaultCommandTimeout: 10000,
  },
});
