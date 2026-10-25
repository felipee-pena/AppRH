import js from "@eslint/js";
import globals from "globals";
import reactHooks from "eslint-plugin-react-hooks";
import reactRefresh from "eslint-plugin-react-refresh";
import tseslint from "typescript-eslint";

export default tseslint.config(
  {
    ignores: [
      "dist",
      "coverage",
      "node_modules",
    ],
  },

  js.configs.recommended,

  ...tseslint.configs.recommended,

  {
    files: [
      "src/**/*.{ts,tsx}",
      "vite.config.ts",
    ],

    languageOptions: {
      ecmaVersion: 2024,

      globals: {
        ...globals.browser,
        ...globals.es2024,
        ...globals.node,
      },

      parserOptions: {
        sourceType: "module",
      },
    },

    plugins: {
      "react-hooks": reactHooks,
      "react-refresh": reactRefresh,
    },

    rules: {
      ...reactHooks.configs.recommended.rules,

      "react-refresh/only-export-components": [
        "warn",
        {
          allowConstantExport: true,
        },
      ],

      "@typescript-eslint/no-explicit-any": "warn",

      "@typescript-eslint/no-unused-vars": [
        "error",
        {
          argsIgnorePattern: "^_",
          varsIgnorePattern: "^_",
          caughtErrorsIgnorePattern: "^_",
        },
      ],
    },
  },

  {
    files: [
      "src/contexts/AuthContext.tsx",
    ],

    rules: {
      "react-hooks/set-state-in-effect": "off",
      "react-refresh/only-export-components": "off",
    },
  },

  {
    files: [
      "src/test/**/*.{ts,tsx}",
    ],

    languageOptions: {
      globals: {
        ...globals.browser,
        ...globals.es2024,
        ...globals.node,
      },
    },

    rules: {
      "react-refresh/only-export-components": "off",
    },
  }
);
