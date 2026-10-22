/** ルート定義に基づくアクセス判定（表示制御専用。最終的な可否は API が判定する）。 */
import { matchPath } from 'react-router-dom'
import { ROUTE_DEFS, type RouteDef } from '@/config/route-defs'
import type { Role } from '@/types/enums'

/**
 * パスに対応するルート定義を探す。
 *
 * @param pathname クエリを含まないパス
 * @returns ルート定義。未定義なら `undefined`
 */
export function findRouteDef(pathname: string): RouteDef | undefined {
  return ROUTE_DEFS.find((route) => matchPath({ path: route.path, end: true }, pathname) !== null)
}

/**
 * ロールがそのパスへ入れるかを判定する（`ddmsaa.returnTo` の検証に使う）。
 *
 * @param path パス（クエリを含んでよい）
 * @param role 利用者のロール
 * @returns 定義済みで、ロールが許可されていれば `true`
 */
export function canAccessPath(path: string, role: Role): boolean {
  const pathname = path.split(/[?#]/)[0] ?? ''
  const route = findRouteDef(pathname)
  if (!route || route.roles === 'public') return false
  return route.roles.includes(role)
}
