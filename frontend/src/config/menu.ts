/** ナビゲーションメニュー（spec-05-screen-list-ui.md §4）。権限のない項目は表示しない。 */
import type { Role } from '@/types/enums'
import type { ScreenId } from './route-defs'

/** メニュー項目。 */
export interface MenuItem {
  /** 遷移先の画面 ID。 */
  screenId: ScreenId
  path: string
  /** i18n キー。 */
  labelKey: string
  roles: readonly Role[]
}

/** 全メニュー項目（表示順）。 */
export const MENU_ITEMS: readonly MenuItem[] = [
  {
    screenId: 'SCR-02',
    path: '/',
    labelKey: 'ui.menu.home',
    roles: ['ADMIN', 'EMPLOYEE', 'CUSTOMER'],
  },
  {
    screenId: 'SCR-11',
    path: '/orders',
    labelKey: 'ui.menu.orders',
    roles: ['ADMIN', 'EMPLOYEE', 'CUSTOMER'],
  },
  { screenId: 'SCR-14', path: '/approvals', labelKey: 'ui.menu.approvals', roles: ['ADMIN'] },
  {
    screenId: 'SCR-06',
    path: '/customers',
    labelKey: 'ui.menu.customers',
    roles: ['ADMIN', 'EMPLOYEE'],
  },
  {
    screenId: 'SCR-07',
    path: '/customers/:own',
    labelKey: 'ui.menu.ownCompany',
    roles: ['CUSTOMER'],
  },
  {
    screenId: 'SCR-09',
    path: '/products',
    labelKey: 'ui.menu.products',
    roles: ['ADMIN', 'EMPLOYEE'],
  },
  {
    screenId: 'SCR-16',
    path: '/reports',
    labelKey: 'ui.menu.reports',
    roles: ['ADMIN', 'EMPLOYEE'],
  },
  { screenId: 'SCR-04', path: '/users', labelKey: 'ui.menu.users', roles: ['ADMIN'] },
  {
    screenId: 'SCR-15',
    path: '/approval-rules',
    labelKey: 'ui.menu.approvalRules',
    roles: ['ADMIN'],
  },
  { screenId: 'SCR-17', path: '/audit-logs', labelKey: 'ui.menu.auditLogs', roles: ['ADMIN'] },
  {
    screenId: 'SCR-18',
    path: '/notification-logs',
    labelKey: 'ui.menu.notificationLogs',
    roles: ['ADMIN'],
  },
]

/**
 * ロールに表示するメニュー項目を返す。
 *
 * @param role ログイン利用者のロール
 * @returns 表示順のメニュー項目
 */
export function menuItemsFor(role: Role): MenuItem[] {
  return MENU_ITEMS.filter((item) => item.roles.includes(role))
}
