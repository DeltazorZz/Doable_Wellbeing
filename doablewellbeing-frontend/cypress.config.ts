import { defineConfig } from "cypress";

export default defineConfig({
  e2e: {
    baseUrl: "http://localhost:3000",
    video: false,
    screenshotOnRunFailure: true,
    defaultCommandTimeout: 8000,
    env: {
      apiBaseUrl: "http://localhost:8080",
    },
    setupNodeEvents(on, config) {
      return config;
    },
  },
});
