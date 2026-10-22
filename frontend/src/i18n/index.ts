/**
 * i18n の初期化（react-i18next）。すべての UI 文言とバックエンドの messageId は i18n を経由する。
 * 既定言語は日本語。言語は localStorage に保存し、ヘッダーで日/中/英を切り替える。
 */
import i18n from 'i18next'
import { initReactI18next } from 'react-i18next'
import messagesEn from './generated/messages.en.json'
import messagesJa from './generated/messages.ja.json'
import messagesZh from './generated/messages.zh.json'
import { uiJa } from './locales/ui.ja'

/** 対応言語。 */
export const SUPPORTED_LANGUAGES = ['ja', 'zh', 'en'] as const
export type Language = (typeof SUPPORTED_LANGUAGES)[number]

/** 既定言語。 */
export const DEFAULT_LANGUAGE: Language = 'ja'

/** 言語の保存キー（localStorage）。 */
export const LANGUAGE_STORAGE_KEY = 'ddmsaa.language'

/**
 * 言語として有効な値かを判定する。
 *
 * @param value 判定する値
 * @returns 対応言語なら `true`
 */
export function isLanguage(value: unknown): value is Language {
  return SUPPORTED_LANGUAGES.includes(value as Language)
}

/**
 * 保存済みの言語を読み出す。
 *
 * @returns 保存済みの言語。なければ既定言語
 */
export function loadLanguage(): Language {
  try {
    const stored = window.localStorage.getItem(LANGUAGE_STORAGE_KEY)
    return isLanguage(stored) ? stored : DEFAULT_LANGUAGE
  } catch {
    return DEFAULT_LANGUAGE
  }
}

/**
 * 言語を切り替えて保存する。
 *
 * @param language 切り替え先の言語
 */
export async function changeLanguage(language: Language): Promise<void> {
  try {
    window.localStorage.setItem(LANGUAGE_STORAGE_KEY, language)
  } catch {
    // 保存できなくても切り替えは行う。
  }
  await i18n.changeLanguage(language)
}

// messageId（I/W/E）は翻訳リソースの最上位、UI 文言は `ui.*` に置く（名前空間は 1 つ）。
// `{0}` のプレースホルダーは同期スクリプトが `{{0}}` に変換済み。
void i18n.use(initReactI18next).init({
  resources: {
    ja: { translation: { ...messagesJa, ui: uiJa } },
    zh: { translation: { ...messagesZh, ui: {} } },
    en: { translation: { ...messagesEn, ui: {} } },
  },
  lng: loadLanguage(),
  fallbackLng: DEFAULT_LANGUAGE,
  supportedLngs: [...SUPPORTED_LANGUAGES],
  interpolation: { escapeValue: false },
})

export default i18n
