import { defineConfig } from "@playwright/test";

export default defineConfig({
  testDir: ".",
  testMatch: ["generate-screenshots.spec.ts", "generate-feature-graphic.spec.ts"],
  timeout: 120_000,
  use: {
    headless: true,
  },
  projects: [
    { name: "screenshots", use: { browserName: "chromium" } },
  ],
});
