import { App as AntdApp, ConfigProvider } from 'antd'
import enUS from 'antd/locale/en_US'
import jaJP from 'antd/locale/ja_JP'
import zhCN from 'antd/locale/zh_CN'
import dayjs from 'dayjs'
import 'dayjs/locale/en'
import 'dayjs/locale/ja'
import 'dayjs/locale/zh-cn'
import { useEffect, type ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import { DEFAULT_LANGUAGE, isLanguage, type Language } from './index'

const ANTD_LOCALES = { ja: jaJP, zh: zhCN, en: enUS } as const
const DAYJS_LOCALES: Record<Language, string> = { ja: 'ja', zh: 'zh-cn', en: 'en' }

/** LocaleProvider の props。 */
interface LocaleProviderProps {
  children: ReactNode
}

/**
 * 言語に合わせて antd の locale と dayjs の locale を同期する（言語切り替え時も追随する）。
 * antd の `App`（message / modal の文脈）もここで提供する。
 *
 * @param props 子要素
 */
export function LocaleProvider({ children }: LocaleProviderProps) {
  const { i18n } = useTranslation()
  const language: Language = isLanguage(i18n.language) ? i18n.language : DEFAULT_LANGUAGE

  useEffect(() => {
    dayjs.locale(DAYJS_LOCALES[language])
    document.documentElement.lang = language
  }, [language])

  return (
    <ConfigProvider locale={ANTD_LOCALES[language]}>
      <AntdApp>{children}</AntdApp>
    </ConfigProvider>
  )
}
