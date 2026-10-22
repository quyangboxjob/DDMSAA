import { BrowserRouter } from 'react-router-dom'
import { AuthProvider } from '@/auth/auth-provider'
import { LocaleProvider } from '@/i18n/locale-provider'
import { AppRoutes } from '@/routes'

/** アプリのルート。Context（言語・認証）と Router を組み立てる（状態管理は Context + Hooks）。 */
export function App() {
  return (
    <LocaleProvider>
      <BrowserRouter>
        <AuthProvider>
          <AppRoutes />
        </AuthProvider>
      </BrowserRouter>
    </LocaleProvider>
  )
}
