import { Breadcrumb, Button, Layout, Menu, Space, Typography } from 'antd'
import { useMemo } from 'react'
import { useTranslation } from 'react-i18next'
import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '@/auth/auth-context'
import { findRouteDef } from '@/auth/route-access'
import { LanguageSwitcher } from '@/components/common/language-switcher'
import { menuItemsFor } from '@/config/menu'
import { enumLabel } from '@/i18n/enum-label'

const { Header, Sider, Content, Footer } = Layout

/** 本システムのコンテンツ最大幅（spec-12 §1 の提案値）。 */
const CONTENT_MAX_WIDTH = 1440

/**
 * 認証後の共通レイアウト（spec-12 §1）。固定ヘッダー（システム名・利用者・言語・ログアウト）、
 * ロール別の左ナビゲーション、パンくず、フッターを持つ。
 */
export function MainLayout() {
  const { t } = useTranslation()
  const { me, role, logout } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()

  const items = useMemo(() => {
    if (!role) return []
    return menuItemsFor(role).map((item) => {
      // 「自社情報」は利用者の顧客 ID の詳細画面（SCR-07）。
      const path =
        item.path === '/customers/:own' ? `/customers/${me?.customerId ?? ''}` : item.path
      return { key: path, label: <Link to={path}>{t(item.labelKey)}</Link> }
    })
  }, [role, me?.customerId, t])

  const selectedKey = useMemo(() => {
    const matches = items
      .map((item) => item.key)
      .filter((key) =>
        key === '/' ? location.pathname === '/' : location.pathname.startsWith(key),
      )
    return matches.sort((a, b) => b.length - a.length)[0]
  }, [items, location.pathname])

  const current = findRouteDef(location.pathname)
  const breadcrumbs = [
    { title: <Link to="/">{t('ui.menu.home')}</Link> },
    ...(current && current.screenId !== 'SCR-02' ? [{ title: t(current.titleKey) }] : []),
  ]

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Header style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Typography.Title level={4} style={{ color: '#fff', margin: 0 }}>
          {t('ui.app.title')}
        </Typography.Title>
        <Space size="middle">
          <LanguageSwitcher />
          {me && role && (
            <Typography.Text style={{ color: '#fff' }}>
              {me.userName}（{enumLabel(t, 'Role', role)}）
            </Typography.Text>
          )}
          <Button size="small" onClick={handleLogout}>
            {t('ui.common.logout')}
          </Button>
        </Space>
      </Header>
      <Layout>
        <Sider width={220} theme="light">
          <Menu mode="inline" items={items} selectedKeys={selectedKey ? [selectedKey] : []} />
        </Sider>
        <Layout style={{ padding: '0 24px 24px' }}>
          <Breadcrumb items={breadcrumbs} style={{ margin: '16px 0' }} />
          <Content style={{ maxWidth: CONTENT_MAX_WIDTH }}>
            <Outlet />
          </Content>
          <Footer style={{ textAlign: 'center' }}>
            {t('ui.app.footer')} / v{__APP_VERSION__}
          </Footer>
        </Layout>
      </Layout>
    </Layout>
  )
}
