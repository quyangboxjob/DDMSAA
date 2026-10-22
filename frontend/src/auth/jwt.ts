/** JWT ペイロードの読み取り。署名は検証しない（検証は API Gateway の責務。表示制御専用）。 */
import { ROLES, type Role } from '@/types/enums'

/** 表示制御に使う JWT クレーム。 */
export interface JwtClaims {
  /** ユーザー ID（`sub`）。 */
  userId: string
  /** ロール（`role`）。 */
  role: Role
  /** 有効期限（`exp`、エポック秒）。 */
  expiresAt: number
}

/**
 * JWT のペイロードを読み取る。
 *
 * @param token JWT
 * @returns クレーム。形式が不正、またはロールが未知なら `null`
 */
export function decodeJwt(token: string): JwtClaims | null {
  const part = token.split('.')[1]
  if (!part) return null
  try {
    const base64 = part.replace(/-/g, '+').replace(/_/g, '/')
    const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=')
    const bytes = Uint8Array.from(atob(padded), (c) => c.charCodeAt(0))
    const payload = JSON.parse(new TextDecoder().decode(bytes)) as {
      sub?: unknown
      role?: unknown
      exp?: unknown
    }
    if (typeof payload.sub !== 'string' || typeof payload.exp !== 'number') return null
    if (!ROLES.includes(payload.role as Role)) return null
    return { userId: payload.sub, role: payload.role as Role, expiresAt: payload.exp }
  } catch {
    return null
  }
}

/**
 * JWT が有効期限内かを判定する（期限切れの JWT を送らないための事前チェック）。
 *
 * @param claims decodeJwt の結果
 * @param nowMs 現在時刻（ミリ秒）。テストで差し替える
 * @returns 期限内なら `true`
 */
export function isUnexpired(claims: JwtClaims, nowMs: number = Date.now()): boolean {
  return claims.expiresAt * 1000 > nowMs
}
