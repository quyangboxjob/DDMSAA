# Frontend 開発規約（spec-frontend-coding-rule）

> フロントエンド（React）層のアーキテクチャと実装規約。AI が任意のフロントエンドコードを生成する際は、**必ず**本ファイルを [`spec-naming-rule.md`](spec-naming-rule.md) と同時にプロンプトへ含めること。
> 本ファイルは「基本設計/詳細設計 → フロントエンドコード」間の実装層 SSOT である。

---

## メタデータ
- version: 1.0.0
- target: React / Vite / TypeScript（バージョンは `基本設計/共通/spec-01-system-architecture-ui.md` §2.1 に従う）
- created: 2026-06-11
- status: approved

---

## 0. 使用方法と優先順位

- AI がフロントエンドコードを生成する際は、**必ず**次を読み込み厳密に遵守する：本ファイル + `spec-naming-rule.md` + 当該機能の詳細設計 + `spec-07-api-list.yaml`（エンドポイント/スキーマ）+ `spec-09-message-list.yaml`（messageId）+ `spec-03-auth-ui.md`（ロール別表示）。
- **優先順位（競合時）**：[`spec-generation-rule.md`](spec-generation-rule.md) §1 に従う。命名は一律 `spec-naming-rule.md` を唯一の情報源とする。
- 型の項目、エンドポイント、列挙値は `spec-07-api-list.yaml` を正とし、表示文言は `spec-09-message-list.yaml` を正とする。**フロントエンドで項目を再定義せず、文言をハードコードしない**。

---

## 1. 技術スタックとディレクトリ構成

> **技術スタックとバージョンは `基本設計/共通/spec-01-system-architecture-ui.md` §2.1 を唯一の情報源とする**（React / Vite / TS strict / React Router / Ant Design / Context+Hooks / axios / Vitest+Testing Library / pnpm）。本節ではバージョンを重複宣言せず、実装層の約束事のみを示す：

| 実装層の約束事 | 説明 |
|------|------|
| コンポーネント | 関数コンポーネント + Hooks とし、class コンポーネントは禁止 |
| 型 | TS の `enum` は禁止。Union Type を用いる（§3 / spec-naming-rule §1.3） |
| データ取得 | **axios** ラッパーに統一：JWT の付与、`Result<T>` の展開、エラーの `ErrorResponse` 変換 |
| スタイル/コンポーネント | **Ant Design** コンポーネント + テーマ。必要に応じて CSS Modules |

**ディレクトリ構成**（詳細設計 §6 の出力先と整合）

```
frontend/src/
├── pages/<domain>/<feature>-page.tsx          ページ単位（ルーティングのマウント点）
├── components/<domain>/<feature>-<role>.tsx    子コンポーネント（Form/Table/Modal/Card…）
├── types/<domain>.ts                           型（OpenAPI スキーマに準拠）
├── api/<domain>-api.ts                         API client（operationId に準拠）
├── i18n/messages.ts                            messageId → 文言解決（messages.yaml 由来）
└── routes.tsx                                  ルーティング表
```

- ファイル名は一律 **kebab-case**（spec-naming-rule §1.2）：`user-list-page.tsx`、`user-detail-form.tsx`。

---

## 2. コンポーネントロール接尾辞（spec-naming-rule §2.2 の完全な定義元）

> spec-naming-rule は「コンポーネントロール接尾辞の完全な定義」を本規約に委ねている。完全な一覧は下記のとおり：

| 接尾辞 | 用途 | 例 |
|------|------|------|
| `Page` | ページ単位のコンポーネント（ルーティングのマウント、子コンポーネントの組み合わせ、ページ状態の保持） | `UserListPage` |
| `Table` | 一覧/テーブル表示 | `UserTable` |
| `Form` | 入力フォーム | `UserDetailForm` |
| `Modal` | モーダルダイアログ（確認/二次操作） | `RetireConfirmModal` |
| `Card` | カード形式の表示 | `DashboardCard` |
| `Panel` | ブロック/区画コンテナ | `DashboardPanel` |

<!-- rule: fe.component-role-suffix -->
- コンポーネント名 = 単数形の業務名 + ロール接尾辞（PascalCase）。ファイル名 = 同名の kebab-case。
- 1 ファイルにつき主コンポーネントは 1 つとし、export はファイル名に対応させる。

