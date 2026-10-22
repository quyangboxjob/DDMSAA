/**
 * 認証 API（spec-07-api-list.yaml の auth タグ・getMe）。関数名は operationId に一致させる。
 * `/auth/**` は auth-service、`/me` は business-service-core が処理する（Gateway がルーティング）。
 */
import type { LoginInitRequest, MagicLinkVerifyRequest, MeVO, TokenResponse } from '@/types/auth'
import { http, type ApiSuccess } from './http'

/**
 * ログイン開始（operationId: loginInit）。可否に関わらず常に成功し、Magic Link を送信する（I0001）。
 *
 * @param request ユーザー ID
 * @returns 成功応答（messageId = I0001）
 */
export function loginInit(request: LoginInitRequest): Promise<ApiSuccess<null>> {
  return http.post<null>('/auth/login-init', request)
}

/**
 * Magic Link の検証と JWT 発行（operationId: verifyMagicLink）。無効・期限切れは 401 / E0004。
 *
 * @param request メールのリンクに含まれるワンタイムトークン
 * @returns JWT
 */
export async function verifyMagicLink(request: MagicLinkVerifyRequest): Promise<TokenResponse> {
  return (await http.post<TokenResponse>('/auth/magic-link/verify', request)).data
}

/**
 * Magic Link の再送（operationId: resendMagicLink）。常に成功する（I0001）。
 *
 * @param request ユーザー ID
 * @returns 成功応答（messageId = I0001）
 */
export function resendMagicLink(request: LoginInitRequest): Promise<ApiSuccess<null>> {
  return http.post<null>('/auth/magic-link/resend', request)
}

/**
 * ログイン利用者情報の取得（operationId: getMe）。
 *
 * @returns 利用者情報（ユーザー ID・表示名・ロール・顧客）
 */
export async function getMe(): Promise<MeVO> {
  return (await http.get<MeVO>('/me')).data
}
