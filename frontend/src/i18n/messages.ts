/**
 * messageId → 表示文言の解決（spec-frontend-coding-rule §5）。バックエンドは messageId と
 * messageArgs だけを返し、現在の言語のカタログ（spec-09 から同期）で文言にする。
 */
import type { TFunction } from 'i18next'
import { ApiError } from '@/api/http'

/** メッセージの区分（messageId の先頭文字）。 */
export type MessageCategory = 'INFO' | 'WARNING' | 'ERROR'

/**
 * messageId の区分を返す。
 *
 * @param messageId I / W / E で始まる ID
 * @returns 区分。形式が不正なら `null`
 */
export function messageCategory(messageId: string): MessageCategory | null {
  switch (messageId.charAt(0)) {
    case 'I':
      return 'INFO'
    case 'W':
      return 'WARNING'
    case 'E':
      return 'ERROR'
    default:
      return null
  }
}

/**
 * messageId を現在の言語の文言に解決する。`{0}`,`{1}` は messageArgs で置換する。
 *
 * @param t i18next の翻訳関数
 * @param messageId メッセージ ID
 * @param messageArgs 置換値
 * @param fallback カタログにない場合の文言（サーバの既定文言）
 * @returns 表示文言
 */
export function resolveMessage(
  t: TFunction,
  messageId: string,
  messageArgs: readonly string[] = [],
  fallback?: string,
): string {
  const args = Object.fromEntries(messageArgs.map((value, index) => [String(index), value]))
  return t(messageId, { ...args, defaultValue: fallback ?? messageId })
}

/**
 * API エラーを表示文言に解決する。
 *
 * @param t i18next の翻訳関数
 * @param error 捕捉した値（ApiError 以外は E0012 として扱う）
 * @returns 表示文言
 */
export function resolveErrorMessage(t: TFunction, error: unknown): string {
  if (error instanceof ApiError) {
    return resolveMessage(t, error.messageId, error.error.messageArgs, error.error.message)
  }
  return resolveMessage(t, 'E0012')
}
