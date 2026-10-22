import { Select } from 'antd'
import { useTranslation } from 'react-i18next'
import { changeLanguage, isLanguage, SUPPORTED_LANGUAGES, type Language } from '@/i18n'

/**
 * 言語（日/中/英）の切り替え。切り替えると i18n・antd locale・dayjs locale が追随する。
 */
export function LanguageSwitcher() {
  const { t, i18n } = useTranslation()
  const current: Language = isLanguage(i18n.language) ? i18n.language : 'ja'

  return (
    <Select<Language>
      aria-label={t('ui.common.language')}
      size="small"
      value={current}
      style={{ width: 110 }}
      options={SUPPORTED_LANGUAGES.map((language) => ({
        value: language,
        label: t(`ui.languages.${language}`),
      }))}
      onChange={(language) => void changeLanguage(language)}
    />
  )
}
