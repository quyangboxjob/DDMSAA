import { describe, expect, it } from 'vitest'
import { ApiError } from '@/api/http'
import { ORDER_STATUSES, ROLES, TAX_CATEGORIES } from '@/types/enums'
import i18n, { changeLanguage, DEFAULT_LANGUAGE, isLanguage, LANGUAGE_STORAGE_KEY } from '..'
import { enumLabel } from '../enum-label'
import messagesJa from '../generated/messages.ja.json'
import messagesZh from '../generated/messages.zh.json'
import { messageCategory, resolveErrorMessage, resolveMessage } from '../messages'

const t = i18n.t.bind(i18n)

describe('メッセージの解決（messageId）', () => {
  it('既定言語は日本語', () => {
    expect(DEFAULT_LANGUAGE).toBe('ja')
    expect(i18n.language).toBe('ja')
  })

  it('messageId を日本語の文言に解決する', () => {
    expect(resolveMessage(t, 'I0003')).toBe('ログインしました')
  })

  it('{0} を messageArgs で置換する', () => {
    expect(resolveMessage(t, 'I0010', ['OD00000001'])).toBe('登録しました（ID: OD00000001）')
    expect(resolveMessage(t, 'E0032', ['顧客名', '200'])).toBe(
      '顧客名は200文字以内で入力してください',
    )
  })

  it('カタログにない messageId はサーバの既定文言にフォールバックする', () => {
    expect(resolveMessage(t, 'E9999', [], 'サーバ既定文言')).toBe('サーバ既定文言')
    expect(resolveMessage(t, 'E9999')).toBe('E9999')
  })

  it('ApiError を messageId で解決し、それ以外は E0012 にする', () => {
    const error = new ApiError(409, {
      messageId: 'E0006',
      message: 'x',
      timestamp: '2026-10-22T10:00:00+09:00',
      path: '/api/v1/orders',
    })
    expect(resolveErrorMessage(t, error)).toBe(messagesJa.E0006)
    expect(resolveErrorMessage(t, new Error('boom'))).toBe(messagesJa.E0012)
  })

  it('messageId の区分を先頭文字から判定する', () => {
    expect(messageCategory('I0001')).toBe('INFO')
    expect(messageCategory('W0010')).toBe('WARNING')
    expect(messageCategory('E0001')).toBe('ERROR')
    expect(messageCategory('X0001')).toBeNull()
  })

  it('3 言語のカタログは同じ messageId を持つ', () => {
    expect(Object.keys(messagesZh).sort()).toEqual(Object.keys(messagesJa).sort())
  })
})

describe('言語の切り替え', () => {
  it('切り替えると別言語の文言になり、保存される', async () => {
    await changeLanguage('zh')
    expect(resolveMessage(t, 'I0003')).toBe(messagesZh.I0003)
    expect(window.localStorage.getItem(LANGUAGE_STORAGE_KEY)).toBe('zh')
    await changeLanguage('ja')
  })

  it('画面ラベルは未提供の言語では日本語にフォールバックする（TBD-S1）', async () => {
    await changeLanguage('en')
    expect(t('ui.common.save')).toBe('保存')
    await changeLanguage('ja')
  })

  it('isLanguage は対応言語だけを許可する', () => {
    expect(isLanguage('ja')).toBe(true)
    expect(isLanguage('fr')).toBe(false)
    expect(isLanguage(null)).toBe(false)
  })
})

describe('列挙の表示名（spec-12 §6.1）', () => {
  it('ロール・注文状態・税区分を日本語名で返す', () => {
    expect(enumLabel(t, 'Role', 'EMPLOYEE')).toBe('営業担当')
    expect(enumLabel(t, 'OrderStatus', 'PENDING_APPROVAL')).toBe('承認待ち')
    expect(enumLabel(t, 'TaxCategory', 'REDUCED')).toBe('軽減税率（8%）')
  })

  it('すべての列挙値に表示名がある（コードそのものにならない）', () => {
    for (const role of ROLES) expect(enumLabel(t, 'Role', role)).not.toBe(role)
    for (const status of ORDER_STATUSES)
      expect(enumLabel(t, 'OrderStatus', status)).not.toBe(status)
    for (const tax of TAX_CATEGORIES) expect(enumLabel(t, 'TaxCategory', tax)).not.toBe(tax)
  })
})
