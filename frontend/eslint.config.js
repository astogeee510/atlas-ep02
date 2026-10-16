import js from '@eslint/js';
import globals from 'globals';
import react from 'eslint-plugin-react';
import reactHooks from 'eslint-plugin-react-hooks';
import jsxA11y from 'eslint-plugin-jsx-a11y';
import security from 'eslint-plugin-security';

// SAST del frontend: buenas practicas JS, React, accesibilidad (WCAG) y seguridad.
export default [
  {
    ignores: [
      'dist',
      'coverage',
      'node_modules',
      'lhci-report',
      'cypress/videos',
      'cypress/screenshots',
    ],
  },
  js.configs.recommended,
  react.configs.flat.recommended,
  react.configs.flat['jsx-runtime'],
  jsxA11y.flatConfigs.recommended,
  security.configs.recommended,
  {
    files: ['**/*.{js,jsx}'],
    languageOptions: {
      ecmaVersion: 'latest',
      sourceType: 'module',
      globals: { ...globals.browser, ...globals.node },
      parserOptions: { ecmaFeatures: { jsx: true } },
    },
    plugins: { 'react-hooks': reactHooks },
    settings: { react: { version: 'detect' } },
    rules: {
      ...reactHooks.configs.recommended.rules,
      'react/prop-types': 'off',
      // Prohibido inyectar HTML sin escapar (previene XSS, NFR-SEG-5)
      'react/no-danger': 'error',
      'no-eval': 'error',
      'no-implied-eval': 'error',
      'no-new-func': 'error',
    },
  },
  {
    files: ['cypress/**/*.js'],
    languageOptions: {
      globals: { ...globals.mocha, cy: 'readonly', Cypress: 'readonly', expect: 'readonly' },
    },
  },
];
