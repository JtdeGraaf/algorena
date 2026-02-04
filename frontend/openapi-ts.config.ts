import {defineConfig} from '@hey-api/openapi-ts';

export default defineConfig({
    input: 'http://localhost:8080/v3/api-docs',
    output: {
        path: 'src/api/generated',
    },
    plugins: [
        '@hey-api/typescript',
        '@hey-api/sdk',
        '@hey-api/client-fetch',
    ],
});

