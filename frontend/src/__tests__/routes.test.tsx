import { render, screen, waitFor } from '@testing-library/react'
import { AxiosError, type AxiosAdapter } from 'axios'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { axiosInstance, http } from '@/api/http'
import { AuthProvider } from '@/auth/auth-provider'
import { RETURN_TO_KEY } from '@/auth/return-to'
import { getAccessToken, setAccessToken } from '@/auth/token-storage'
import '@/i18n'
import { LocaleProvider } from '@/i18n/locale-provider'
import type { MeVO } from '@/types/auth'
import type { Role } from '@/types/enums'
import { AppRoutes } from '../routes'

const getMeMock = vi.hoisted(() => vi.fn())
vi.mock('@/api/auth-api', () => ({ getMe: getMeMock }))

function jwtFor(role: Role, exp = Math.floor(Date.now() / 1000) + 3600): string {
  const encode = (value: object) => btoa(JSON.stringify(value)).replace(/=+$/, '')
  return `${encode({ alg: 'HS256' })}.${encode({ sub: 'U1', role, exp })}.sig`
}

const me = (role: Role): MeVO => ({
  userId: 'U1',
  userName: 'テスト利用者',
  role,
  customerId: role === 'CUSTOMER' ? 'CS00000001' : null,
  customerName: role === 'CUSTOMER' ? 'サンプル商事' : null,
})

function renderAt(path: string) {
  return render(
    <LocaleProvider>
      <MemoryRouter initialEntries={[path]}>
        <AuthProvider>
          <AppRoutes />
        </AuthProvider>
      </MemoryRouter>
    </LocaleProvider>,
  )
}

function signIn(role: Role) {
  setAccessToken(jwtFor(role))
  getMeMock.mockResolvedValue(me(role))
}

describe('ルーティングとルートガード（spec-12 §7）', () => {
  beforeEach(() => {
    getMeMock.mockReset()
  })

  it('未ログインで保護された画面へ行くと SCR-01 へ送り、遷移先を returnTo に保存する', async () => {
    renderAt('/orders?status=DRAFT')
    expect(await screen.findByTestId('placeholder-SCR-01')).toBeInTheDocument()
    expect(window.localStorage.getItem(RETURN_TO_KEY)).toContain('/orders?status=DRAFT')
  })

  it('SCR-01 は未ログインで表示でき、ナビゲーションを出さない', async () => {
    renderAt('/login')
    expect(await screen.findByTestId('placeholder-SCR-01')).toBeInTheDocument()
    expect(screen.queryByRole('menu')).not.toBeInTheDocument()
  })

  it('期限切れの JWT は破棄して SCR-01 へ送る', async () => {
    setAccessToken(jwtFor('ADMIN', 1))
    renderAt('/users')
    expect(await screen.findByTestId('placeholder-SCR-01')).toBeInTheDocument()
    expect(getAccessToken()).toBeNull()
    expect(getMeMock).not.toHaveBeenCalled()
  })

  it('許可ロールの画面を表示する（ADMIN → SCR-04）', async () => {
    signIn('ADMIN')
    renderAt('/users')
    expect(await screen.findByTestId('placeholder-SCR-04')).toBeInTheDocument()
  })

  it('許可されないロールは SCR-19（権限エラー）へ送る', async () => {
    signIn('EMPLOYEE')
    renderAt('/users')
    expect(await screen.findByText('この画面にはアクセスできません')).toBeInTheDocument()
  })

  it('未定義パスは SCR-20（未存在エラー）へ送る', async () => {
    signIn('ADMIN')
    renderAt('/no-such-page')
    expect(await screen.findByText('ページが見つかりません')).toBeInTheDocument()
  })

  it('未ログインで未定義パスへ行くと SCR-01 へ送る', async () => {
    renderAt('/no-such-page')
    expect(await screen.findByTestId('placeholder-SCR-01')).toBeInTheDocument()
  })

  it('メニューはロール別に出し分ける（EMPLOYEE は承認待ち・ユーザーを出さない）', async () => {
    signIn('EMPLOYEE')
    renderAt('/')
    expect(await screen.findByRole('link', { name: '商品' })).toBeInTheDocument()
    expect(screen.queryByRole('link', { name: '承認待ち' })).not.toBeInTheDocument()
    expect(screen.queryByRole('link', { name: 'ユーザー' })).not.toBeInTheDocument()
  })

  it('CUSTOMER のメニューは自社情報を自分の顧客 ID の詳細へ向ける', async () => {
    signIn('CUSTOMER')
    renderAt('/')
    const link = await screen.findByRole('link', { name: '自社情報' })
    expect(link).toHaveAttribute('href', '/customers/CS00000001')
    expect(screen.queryByRole('link', { name: '顧客' })).not.toBeInTheDocument()
  })
})

describe('セッション切れ（401 / E0001）', () => {
  const original = axiosInstance.defaults.adapter
  beforeEach(() => getMeMock.mockReset())
  afterEach(() => {
    axiosInstance.defaults.adapter = original
  })

  it('JWT を破棄し、遷移先を保存して SCR-01 へ戻す', async () => {
    signIn('ADMIN')
    renderAt('/users')
    expect(await screen.findByTestId('placeholder-SCR-04')).toBeInTheDocument()

    const adapter: AxiosAdapter = (config) =>
      Promise.reject(
        new AxiosError('unauthorized', '401', config, null, {
          status: 401,
          statusText: '',
          headers: {},
          config,
          data: {
            messageId: 'E0001',
            message: 'expired',
            timestamp: '2026-10-22T10:00:00+09:00',
            path: '/api/v1/users',
          },
        }),
      )
    axiosInstance.defaults.adapter = adapter
    await http.get('/users').catch(() => undefined)

    await waitFor(() => expect(screen.getByTestId('placeholder-SCR-01')).toBeInTheDocument())
    expect(getAccessToken()).toBeNull()
    expect(window.localStorage.getItem(RETURN_TO_KEY)).toContain('/users')
  })
})
