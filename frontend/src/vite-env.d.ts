/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** API の basePath。既定は `/api/v1`。 */
  readonly VITE_API_BASE_URL?: string
  /** 開発サーバ（Vite）が /api を中継する api-gateway の URL。 */
  readonly VITE_DEV_PROXY_TARGET?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

declare const __APP_VERSION__: string
