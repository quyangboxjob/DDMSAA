import { Button, Result } from 'antd'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router-dom'

/** ErrorPage の props。 */
interface ErrorPageProps {
  /** `403`（SCR-19 権限エラー）または `404`（SCR-20 未存在エラー）。 */
  status: '403' | '404'
  /** 表示する messageId（403 は E0002、404 は E0005）。 */
  messageId: 'E0002' | 'E0005'
}

/**
 * 権限エラー（SCR-19）・未存在エラー（SCR-20）の共通表示。認証済みの利用者にだけ表示する。
 *
 * @param props ステータスと messageId
 */
export function ErrorPage({ status, messageId }: ErrorPageProps) {
  const { t } = useTranslation()
  const titleKey = status === '403' ? 'ui.errorPages.forbiddenTitle' : 'ui.errorPages.notFoundTitle'
  return (
    <Result
      status={status}
      title={t(titleKey)}
      subTitle={t(messageId)}
      extra={
        <Link to="/">
          <Button type="primary">{t('ui.errorPages.toHome')}</Button>
        </Link>
      }
    />
  )
}
