import { Spin } from 'antd'
import { useEffect, type ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import type { Role } from '@/types/enums'
import { useAuth } from './auth-context'
import { saveReturnTo } from './return-to'

/** RequireAuth の props。 */
interface RequireAuthProps {
  /** 許可ロール。`'any'` は認証済みなら誰でも可（SCR-19 / SCR-20 など）。 */
  roles: readonly Role[] | 'any'
  children: ReactNode
}

/**
 * ルートガード（spec-12 §7）。未ログインは遷移先を `ddmsaa.returnTo` に保存して SCR-01 へ、
 * ログイン済みでロールが許可されない場合は SCR-19 へ遷移させる。
 * ガードは表示制御のみで、最終的な可否は API が判定する。
 *
 * @param props 許可ロールと子要素
 */
export function RequireAuth({ roles, children }: RequireAuthProps) {
  const { status, role } = useAuth()
  const location = useLocation()
  const unauthenticated = status === 'unauthenticated' || (status === 'authenticated' && !role)

  useEffect(() => {
    if (unauthenticated) saveReturnTo(`${location.pathname}${location.search}`)
  }, [unauthenticated, location.pathname, location.search])

  if (status === 'loading') {
    return <Spin fullscreen />
  }
  if (unauthenticated || role === null) {
    return <Navigate to="/login" replace />
  }
  if (roles !== 'any' && !roles.includes(role)) {
    return <Navigate to="/403" replace />
  }
  return <>{children}</>
}