---

## 3. 型定義

- 型は `spec-07-api-list.yaml` の `components.schemas` に準拠し、**項目名、null 許容性、列挙値を一対一で対応**させる。項目の追加/改名は行わない。
- **列挙は一律 Union Type を用い、TypeScript の `enum` は禁止**（spec-naming-rule §1.3）：

```typescript
// ✅ バックエンドの文字列と整合
type EmploymentType = "REGULAR" | "PARTTIME" | "CONTRACT"
type WorkStatus = "ACTIVE" | "RETIRED" | "CONTRACTED" | "OUT_OF_CONTRACT"
type Role = "ADMIN" | "MANAGER" | "USER"  // 例。ロールはプロジェクトで定義する
```

- レスポンスのエンベロープ型は API yaml に合わせる：`Result<T>` / `PageResult<T>` / `ErrorResponse`（`messageId` / `messageArgs` / `details[]` を含む）。

---

## 4. API client

- `<domain>-api.ts` を 1 つ設け、関数名は `operationId`（`spec-07-api-list.yaml`）に対応させる：`pageUsers`、`getUser`、`createUser`…
- 共通ラッパー：`Authorization: Bearer <token>` の付与、basePath `/api/v1`、`Result<T>.data` の展開、エラーの `ErrorResponse` 変換。
- **成功/失敗の通知は一律呼び出し側が `messageId` に基づいて行い**、api 層で直接ダイアログを表示しない。

```typescript
export async function createUser(dto: UserCreateDto): Promise<string> {
  const res = await http.post<Result<string>>("/users", dto)
  return res.data   // 失敗時は http 層が ErrorResponse を保持したエラーをスローする
}
```

---

## 5. 国際化 i18n（三言語：日/中/英）とメッセージ解決

- **i18n = react-i18next**。**すべての UI 文言（ラベル/メニュー/ボタン/表ヘッダー/列挙表示名 + バックエンドメッセージ）は必ず i18n を経由し、コンポーネント内でいかなる言語の文言もハードコードしない**。デフォルト言語は日本語とし、ページヘッダーで 日/中/英 を切り替え可能とする。
- **バックエンドメッセージ**：バックエンドは `messageId`(+`messageArgs`) のみを返し、フロントエンドが現在の言語に応じて i18n リソース（`09` の `messages.{ja,zh,en}.yaml` から同期）から解決する。`messageArgs` は `{0},{1}` プレースホルダーに従って置換する。
- **UI 文言/列挙表示名**：同様に i18n リソースで三言語を管理する（messageId とは名前空間を分ける）。
- 言語切り替え時は **antd locale**（ja_JP/zh_CN/en_US）と **dayjs locale** を同期して切り替える。

```typescript
// バックエンドメッセージ（messageId）：現在の言語で解決 + プレースホルダー
toast(t("I0010", { 0: userId }))      // ja: 登録しました（ID: U0001）
// UI 文言：
<Button>{t("ui.user.create")}</Button>
```

- 確認ダイアログ（W 系）、エラー（E 系）、情報（I 系）、UI ラベルはすべて同一の i18n を経由する。
- 日時の表示書式は `基本設計/共通/spec-12-screen-common-ui.md` に従い、表示層の共通ユーティリティを用いる。

---

## 6. 状態・データ取得

- ページ状態（list/query/page/role など）は Page コンポーネントまたはローカル store に保持し、子コンポーネントは props 経由で受け取る（**単方向データフロー**）。
- `role` は JWT の解析結果から取得し、**表示制御**（§8 参照）にのみ用い、セキュリティ境界としては扱わない。
- ローディング/エラー/空状態を明示的に処理し、未定義データのレンダリングを避ける。

---

## 7. クライアント検証（バックエンドと整合させる。UX 向けであり、バックエンドの代替ではない）

- 検証ルールは**バックエンドの Bean Validation / OpenAPI 制約に準拠**させる（必須/形式/長さ/列挙）。表示文言は `messageId` 経由とする（例：`E0030`〜`E0034`）。
- クライアント検証は UX の向上のみを目的とし、**バックエンド検証が最終的な権威**である。フロントエンドで検証済みと仮定してはならない。

---

