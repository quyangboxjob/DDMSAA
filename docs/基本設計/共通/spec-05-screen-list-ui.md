<!-- devtool:file-region id="document-header" -->
# 05 画面一覧

> システムの**全画面の索引**。画面 ID（`SCR-`）はここで採番する。各画面の項目・レイアウトは `spec-16-screen-design-ui.md`、遷移は `spec-10-screen-transition-ui.md`、共通 UI 方針は `spec-12-screen-common-ui.md` を参照し、本書では再定義しない。
> **帰属：システム全体で唯一 → `共通/`**。

（文書ヘッダは `spec-00-doc-standard-ui.md` を参照）

---

<!-- devtool:end-file-region -->
<!-- devtool:block id="basic.common.05-screen-list-ui.s01" -->
## 1. 画面一覧

> ID 形式は `spec-00-doc-standard-ui.md` §3（`SCR-99`）に従う。廃止画面は行を残しステータスを「廃止」とする。

| 画面 ID | 画面名 | 種別 | URL パス | 関連機能 ID | 利用ロール | 主な API（operationId） | 画面設計 | ステータス |
|------|------|------|------|------|------|------|------|------|
| SCR-01 | ログイン | 認証 | `/login` | F-AUTH-01 | 全ロール（未認証） | `login` | §〇 | 新規 |
| SCR-02 | 〇〇一覧 | 一覧 | `/〇〇` | F-〇〇-01 | R-〇〇 | `page〇〇` | §〇 | 新規 |
| | | | | | | | | |

| 列 | 記入要領 |
|------|------|
| 種別 | 認証 / ダッシュボード / 一覧 / 詳細 / 登録・編集 / 確認 / 完了 / ポップアップ / エラー |
| URL パス | フロントエンドのルーティングパス。形式はプロジェクトで定める（例：kebab-case、API リソース名に対応させる）。`routes.tsx` で画面 ID とトレース可能にする（`spec-frontend-coding-rule.md` §9） |
| 利用ロール | `spec-03-auth-ui.md` と一致させる。権限外はメニュー非表示（`spec-12` §7） |
| 主な API | `spec-07-api-list.yaml` の `operationId` を参照する |

<!-- devtool:endblock -->
<!-- devtool:block id="basic.common.05-screen-list-ui.s02" -->
## 2. 画面 × ロール アクセスマトリクス

> 権限の正本は `spec-03-auth-ui.md`（操作権限・データ範囲・項目参照）。本表は画面単位の到達可否のみを示し、`spec-03` §2 および `spec-02` §3 と矛盾させない。

| 画面 ID | R-〇〇 | R-〇〇 | R-〇〇 | 未認証 |
|------|:---:|:---:|:---:|:---:|
| SCR-01 | ○ | ○ | ○ | ○ |
| SCR-02 | ○ | × | × | × |

<!-- devtool:endblock -->
<!-- devtool:block id="basic.common.05-screen-list-ui.s03" -->
## 3. 画面 × API マトリクス（任意）

> 画面で使用する API の漏れ確認用。`operationId` が `spec-07` に存在することを確認する。

| 画面 ID | 使用 API |
|------|------|
| SCR-02 | `page〇〇`, `delete〇〇` |

<!-- devtool:endblock -->
<!-- devtool:block id="basic.common.05-screen-list-ui.s04" -->
## 4. ナビゲーション（メニュー）構成

> ロールごとのメニュー階層。共通レイアウトは `spec-12` §1 に従う。

```
〇〇システム
├── ダッシュボード        (SCR-〇〇)  [R-〇〇, R-〇〇]
├── 〇〇管理
│   ├── 〇〇一覧          (SCR-02)    [R-〇〇]
│   └── 〇〇登録          (SCR-03)    [R-〇〇]
└── 個人設定              (SCR-〇〇)  [全ロール]
```

<!-- devtool:endblock -->
