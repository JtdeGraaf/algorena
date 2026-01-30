import { defineConfig } from '@hey-api/openapi-ts';

export default defineConfig({
  input: '../backend/temp api specs for frontend.json',
  output: {
    path: 'src/api/generated',
  },
  plugins: [
    '@hey-api/typescript',
    '@hey-api/sdk',
    '@hey-api/client-fetch',
  ],
});

