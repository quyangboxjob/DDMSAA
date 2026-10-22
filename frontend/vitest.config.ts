import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vitest/config'

// フロントエンドの単体・結合テストは jsdom で実行する（spec-test-rule §7、2026-10-21 決定）。
// jsdom は package.json で固定している。更新時は全テストが起動することを確認する。
export default defineConfig({
  define: {
    __APP_VERSION__: JSON.stringify('test'),
  },
  resolve: {
    alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) },
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./vitest.setup.ts'],
    include: ['src/**/__tests__/**/*.test.{ts,tsx}'],
    reporters: ['default', 'junit'],
    outputFile: { junit: './test-results/junit.xml' },
  },
})
