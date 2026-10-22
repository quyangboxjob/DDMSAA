/**
 * ルート定義（spec-05-screen-list-ui.md §1・§2 の画面 ID・パス・アクセスマトリクスに一致させる）。
 * ここは表示制御用の宣言であり、最終的な可否は API が判定する（spec-12 §7）。
 */
import type { Role } from '@/types/enums'

/** 画面 ID（SCR-03 は廃止のため欠番）。 */
export type ScreenId =
  | 'SCR-01'
  | 'SCR-02'
  | 'SCR-04'
  | 'SCR-05'
  | 'SCR-06'
  | 'SCR-07'
  | 'SCR-08'
  | 'SCR-09'
  | 'SCR-10'
  | 'SCR-11'
  | 'SCR-12'
  | 'SCR-13'
  | 'SCR-14'
  | 'SCR-15'
  | 'SCR-16'
  | 'SCR-17'
  | 'SCR-18'
  | 'SCR-19'
  | 'SCR-20'
  | 'SCR-21'

/** ルート 1 件の定義。 */
export interface RouteDef {
  screenId: ScreenId
  /** react-router のパス（パラメータは `:name`）。 */
  path: string
  /** 画面名の i18n キー（`ui.screens.<SCR-xx>`）。 */
  titleKey: string
  /** 許可ロール。`'public'` は未認証でも到達できる（SCR-01 / SCR-21）。 */
  roles: readonly Role[] | 'public'
  /** 認証済みなら誰でも表示できるエラー画面（SCR-19 / SCR-20）。 */
  errorPage?: boolean
}

const ALL: readonly Role[] = ['ADMIN', 'EMPLOYEE', 'CUSTOMER']
const ADMIN: readonly Role[] = ['ADMIN']
const STAFF: readonly Role[] = ['ADMIN', 'EMPLOYEE']

const def = (
  screenId: ScreenId,
  path: string,
  roles: RouteDef['roles'],
  extra: Partial<RouteDef> = {},
): RouteDef => ({ screenId, path, titleKey: `ui.screens.${screenId}`, roles, ...extra })

/** 全ルート定義。 */
export const ROUTE_DEFS: readonly RouteDef[] = [
  def('SCR-01', '/login', 'public'),
  def('SCR-21', '/auth/verify', 'public'),
  def('SCR-02', '/', ALL),
  def('SCR-04', '/users', ADMIN),
  def('SCR-05', '/users/new', ADMIN),
  def('SCR-05', '/users/:userId/edit', ADMIN),
  def('SCR-06', '/customers', STAFF),
  def('SCR-08', '/customers/new', ADMIN),
  def('SCR-08', '/customers/:customerId/edit', ADMIN),
  def('SCR-07', '/customers/:customerId', ALL),
  def('SCR-09', '/products', STAFF),
  def('SCR-10', '/products/new', STAFF),
  def('SCR-10', '/products/:productId/edit', STAFF),
  def('SCR-11', '/orders', ALL),
  def('SCR-12', '/orders/new', STAFF),
  def('SCR-12', '/orders/:orderId/edit', STAFF),
  def('SCR-13', '/orders/:orderId', ALL),
  def('SCR-14', '/approvals', ADMIN),
  def('SCR-15', '/approval-rules', ADMIN),
  def('SCR-16', '/reports', STAFF),
  def('SCR-17', '/audit-logs', ADMIN),
  def('SCR-18', '/notification-logs', ADMIN),
  def('SCR-19', '/403', ALL, { errorPage: true }),
  def('SCR-20', '/404', ALL, { errorPage: true }),
]
