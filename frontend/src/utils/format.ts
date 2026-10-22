/**
 * 表示形式の共通ユーティリティ（spec-12 §6）。各コンポーネントで独自の書式化・タイムゾーン変換を
 * しないこと。`updatedAt` など楽観ロックの値はこの関数で変換せず、API の文字列のまま保持して返送する。
 */
import dayjs from 'dayjs'
import timezone from 'dayjs/plugin/timezone'
import utc from 'dayjs/plugin/utc'

dayjs.extend(utc)
dayjs.extend(timezone)

/** 表示タイムゾーン（日本時間）。 */
export const DISPLAY_TIME_ZONE = 'Asia/Tokyo'

const DATE_ONLY = /^\d{4}-\d{2}-\d{2}$/

/**
 * 日付を `YYYY-MM-DD` で表示する。日付のみの文字列は変換せずそのまま返す。
 *
 * @param value ISO 8601 文字列（日付のみ、または `+09:00` 付き日時）
 * @returns 表示文字列。空・不正なら `''`
 */
export function formatDate(value: string | null | undefined): string {
  if (!value) return ''
  if (DATE_ONLY.test(value)) return value
  const parsed = dayjs(value)
  return parsed.isValid() ? parsed.tz(DISPLAY_TIME_ZONE).format('YYYY-MM-DD') : ''
}

/**
 * 日時を日本時間の `YYYY-MM-DD HH:mm` で表示する。
 *
 * @param value ISO 8601 の日時文字列
 * @returns 表示文字列。空・不正なら `''`
 */
export function formatDateTime(value: string | null | undefined): string {
  if (!value) return ''
  const parsed = dayjs(value)
  return parsed.isValid() ? parsed.tz(DISPLAY_TIME_ZONE).format('YYYY-MM-DD HH:mm') : ''
}

const INTEGER_FORMAT = new Intl.NumberFormat('ja-JP', { maximumFractionDigits: 0 })

function groupInteger(integerPart: string): string {
  const negative = integerPart.startsWith('-')
  const digits = negative ? integerPart.slice(1) : integerPart
  return (negative ? '-' : '') + INTEGER_FORMAT.format(BigInt(digits || '0'))
}

/**
 * 数値・件数を 3 桁区切りで表示する（小数部はそのまま保持する）。
 *
 * @param value 数値または数値文字列
 * @returns 表示文字列。数値でなければ `''`
 */
export function formatNumber(value: number | string | null | undefined): string {
  if (value === null || value === undefined || value === '') return ''
  const text = String(value)
  if (!/^-?\d+(\.\d+)?$/.test(text)) return ''
  const [integerPart = '0', fraction] = text.split('.')
  return fraction === undefined
    ? groupInteger(integerPart)
    : `${groupInteger(integerPart)}.${fraction}`
}

/**
 * 数量を 3 桁区切りで表示する。小数は 3 桁まで、末尾の 0 は省略しない（API の桁をそのまま使う）。
 *
 * @param value 数量（数値または数値文字列）
 * @returns 表示文字列。数値でなければ `''`
 */
export function formatQuantity(value: number | string | null | undefined): string {
  if (value === null || value === undefined || value === '') return ''
  const text = String(value)
  if (!/^-?\d+(\.\d+)?$/.test(text)) return ''
  const [integerPart = '0', fraction] = text.split('.')
  return fraction === undefined
    ? groupInteger(integerPart)
    : `${groupInteger(integerPart)}.${fraction.slice(0, 3)}`
}

/**
 * 金額を 3 桁区切り＋「円」で表示する（例: `12,345 円`）。
 * 小数の表示桁は未決（TBD-S2）のため、API が返した小数部をそのまま表示する。
 *
 * @param value 金額（数値または数値文字列）
 * @param unit 通貨単位の文言（i18n の `ui.common.yen`）
 * @returns 表示文字列。数値でなければ `''`
 */
export function formatAmount(value: number | string | null | undefined, unit: string): string {
  const text = formatNumber(value)
  return text === '' ? '' : `${text} ${unit}`
}
