/** 認証 API の型（spec-07-api-list.yaml の auth タグ / MeVO）。 */
import type { Role } from './enums'

/** ログイン開始・Magic Link 再送のリクエスト（loginInit / resendMagicLink）。 */
export interface LoginInitRequest {
  userId: string
}

/** Magic Link 検証のリクエスト（verifyMagicLink）。 */
export interface MagicLinkVerifyRequest {
  token: string
}

/** JWT 発行応答（verifyMagicLink）。 */
export interface TokenResponse {
  accessToken: string
  tokenType: string
}

/** ログイン利用者情報（getMe）。 */
export interface MeVO {
  userId: string
  userName: string
  role: Role
  /** CUSTOMER のみ。 */
  customerId: string | null
  customerName: string | null
}
