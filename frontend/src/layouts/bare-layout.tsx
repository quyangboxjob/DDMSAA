import { Layout } from 'antd'
import { Outlet } from 'react-router-dom'
import { LanguageSwitcher } from '@/components/common/language-switcher'

/**
 * ナビゲーションとユーザーメニューを出さないレイアウト（SCR-01 ログイン・SCR-21 ログイン認証用。
 * spec-12 §1）。言語の切り替えだけを右上に置く。
 */
export function BareLayout() {
  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Layout.Content style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
        <div style={{ alignSelf: 'flex-end', padding: 16 }}>
          <LanguageSwitcher />
        </div>
        <div style={{ width: '100%', maxWidth: 480, padding: 24 }}>
          <Outlet />
        </div>
      </Layout.Content>
    </Layout>
  )
}
