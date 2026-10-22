/**
 * JWT のブラウザ内保存。TBD-U4（メモリのみ／sessionStorage）は未決のため、暫定で sessionStorage
 * を使う（再読込でもログインを維持し、タブを閉じると破棄される）。保存場所を変えるときは
 * このファイルだけを差し替える。ストレージが使えない環境ではメモリに退避する。
 */

/** sessionStorage のキー。 */
export const ACCESS_TOKEN_KEY = 'ddmsaa.accessToken'

let memoryToken: string | null = null

/**
 * 保存済みの JWT を返す。
 *
 * @returns JWT。未保存なら `null`
 */
export function getAccessToken(): string | null {
  try {
    return window.sessionStorage.getItem(ACCESS_TOKEN_KEY)
  } catch {
    return memoryToken
  }
}

/**
 * JWT を保存する。
 *
 * @param token 保存する JWT
 */
export function setAccessToken(token: string): void {
  try {
    window.sessionStorage.setItem(ACCESS_TOKEN_KEY, token)
  } catch {
    // ストレージが使えない場合はメモリのみで保持する。
    memoryToken = token
  }
}

/** 保存済みの JWT を破棄する。 */
export function clearAccessToken(): void {
  memoryToken = null
  try {
    window.sessionStorage.removeItem(ACCESS_TOKEN_KEY)
  } catch {
    // 何もしない。
  }
}
