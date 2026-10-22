/**
 * ルーティング表（spec-frontend-coding-rule §9）。パスと画面 ID（SCR-xx）は `config/route-defs.ts`
 * のとおりにトレースできる。画面は feature ごとに生成し、`pageComponents` へ登録する。
 */
import type { ComponentType } from 'react'
import { Navigate, useRoutes, type RouteObject } from 'react-router-dom'
import { RequireAuth } from '@/auth/require-auth'
import { ROUTE_DEFS, type ScreenId } from '@/config/route-defs'
import { BareLayout } from '@/layouts/bare-layout'
import { MainLayout } from '@/layouts/main-layout'
import { ErrorPage } from '@/pages/common/error-page'
import { PlaceholderPage } from '@/pages/common/placeholder-page'

/**
 * 実装済みの画面コンポーネント。feature 生成時にここへ追加する（未登録の画面は PlaceholderPage）。
 * 例：`'SCR-11': OrderListPage`
 */
export const pageComponents: Partial<Record<ScreenId, ComponentType>> = {
  'SCR-19': () => <ErrorPage status="403" messageId="E0002" />,
  'SCR-20': () => <ErrorPage status="404" messageId="E0005" />,
}

function pageFor(screenId: ScreenId) {
  const Page = pageComponents[screenId]
  return Page ? <Page /> : <PlaceholderPage screenId={screenId} />
}

/** 全ルート。公開（SCR-01 / SCR-21）は BareLayout、それ以外は認証必須の MainLayout。 */
export const routes: RouteObject[] = [
  {
    element: <BareLayout />,
    children: ROUTE_DEFS.filter((route) => route.roles === 'public').map((route) => ({
      path: route.path,
      element: pageFor(route.screenId),
    })),
  },
  {
    element: <MainLayout />,
    children: [
      ...ROUTE_DEFS.filter((route) => route.roles !== 'public').map((route) => ({
        path: route.path,
        element: (
          <RequireAuth
            roles={route.errorPage ? 'any' : route.roles === 'public' ? [] : route.roles}
          >
            {pageFor(route.screenId)}
          </RequireAuth>
        ),
      })),
      // 未定義パスは SCR-20 へ（未ログインなら RequireAuth が SCR-01 へ送る）。
      {
        path: '*',
        element: (
          <RequireAuth roles="any">
            <Navigate to="/404" replace />
          </RequireAuth>
        ),
      },
    ],
  },
]

/** ルート表を描画する。Router と AuthProvider の内側で使う。 */
export function AppRoutes() {
  return useRoutes(routes)
}
