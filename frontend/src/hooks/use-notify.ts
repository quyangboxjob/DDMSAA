import { App as AntdApp } from 'antd'
import { useCallback, useMemo } from 'react'
import { useTranslation } from 'react-i18next'
import { resolveErrorMessage, resolveMessage } from '@/i18n/messages'

/** 通知用の操作（antd の message / modal を messageId 経由で使う）。 */
export interface Notifier {
  /**
   * 情報メッセージ（I）を Toast で表示する（3 秒で自動消去）。
   *
   * @param messageId メッセージ ID
   * @param messageArgs 置換値
   */
  info: (messageId: string, messageArgs?: readonly string[]) => void
  /**
   * API エラーを Toast で表示する。エラー Toast は自動消去しない（spec-12 §3）。
   *
   * @param error 捕捉した値
   */
  error: (error: unknown) => void
  /**
   * 確認ダイアログ（W）を表示する。
   *
   * @param messageId W 区分のメッセージ ID
   * @param messageArgs 置換値
   * @returns OK なら `true`、キャンセルなら `false`
   */
  confirm: (messageId: string, messageArgs?: readonly string[]) => Promise<boolean>
}

/**
 * messageId に基づいて通知を出す。api 層は通知せず、呼び出し側がこの Hook を使う
 * （spec-frontend-coding-rule §4）。LocaleProvider（antd の App）の内側で使う。
 *
 * @returns 通知操作
 */
export function useNotify(): Notifier {
  const { t } = useTranslation()
  const { message, modal } = AntdApp.useApp()

  const info = useCallback(
    (messageId: string, messageArgs?: readonly string[]) => {
      void message.success({ content: resolveMessage(t, messageId, messageArgs), duration: 3 })
    },
    [message, t],
  )

  const error = useCallback(
    (cause: unknown) => {
      void message.error({ content: resolveErrorMessage(t, cause), duration: 0 })
    },
    [message, t],
  )

  const confirm = useCallback(
    (messageId: string, messageArgs?: readonly string[]) =>
      new Promise<boolean>((resolve) => {
        modal.confirm({
          content: resolveMessage(t, messageId, messageArgs),
          okText: t('ui.common.ok'),
          cancelText: t('ui.common.cancel'),
          onOk: () => resolve(true),
          onCancel: () => resolve(false),
        })
      }),
    [modal, t],
  )

  return useMemo(() => ({ info, error, confirm }), [info, error, confirm])
}
