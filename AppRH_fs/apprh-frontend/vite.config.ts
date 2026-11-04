import {
  defineConfig,
} from "vitest/config";

import react from
  "@vitejs/plugin-react";

export default defineConfig({
  plugins: [
    react(),
  ],

  server: {
    port:
      5173,

    strictPort:
      true,

    proxy: {
      "/api": {
        target:
          "http://localhost:8081",

        changeOrigin:
          true,
      },

      "/auth": {
        target:
          "http://localhost:8081",

        changeOrigin:
          true,
      },

      "/oauth2": {
        target:
          "http://localhost:8081",

        changeOrigin:
          true,
      },

      "/login/oauth2": {
        target:
          "http://localhost:8081",

        changeOrigin:
          true,
      },
    },
  },

  test: {
    environment:
      "jsdom",

    globals:
      true,

    setupFiles: [
      "./src/test/setup.ts",
    ],

    include: [
      "src/test/**/*.test.ts",
      "src/test/**/*.test.tsx",
    ],

    pool:
      "threads",

    maxWorkers:
      2,

    isolate:
      true,

    testTimeout:
      10000,

    hookTimeout:
      10000,

    teardownTimeout:
      10000,

    coverage: {
      provider:
        "v8",

      reporter: [
        "text",
        "html",
        "json",
        "lcov",
      ],

      reportsDirectory:
        "./coverage",

      include: [
        "src/components/**/*.tsx",
        "src/contexts/**/*.tsx",
        "src/hooks/**/*.ts",
        "src/layouts/**/*.tsx",
        "src/pages/**/*.tsx",
        "src/routes/**/*.tsx",
        "src/services/**/*.ts",
        "src/utils/**/*.ts",
        "src/App.tsx",
      ],

      exclude: [
        "src/test/**",
        "src/interfaces/**",
        "src/main.tsx",
        "src/vite-env.d.ts",
      ],

      thresholds: {
        statements:
          30,

        branches:
          35,

        functions:
          33,

        lines:
          30,
      },
    },
  },
});