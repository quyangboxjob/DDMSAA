# frontend

React 19 + TypeScript（strict）+ Vite + Ant Design + React Router + react-i18next。技術スタックとバージョンは `docs/基本設計/共通/spec-01-system-architecture-ui.md` §2.1、実装規約は `docs/開発規約/spec-frontend-coding-rule.md` を正とする。

## 使い方

```bash
pnpm install
pnpm dev            # http://localhost:3000（/api は VITE_DEV_PROXY_TARGET の api-gateway へ中継）
pnpm typecheck
pnpm lint
pnpm test           # vitest（jsdom）。JUnit XML は test-results/junit.xml
pnpm build
pnpm sync:messages  # spec-09 のメッセージカタログから src/i18n/generated/*.json を再生成
```

環境変数は `.env.example` を `.env.local` に複製して設定する（秘密値は置かない）。

## 構成

| パス | 内容 |
|---|---|
| `src/types/` | `common.ts`（Result / PageResult / ErrorResponse）、`enums.ts`（T07 の 13 列挙、Union Type）、`auth.ts` |
| `src/api/` | `http.ts`（JWT 付与・Result 展開・ErrorResponse 変換・401 処理・ダウンロード）、`auth-api.ts`（operationId どおり） |
| `src/auth/` | JWT 保存・解読、`ddmsaa.returnTo`、AuthProvider、ルートガード |
| `src/i18n/` | 3 言語（日/中/英）、messageId 解決、列挙の表示名、antd / dayjs locale 同期 |
| `src/config/` | `route-defs.ts`（画面 ID・パス・許可ロール）、`menu.ts`（ロール別メニュー） |
| `src/layouts/`・`src/routes.tsx` | 共通レイアウトとルーティング表。未実装の画面は PlaceholderPage |
| `src/utils/format.ts` | 日付・日時（日本時間）・数量・金額の表示 |

## feature 画面の追加

1. `types/<domain>.ts` → `api/<domain>-api.ts` → `components/<domain>/…` → `pages/<domain>/…` の順に生成する。
2. `routes.tsx` の `pageComponents` に画面 ID で登録する（パス・許可ロールは `config/route-defs.ts`）。
3. 文言は i18n 経由とし、ハードコードしない。UI ラベルは `src/i18n/locales/ui.ja.ts` に追加する。

## 未決事項

- JWT の保存場所は暫定で sessionStorage（TBD-U4。変更は `src/auth/token-storage.ts` だけ）。
- 画面ラベルの中・英は未提供で、日本語にフォールバックする（TBD-S1）。
- 金額の小数表示は API が返した桁のまま（TBD-S2）。
