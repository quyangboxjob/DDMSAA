/**
 * axios ラッパー（spec-frontend-coding-rule §4）。JWT の付与、`Result<T>` の展開、エラーの
 * `ErrorResponse` 変換を共通化する。成功・失敗の通知は呼び出し側が messageId に基づいて行い、
 * この層ではダイアログを表示しない。
 */
import axios, { AxiosError, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { getAccessToken } from '@/auth/token-storage'
import type { ErrorResponse, Result } from '@/types/common'

/** 401 のうち「JWT を破棄して SCR-01 へ戻す」messageId（spec-12 §3）。 */
export const SESSION_EXPIRED_MESSAGE_IDS: readonly string[] = ['E0001', 'E0040', 'E0041']

/** 成功応答（`Result.data` と、通知に使う messageId）。 */
export interface ApiSuccess<T> {
  data: T
  /** 表示用メッセージ ID（I / W）。なければ通知しない。 */
  messageId?: string
  messageArgs?: string[]
  traceId?: string
}

/** ファイルダウンロードの結果。 */
export interface DownloadedFile {
  blob: Blob
  /** Content-Disposition から読めたファイル名。読めなければ `null`（呼び出し側が命名する）。 */
  fileName: string | null
}

/** API 失敗を表す例外。`error` に ErrorResponse（messageId 等）を保持する。 */
export class ApiError extends Error {
  /** HTTP ステータス。接続できないときは 0。 */
  readonly status: number
  readonly error: ErrorResponse

  constructor(status: number, error: ErrorResponse) {
    super(error.message || error.messageId)
    this.name = 'ApiError'
    this.status = status
    this.error = error
  }

  /** エラーメッセージ ID（E）。 */
  get messageId(): string {
    return this.error.messageId
  }
}

type UnauthorizedHandler = (error: ApiError) => void
let unauthorizedHandler: UnauthorizedHandler | null = null

/**
 * セッション切れ（401 / E0001・E0040・E0041）のときに呼ぶ処理を登録する。
 * AuthProvider が JWT の破棄と SCR-01 への遷移を登録する。
 *
 * @param handler 登録する処理。解除は `null`
 */
export function setUnauthorizedHandler(handler: UnauthorizedHandler | null): void {
  unauthorizedHandler = handler
}

/** 既定の API basePath（`VITE_API_BASE_URL` で上書きできる）。 */
export const API_BASE_URL: string = import.meta.env.VITE_API_BASE_URL ?? '/api/v1'

/** 共通の axios インスタンス。 */
export const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30_000,
  headers: { Accept: 'application/json' },
})

axiosInstance.interceptors.request.use((config) => {
  const token = getAccessToken()
  if (token) config.headers.set('Authorization', `Bearer ${token}`)
  return config
})

/**
 * 応答が得られないとき・本文が ErrorResponse でないときの代替 messageId。
 *
 * @param status HTTP ステータス（接続不可は 0）
 * @returns messageId
 */
export function fallbackMessageId(status: number): string {
  switch (status) {
    case 401:
      return 'E0001'
    case 403:
      return 'E0002'
    case 404:
      return 'E0005'
    case 502:
      return 'E0013'
    case 504:
      return 'E0043'
    case 0:
      return 'E0013'
    default:
      return 'E0012'
  }
}

function isErrorResponse(body: unknown): body is ErrorResponse {
  return (
    typeof body === 'object' &&
    body !== null &&
    typeof (body as { messageId?: unknown }).messageId === 'string'
  )
}

function readBlobText(blob: Blob): Promise<string> {
  if (typeof blob.text === 'function') return blob.text()
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(String(reader.result))
    reader.onerror = () => reject(reader.error)
    reader.readAsText(blob)
  })
}

async function readErrorBody(response: AxiosResponse): Promise<unknown> {
  const body: unknown = response.data
  if (body instanceof Blob) {
    try {
      return JSON.parse(await readBlobText(body))
    } catch {
      return null
    }
  }
  return body
}

/**
 * axios のエラーを ApiError に変換する。ダウンロード（blob）の失敗本文も読み取る。
 *
 * @param error axios が投げた値
 * @returns ApiError
 */
