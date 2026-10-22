import { useCallback, useEffect, useMemo, useRef, useState, type ReactNode } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { getMe } from '@/api/auth-api'
import { setUnauthorizedHandler } from '@/api/http'
import type { MeVO } from '@/types/auth'
import { AuthContext, type AuthContextValue, type AuthStatus } from './auth-context'
import { decodeJwt, isUnexpired } from './jwt'
import { saveReturnTo } from './return-to'
import { clearAccessToken, getAccessToken, setAccessToken } from './token-storage'

/** AuthProvider の props。 */
interface AuthProviderProps {
  children: ReactNode
}

/**
 * 認証状態を提供する。起動時に保存済み JWT の期限を確認して利用者情報（getMe）を取得し、
 * API がセッション切れ（401 / E0001・E0040・E0041）を返したら JWT を破棄して SCR-01 へ戻す。
 * Router の内側に置く。
 *
 * @param props 子要素
 */
export function AuthProvider({ children }: AuthProviderProps) {
  const navigate = useNavigate()
  const location = useLocation()
  const [status, setStatus] = useState<AuthStatus>('loading')
  const [me, setMe] = useState<MeVO | null>(null)
  const locationRef = useRef(location)
  locationRef.current = location

  const logout = useCallback(() => {
    clearAccessToken()
    setMe(null)
    setStatus('unauthenticated')
  }, [])

  const login = useCallback(async (accessToken: string): Promise<MeVO> => {
    setAccessToken(accessToken)
    try {
      const loaded = await getMe()
      setMe(loaded)
      setStatus('authenticated')
      return loaded
    } catch (error) {
      clearAccessToken()
      setMe(null)
      setStatus('unauthenticated')
      throw error
    }
  }, [])

  // 起動時：保存済みの JWT が期限内なら利用者情報を取得する。
  useEffect(() => {
    const token = getAccessToken()
    const claims = token ? decodeJwt(token) : null
    if (!token || !claims || !isUnexpired(claims)) {
      clearAccessToken()
      setStatus('unauthenticated')
      return
    }
    let cancelled = false
    getMe()
      .then((loaded) => {
        if (cancelled) return
        setMe(loaded)
        setStatus('authenticated')
      })
      .catch(() => {
        if (cancelled) return
        clearAccessToken()
        setStatus('unauthenticated')
      })
    return () => {
      cancelled = true
    }
  }, [])

  // セッション切れ：JWT を破棄し、遷移先を保存して SCR-01 へ（E0001 を表示させる）。
  useEffect(() => {
    setUnauthorizedHandler((error) => {
      const current = locationRef.current
      saveReturnTo(`${current.pathname}${current.search}`)
      logout()
      navigate('/login', { replace: true, state: { messageId: error.messageId } })
    })
    return () => setUnauthorizedHandler(null)
  }, [logout, navigate])

  const value = useMemo<AuthContextValue>(
    () => ({ status, me, role: me?.role ?? null, login, logout }),
    [status, me, login, logout],
  )
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
