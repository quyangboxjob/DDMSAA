import { beforeEach, describe, expect, it } from 'vitest'
import { decodeJwt, isUnexpired } from '../jwt'
import {
  consumeReturnTo,
  isReturnable,
  RETURN_TO_KEY,
  RETURN_TO_TTL_MS,
  saveReturnTo,
} from '../return-to'
import { canAccessPath, findRouteDef } from '../route-access'
import {
  ACCESS_TOKEN_KEY,
  clearAccessToken,
  getAccessToken,
  setAccessToken,
} from '../token-storage'

function makeJwt(payload: object): string {
  const encode = (value: object) => {
    const bytes = new TextEncoder().encode(JSON.stringify(value))
    const binary = Array.from(bytes, (b) => String.fromCharCode(b)).join('')
    return btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '')
  }
  return `${encode({ alg: 'HS256' })}.${encode(payload)}.signature`
}

describe('JWT の読み取り', () => {
  it('sub・role・exp を読み取る', () => {
    const claims = decodeJwt(makeJwt({ sub: 'E0001', role: 'EMPLOYEE', exp: 1_900_000_000 }))
    expect(claims).toEqual({ userId: 'E0001', role: 'EMPLOYEE', expiresAt: 1_900_000_000 })
  })

  it('日本語を含むペイロードも読める', () => {
    const claims = decodeJwt(makeJwt({ sub: 'A0001', role: 'ADMIN', exp: 1, name: '管理者' }))
    expect(claims?.userId).toBe('A0001')
  })

  it.each([
    ['形式が不正', 'not-a-jwt'],
    ['ロールが未知', makeJwt({ sub: 'X1', role: 'ROOT', exp: 1 })],
    ['exp がない', makeJwt({ sub: 'E0001', role: 'ADMIN' })],
    ['ペイロードが JSON でない', 'a.b!!.c'],
  ])('%s の場合は null を返す', (_name, token) => {
    expect(decodeJwt(token)).toBeNull()
  })

  it('有効期限を判定する', () => {
    const claims = { userId: 'E0001', role: 'EMPLOYEE' as const, expiresAt: 1000 }
    expect(isUnexpired(claims, 999_000)).toBe(true)
    expect(isUnexpired(claims, 1_000_000)).toBe(false)
  })
})

describe('JWT の保存（sessionStorage）', () => {
  beforeEach(() => clearAccessToken())

  it('保存・取得・破棄ができる', () => {
    expect(getAccessToken()).toBeNull()
    setAccessToken('abc')
    expect(getAccessToken()).toBe('abc')
    expect(window.sessionStorage.getItem(ACCESS_TOKEN_KEY)).toBe('abc')
    clearAccessToken()
    expect(getAccessToken()).toBeNull()
  })
})

describe('ログイン前の画面への復帰（ddmsaa.returnTo）', () => {
  const now = 1_000_000_000_000
  const allowAll = () => true

  it('クエリを含むパスを保存時刻付きで保存し、読み出すと削除する', () => {
    saveReturnTo('/orders?status=DRAFT', now)
    expect(window.localStorage.getItem(RETURN_TO_KEY)).toContain('/orders?status=DRAFT')
    expect(consumeReturnTo(allowAll, now + 1000)).toBe('/orders?status=DRAFT')
    expect(window.localStorage.getItem(RETURN_TO_KEY)).toBeNull()
  })

  it('保存から 60 分を超えると復帰しない（読み出しで削除する）', () => {
    saveReturnTo('/orders', now)
    expect(consumeReturnTo(allowAll, now + RETURN_TO_TTL_MS + 1)).toBeNull()
    expect(window.localStorage.getItem(RETURN_TO_KEY)).toBeNull()
  })

  it('ちょうど 60 分なら復帰する', () => {
    saveReturnTo('/orders', now)
    expect(consumeReturnTo(allowAll, now + RETURN_TO_TTL_MS)).toBe('/orders')
  })

  it('ロールに許可されないパスへは復帰しない', () => {
    saveReturnTo('/users', now)
    expect(consumeReturnTo(() => false, now)).toBeNull()
  })

  it('未保存なら null', () => {
    expect(consumeReturnTo(allowAll, now)).toBeNull()
  })

  it('壊れた保存値は null を返して削除する', () => {
    window.localStorage.setItem(RETURN_TO_KEY, '{broken')
    expect(consumeReturnTo(allowAll, now)).toBeNull()
    expect(window.localStorage.getItem(RETURN_TO_KEY)).toBeNull()
  })

  it.each([
    ['/login', false],
    ['/login?x=1', false],
    ['/auth/verify?token=abc', false],
    ['//evil.example.com', false],
    ['https://evil.example.com', false],
    ['/orders/OD00000001', true],
  ])('isReturnable(%s) = %s', (path, expected) => {
    expect(isReturnable(path)).toBe(expected)
  })

  it('SCR-01 / SCR-21 のパスは保存しない', () => {
    saveReturnTo('/login', now)
    saveReturnTo('/auth/verify?token=abc', now)
    expect(window.localStorage.getItem(RETURN_TO_KEY)).toBeNull()
  })
})

describe('ルートのアクセス判定（spec-05 §2）', () => {
  it('パスから画面を引ける（パラメータ付きを含む）', () => {
    expect(findRouteDef('/orders/OD00000001')?.screenId).toBe('SCR-13')
    expect(findRouteDef('/orders/new')?.screenId).toBe('SCR-12')
    expect(findRouteDef('/customers/new')?.screenId).toBe('SCR-08')
    expect(findRouteDef('/customers/CS00000001')?.screenId).toBe('SCR-07')
    expect(findRouteDef('/nowhere')).toBeUndefined()
  })

  it.each([
    ['/users', 'ADMIN', true],
    ['/users', 'EMPLOYEE', false],
    ['/customers', 'EMPLOYEE', true],
    ['/customers', 'CUSTOMER', false],
    ['/customers/CS00000001', 'CUSTOMER', true],
    ['/orders/new', 'CUSTOMER', false],
    ['/orders/OD00000001', 'CUSTOMER', true],
    ['/approvals', 'EMPLOYEE', false],
    ['/reports', 'EMPLOYEE', true],
    ['/audit-logs', 'ADMIN', true],
    ['/login', 'ADMIN', false],
    ['/nowhere', 'ADMIN', false],
  ] as const)('canAccessPath(%s, %s) = %s', (path, role, expected) => {
    expect(canAccessPath(path, role)).toBe(expected)
  })
})
