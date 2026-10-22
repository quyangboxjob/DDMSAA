import { createContext, useContext } from 'react'
import type { MeVO } from '@/types/auth'
import type { Role } from '@/types/enums'

/** 認証状態。`loading` は起動時の JWT 検証・利用者情報の取得中。 */
export type AuthStatus = 'loading' | 'authenticated' | 'unauthenticated'

/** 認証コンテキストの値。 */
export interface AuthContextValue {
  status: AuthStatus
  /** ログイン利用者。未認証・取得中は `null`。 */
  me: MeVO | null
  /** 表示制御に使うロール（セキュリティ境界ではない）。未認証は `null`。 */
  role: Role | null
  /**
   * Magic Link 検証で得た JWT を保存し、利用者情報を取得する（SCR-21 が呼ぶ）。
   *
   * @param accessToken verifyMagicLink が返した JWT
   * @returns ログイン利用者
   */
  login: (accessToken: string) => Promise<MeVO>
  /** JWT を破棄して未認証にする。遷移はしない。 */
  logout: () => void
}

/** 認証コンテキスト。AuthProvider が提供する。 */
export const AuthContext = createContext<AuthContextValue | null>(null)

/**
 * 認証コンテキストを取得する。
 *
 * @returns 認証コンテキストの値
 * @throws AuthProvider の外で呼んだ場合
 */
export function useAuth(): AuthContextValue {
  const value = useContext(AuthContext)
  if (!value) throw new Error('useAuth must be used within AuthProvider')
  return value
}
