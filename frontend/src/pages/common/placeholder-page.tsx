import { Empty, Typography } from 'antd'
import { useTranslation } from 'react-i18next'
import type { ScreenId } from '@/config/route-defs'

/** PlaceholderPage の props。 */
interface PlaceholderPageProps {
  screenId: ScreenId
}

/**
 * 未実装画面の受け皿。feature の画面を生成したら `routes.tsx` の `pageComponents` に登録して置き換える。
 *
 * @param props 画面 ID
 */
export function PlaceholderPage({ screenId }: PlaceholderPageProps) {
  const { t } = useTranslation()
  return (
    <div data-testid={`placeholder-${screenId}`}>
      <Typography.Title level={3}>
        {screenId} {t(`ui.screens.${screenId}`)}
      </Typography.Title>
      <Empty description={t('ui.common.notImplemented')} />
    </div>
  )
}
