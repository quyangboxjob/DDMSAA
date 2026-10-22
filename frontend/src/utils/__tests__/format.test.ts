import { describe, expect, it } from 'vitest'
import { formatAmount, formatDate, formatDateTime, formatNumber, formatQuantity } from '../format'

describe('日付・日時の表示', () => {
  it('日付のみの文字列はそのまま返す', () => {
    expect(formatDate('2026-10-22')).toBe('2026-10-22')
  })

  it('日時は日本時間の YYYY-MM-DD HH:mm にする', () => {
    expect(formatDateTime('2026-10-22T10:05:30.123456+09:00')).toBe('2026-10-22 10:05')
  })

  it('UTC の日時も日本時間に変換する', () => {
    expect(formatDateTime('2026-10-22T00:30:00Z')).toBe('2026-10-22 09:30')
  })

  it('日時から日付を表示するときも日本時間にする', () => {
    expect(formatDate('2026-10-21T16:00:00Z')).toBe('2026-10-22')
  })

  it.each([[null], [undefined], [''], ['not-a-date']])('%s は空文字を返す', (value) => {
    expect(formatDateTime(value)).toBe('')
    expect(formatDate(value)).toBe('')
  })
})

describe('数値・数量・金額の表示', () => {
  it('3 桁区切りにする', () => {
    expect(formatNumber(1234567)).toBe('1,234,567')
    expect(formatNumber('1234')).toBe('1,234')
    expect(formatNumber(0)).toBe('0')
    expect(formatNumber(-1234)).toBe('-1,234')
  })

  it('数量は小数 3 桁まで、末尾の 0 を省略しない', () => {
    expect(formatQuantity('2.500')).toBe('2.500')
    expect(formatQuantity('1234.5')).toBe('1,234.5')
    expect(formatQuantity(10)).toBe('10')
    expect(formatQuantity('1.23456')).toBe('1.234')
  })

  it('金額は 3 桁区切り＋単位にする', () => {
    expect(formatAmount(12345, '円')).toBe('12,345 円')
    expect(formatAmount('13700.00', '円')).toBe('13,700.00 円')
  })

  it.each([[null], [undefined], [''], ['abc']])('%s は空文字を返す', (value) => {
    expect(formatNumber(value)).toBe('')
    expect(formatQuantity(value)).toBe('')
    expect(formatAmount(value, '円')).toBe('')
  })
})
