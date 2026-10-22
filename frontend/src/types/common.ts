/**
 * 共通型。spec-07-api-list.yaml の components.schemas（Result / PageResult / ErrorResponse /
 * FieldError）と一対一で対応させる。項目の追加・改名はしない。
 */

/** 統一レスポンスのエンベロープ（`Result.code` は文字列、成功は `"0"`）。 */
export interface Result<T = unknown> {
  /** 結果コード。成功は `"0"`。 */
  code: string
  /** サーバ側の既定文言（フォールバック）。表示は messageId を正とする。 */
  message: string
  /** 表示用メッセージ ID（I / W）。画面側が i18n で解決して表示する。 */
  messageId?: string
  /** messageId の `{0}`,`{1}` の置換値。 */
  messageArgs?: string[]
  traceId?: string
  timestamp?: string
  /** 応答データ。データなしの API は `null`。 */
  data: T
}

/** ページング応答（`Result.data` に入る）。 */
export interface PageResult<T> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
  pages: number
}

/** 項目単位のエラー。 */
export interface FieldError {
  field?: string
  reason?: string
}

/** エラー応答。表示文言は messageId を i18n で解決し、`message` はフォールバック専用。 */
export interface ErrorResponse {
  /** エラーメッセージ ID（E）。 */
  messageId: string
  messageArgs?: string[]
  /** サーバ側の既定文言（フォールバック / ログ用）。 */
  message: string
  timestamp: string
  path: string
  traceId?: string
  details?: FieldError[]
}
