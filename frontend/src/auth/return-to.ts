/**
 * ログイン前の画面への復帰（spec-12 §7、spec-10）。Magic Link は別タブで開かれるため、
 * 遷移先を localStorage に保存時刻付きで保存し、SCR-21 の認証成功後に読み出す。
 */

/** localStorage のキー。 */
export const RETURN_TO_KEY = 'ddmsaa.returnTo'

/** 保存の有効時間（ミリ秒）。JWT の有効期限と同じ 60 分。 */
export const RETURN_TO_TTL_MS = 60 * 60 * 1000

/** 復帰先にしない画面（SCR-01 / SCR-21 自身）。 */
const EXCLUDED_PATHS = ['/login', '/auth/verify']

interface StoredReturnTo {
  path: string
  savedAt: number
}

/**
 * 遷移先（クエリを含む）を保存する。復帰先にできないパスは保存しない。
 *
 * @param path 遷移先のパス（クエリを含む）
 * @param nowMs 保存時刻（ミリ秒）。テストで差し替える
 */
export function saveReturnTo(path: string, nowMs: number = Date.now()): void {
  if (!isReturnable(path)) return
  try {
    const value: StoredReturnTo = { path, savedAt: nowMs }
    window.localStorage.setItem(RETURN_TO_KEY, JSON.stringify(value))
  } catch {
    // 保存できなくても復帰できないだけで、ログイン自体には影響しない。
  }
}

/**
 * 保存済みの遷移先を読み出して削除する。保存から 60 分以内で、呼び出し側が許可したパスだけ返す。
 *
 * @param isAllowed 利用者のロールでそのパスに入れるかの判定
 * @param nowMs 現在時刻（ミリ秒）。テストで差し替える
 * @returns 復帰先のパス。該当しなければ `null`（読み出し後は常に削除する）
 */
export function consumeReturnTo(
  isAllowed: (path: string) => boolean,
  nowMs: number = Date.now(),
): string | null {
  let stored: StoredReturnTo | null = null
  try {
    const raw = window.localStorage.getItem(RETURN_TO_KEY)
    window.localStorage.removeItem(RETURN_TO_KEY)
    stored = raw ? (JSON.parse(raw) as StoredReturnTo) : null
  } catch {
    return null
  }
  if (!stored || typeof stored.path !== 'string' || typeof stored.savedAt !== 'number') return null
  if (nowMs - stored.savedAt > RETURN_TO_TTL_MS) return null
  if (!isReturnable(stored.path) || !isAllowed(stored.path)) return null
  return stored.path
}

/**
 * 復帰先にできるパスかを判定する（アプリ内の絶対パスで、SCR-01 / SCR-21 以外）。
 *
 * @param path 判定するパス（クエリを含む）
 * @returns 復帰先にできるなら `true`
 */
export function isReturnable(path: string): boolean {
  if (!path.startsWith('/') || path.startsWith('//')) return false
  const pathname = path.split(/[?#]/)[0] ?? ''
  return !EXCLUDED_PATHS.includes(pathname)
}
