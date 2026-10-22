import type { AxiosAdapter, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { AxiosError } from 'axios'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { clearAccessToken, setAccessToken } from '@/auth/token-storage'
import { ApiError, axiosInstance, fallbackMessageId, http, setUnauthorizedHandler } from '../http'

type Responder = (config: InternalAxiosRequestConfig) => Partial<AxiosResponse> & { status: number }

const originalAdapter = axiosInstance.defaults.adapter

function stub(responder: Responder): InternalAxiosRequestConfig[] {
  const seen: InternalAxiosRequestConfig[] = []
  const adapter: AxiosAdapter = (config) => {
    seen.push(config)
    const partial = responder(config)
    const response = {
      data: null,
      statusText: '',
      headers: {},
      ...partial,
      config,
    } as AxiosResponse
    if (response.status >= 200 && response.status < 300) return Promise.resolve(response)
    return Promise.reject(new AxiosError('failed', String(response.status), config, null, response))
  }
  axiosInstance.defaults.adapter = adapter
  return seen
}

const okBody = (data: unknown, extra: object = {}) => ({
  code: '0',
  message: 'success',
  data,
  ...extra,
})

const errorBody = (messageId: string, extra: object = {}) => ({
  messageId,
  message: 'サーバ既定文言',
  timestamp: '2026-10-22T10:00:00+09:00',
  path: '/api/v1/x',
  ...extra,
})

describe('http（axios ラッパー）', () => {
  beforeEach(() => {
    clearAccessToken()
    setUnauthorizedHandler(null)
  })
  afterEach(() => {
    axiosInstance.defaults.adapter = originalAdapter
  })

  it('保存済みの JWT を Authorization: Bearer で付与する', async () => {
    setAccessToken('jwt-value')
    const seen = stub(() => ({ status: 200, data: okBody(null) }))
    await http.get('/me')
    expect(seen[0]?.headers.get('Authorization')).toBe('Bearer jwt-value')
  })

  it('JWT がなければ Authorization を付けない', async () => {
    const seen = stub(() => ({ status: 200, data: okBody(null) }))
    await http.get('/me')
    expect(seen[0]?.headers.has('Authorization')).toBe(false)
  })

  it('basePath は /api/v1 である', () => {
    expect(axiosInstance.defaults.baseURL).toBe('/api/v1')
  })

  it('Result を展開して data と通知用の messageId を返す', async () => {
    stub(() => ({
      status: 200,
      data: okBody('E0001', { messageId: 'I0010', messageArgs: ['E0001'], traceId: 't-1' }),
    }))
    const result = await http.post<string>('/users', { userName: 'x' })
    expect(result).toEqual({
      data: 'E0001',
      messageId: 'I0010',
      messageArgs: ['E0001'],
      traceId: 't-1',
    })
  })

  it('HTTP 200 でも code が "0" 以外なら ApiError にする', async () => {
    stub(() => ({
      status: 200,
      data: { code: '1', message: 'ng', messageId: 'E0012', data: null },
    }))
    await expect(http.get('/x')).rejects.toMatchObject({ status: 200, messageId: 'E0012' })
  })

  it('エラー応答を ErrorResponse（messageId・details）付きの ApiError に変換する', async () => {
    stub(() => ({
      status: 400,
      data: errorBody('E0008', { details: [{ field: 'email', reason: '必須' }] }),
    }))
    const error = await http.post('/users', {}).catch((e: unknown) => e)
    expect(error).toBeInstanceOf(ApiError)
    expect((error as ApiError).status).toBe(400)
    expect((error as ApiError).error.details).toEqual([{ field: 'email', reason: '必須' }])
  })

  it('本文が ErrorResponse でない失敗はステータスから messageId を決める', async () => {
    stub(() => ({ status: 502, data: '<html>bad gateway</html>' }))
    await expect(http.get('/x')).rejects.toMatchObject({ status: 502, messageId: 'E0013' })
  })

  it('応答がない（接続不可）ときは status 0・E0013 にする', async () => {
    axiosInstance.defaults.adapter = () => Promise.reject(new AxiosError('Network Error'))
    await expect(http.get('/x')).rejects.toMatchObject({ status: 0, messageId: 'E0013' })
  })

  it.each([['E0001'], ['E0040'], ['E0041']])(
    '401 / %s で未認証ハンドラを呼ぶ',
    async (messageId) => {
      const handler = vi.fn()
      setUnauthorizedHandler(handler)
      stub(() => ({ status: 401, data: errorBody(messageId) }))
      await expect(http.get('/me')).rejects.toBeInstanceOf(ApiError)
      expect(handler).toHaveBeenCalledOnce()
    },
  )

  it('401 / E0004（Magic Link 無効）では未認証ハンドラを呼ばない', async () => {
    const handler = vi.fn()
    setUnauthorizedHandler(handler)
    stub(() => ({ status: 401, data: errorBody('E0004') }))
    await expect(http.post('/auth/magic-link/verify', { token: 'x' })).rejects.toMatchObject({
      messageId: 'E0004',
    })
    expect(handler).not.toHaveBeenCalled()
  })

  it('403 / 404 では未認証ハンドラを呼ばない', async () => {
    const handler = vi.fn()
    setUnauthorizedHandler(handler)
    stub(() => ({ status: 403, data: errorBody('E0002') }))
    await expect(http.get('/users')).rejects.toMatchObject({ status: 403 })
    expect(handler).not.toHaveBeenCalled()
  })

  it('download は Blob と Content-Disposition のファイル名を返す', async () => {
    const blob = new Blob(['pdf'])
    stub(() => ({
      status: 200,
      data: blob,
      headers: { 'content-disposition': "attachment; filename*=UTF-8''order_OD00000001.pdf" },
    }))
    const file = await http.download('/orders/OD00000001/pdf')
    expect(file.blob).toBe(blob)
    expect(file.fileName).toBe('order_OD00000001.pdf')
  })

  it('download のファイル名が読めなければ null を返す', async () => {
    stub(() => ({ status: 200, data: new Blob(['x']), headers: {} }))
    expect((await http.download('/x')).fileName).toBeNull()
  })

  it('download の失敗では Blob 本文の ErrorResponse を読む', async () => {
    const body = new Blob([JSON.stringify(errorBody('E0006'))], { type: 'application/json' })
    stub(() => ({ status: 409, data: body }))
    await expect(http.download('/attachments/AT00000001/download')).rejects.toMatchObject({
      status: 409,
      messageId: 'E0006',
    })
  })

  it.each([
    [401, 'E0001'],
    [403, 'E0002'],
    [404, 'E0005'],
    [500, 'E0012'],
    [504, 'E0043'],
  ])('fallbackMessageId(%i) = %s', (status, expected) => {
    expect(fallbackMessageId(status)).toBe(expected)
  })
})