## 8. ロール別表示制御（`spec-03-auth-ui.md` と整合）

- ロール（プロジェクトで定義する）に応じて、ボタン/項目/ブロックの表示を制御する（例：「新規登録」は ADMIN のみ、機微項目はマトリクスに従う）。
- **表示制御 ≠ セキュリティ制御**：非表示は UX のためのみであり、実際のデータ範囲と項目マスキングはバックエンドが強制する（詳細項目はサーバー側で権限に応じて null に設定済みであり、フロントエンドは null かどうかでレンダリングすればよい）。

---

## 9. 画面遷移・ルーティング

- ルーティングは `routes.tsx` に集約し、パスと画面 ID（SCR-xx）をトレース可能にする。
- 遷移関係は詳細設計の「画面遷移」表に従う（例：SCR-01 →行クリック→ SCR-02 参照）。

---

## 10. コード生成順序と出力先

**生成順序**（依存されるものを先に）：

```
types(<domain>.ts) → api(<domain>-api.ts) → 子コンポーネント(Form/Table/Modal/Card) → Page → ルーティング(routes.tsx)
```

**出力先**：`frontend/src/{types,api,components,pages}/<domain>/…` + `routes.tsx`。

> **ファイル規約**：改行・文字コード（`.gitattributes` / `.editorconfig` を含む）は `開発規約/spec-backend-coding-rule.md` §12 に従う（フロントエンド・バックエンド共通）。

---

## 10.1 コメント・TSDoc 規約（必須）

- **すべての export 対象（`export`）の関数/コンポーネント/Hook/型**に TSDoc（`/** ... */`）を付与する：責務の一文説明 + `@param`/`@returns`（関数の場合）。
- React コンポーネント：用途を説明する。主要な props は `type`/`interface` の各項目にコメントを付ける。
- API 関数（`<domain>-api.ts`）：対応する `operationId` と戻り値の型を明記する。
- 例外：自明な内部の小関数は省略可。**情報量のないプレースホルダーコメントは禁止**。
- コード生成時に TSDoc も同時に出力する（事後補完ではない）。機械検証は §11 G-tsdoc を参照。

---

## 11. 生成後の検証ゲート（機械検査可能）

| ゲート | チェック | 照合先 |
|----|------|------|
| G-型 | type の項目/null 許容/列挙がスキーマと一致。`enum` キーワードなし | `spec-07-api-list.yaml` + spec-naming-rule §1.3 |
| G-API | api 関数名 = operationId、パス/メソッドが一致 | `spec-07-api-list.yaml` |
| G-msg | 表示する messageId がすべて存在し、ハードコード文言がない | `spec-09-message-list.yaml` |
| G-命名 | ファイルは kebab、コンポーネントは PascalCase + ロール接尾辞 | `spec-naming-rule.md` §1/§2 + 本ファイル §2 |
| G-権限 | ロール別表示制御が項目マトリクスと一致。表示をセキュリティと見なしていない | `spec-03-auth-ui.md` |
| G-改行 | ファイルがファイル規約（改行・文字コード）に適合する | §10 / `開発規約/spec-backend-coding-rule.md` §12 |
| G-tsdoc | export する関数/コンポーネント/Hook/型 すべてに TSDoc あり（関数は `@param`/`@returns` を含む） | 本ファイル §10.1 |

---

## 12. AI プロンプトテンプレート

```
## 前提（必ずすべて読み込み、厳密に遵守すること）
- spec-frontend-coding-rule.md（本ファイル）+ spec-naming-rule.md
- 当該機能の詳細設計（フロントエンド節 + 画面遷移）
- spec-07-api-list.yaml（当該機能のエンドポイント/スキーマ）/ spec-09-message-list.yaml / spec-03-auth-ui.md

## タスク
§10 の生成順序に従ってフロントエンドコードをファイルごとに生成する。型/エンドポイント/文言は SSOT に厳密に合わせる。
生成後、§11 の検証ゲートを項目ごとにすべて通過させること。
```

---

## 改訂履歴

| バージョン | 日付 | 作成者 | 変更内容 |
|------|------|------|---------|
| 1.0.0 | 2026-06-11 | AI 草案 | 初版（Union Type 必須、messageId 解決、表示≠セキュリティ） |