export async function toApiError(error: unknown): Promise<ApiError> {
  if (error instanceof ApiError) return error
  if (error instanceof AxiosError && error.response) {
    const { status, config } = error.response
    const body = await readErrorBody(error.response)
    if (isErrorResponse(body)) return new ApiError(status, body)
    return new ApiError(status, {
      messageId: fallbackMessageId(status),
      message: error.message,
      timestamp: new Date().toISOString(),
      path: config?.url ?? '',
    })
  }
  const message = error instanceof Error ? error.message : String(error)
  return new ApiError(0, {
    messageId: fallbackMessageId(0),
    message,
    timestamp: new Date().toISOString(),
    path: '',
  })
}

function notifyIfSessionExpired(apiError: ApiError): void {
  if (apiError.status === 401 && SESSION_EXPIRED_MESSAGE_IDS.includes(apiError.messageId)) {
    unauthorizedHandler?.(apiError)
  }
}

async function request<T>(config: AxiosRequestConfig): Promise<ApiSuccess<T>> {
  try {
    const response = await axiosInstance.request<Result<T>>(config)
    const body = response.data
    if (body.code !== '0') {
      // HTTP 200 でも code が "0" 以外なら失敗として扱う（Result の契約）。
      throw new ApiError(response.status, {
        messageId: body.messageId ?? 'E0012',
        messageArgs: body.messageArgs,
        message: body.message,
        timestamp: body.timestamp ?? new Date().toISOString(),
        path: config.url ?? '',
        traceId: body.traceId,
      })
    }
    return {
      data: body.data,
      messageId: body.messageId,
      messageArgs: body.messageArgs,
      traceId: body.traceId,
    }
  } catch (error) {
    const apiError = await toApiError(error)
    notifyIfSessionExpired(apiError)
    throw apiError
  }
}

function parseFileName(disposition: unknown): string | null {
  if (typeof disposition !== 'string') return null
  const encoded = /filename\*=UTF-8''([^;]+)/i.exec(disposition)
  if (encoded?.[1]) {
    try {
      return decodeURIComponent(encoded[1])
    } catch {
      return null
    }
  }
  const plain = /filename="?([^";]+)"?/i.exec(disposition)
  return plain?.[1] ?? null
}

/**
 * Result で包まないファイル応答を取得する（添付・PDF・CSV のダウンロード）。
 * ストレージの URL は使わず、API 経由で取得する（spec-12 §8）。
 *
 * @param url API パス（basePath 以降）
 * @param params クエリパラメータ
 * @returns Blob と、読めた場合のファイル名
 */
async function download(url: string, params?: object): Promise<DownloadedFile> {
  try {
    const response = await axiosInstance.get<Blob>(url, { params, responseType: 'blob' })
    return {
      blob: response.data,
      fileName: parseFileName(response.headers['content-disposition']),
    }
  } catch (error) {
    const apiError = await toApiError(error)
    notifyIfSessionExpired(apiError)
    throw apiError
  }
}

/** 共通 HTTP クライアント。各 `<domain>-api.ts` はこれを通して呼び出す。 */
export const http = {
  /** GET。 */
  get: <T>(url: string, params?: object) => request<T>({ method: 'GET', url, params }),
  /** POST（JSON）。 */
  post: <T>(url: string, body?: unknown) => request<T>({ method: 'POST', url, data: body }),
  /** PUT（JSON）。 */
  put: <T>(url: string, body?: unknown) => request<T>({ method: 'PUT', url, data: body }),
  /** PATCH（JSON）。 */
  patch: <T>(url: string, body?: unknown) => request<T>({ method: 'PATCH', url, data: body }),
  /** DELETE。 */
  delete: <T>(url: string, params?: object) => request<T>({ method: 'DELETE', url, params }),
  /** multipart/form-data のアップロード（ブラウザが boundary を付ける）。 */
  upload: <T>(url: string, form: FormData) =>
    request<T>({
      method: 'POST',
      url,
      data: form,
      headers: { 'Content-Type': 'multipart/form-data' },
    }),
  /** ファイルのダウンロード。 */
  download,
}
