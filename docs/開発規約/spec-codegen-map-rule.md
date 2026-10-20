<!-- devtool:file-region id="document-header" -->
# Codegen Spec Map（フロント・バックエンド一頁早見表）

> spec-wf-codegen によるコード生成時に、「どの成果物がどの規約に従い、どの SSOT を読み、どのゲートを通すか」を示す一頁索引である。
> 本ファイルは**ナビゲーション**のみを担い、規則は再定義しない。規則は参照先の各ファイルを正とする。

<!-- devtool:end-file-region -->
<!-- devtool:block id="rules.codegen-map-rule.s01" -->
## 0. パイプライン上の位置

```
要件定義 → 基本設計 → 詳細設計(機能単位) → スキャフォールド(第0ステップ) → ★ spec-wf-codegen(コード生成) → UT/アップロード
                                                                  ↑ 本マップはこのステップを対象とする
```

<!-- devtool:endblock -->
<!-- devtool:block id="rules.codegen-map-rule.s02" -->
## 1. 権威順序（競合時の優先順位）

競合時の優先順位は [`spec-generation-rule.md`](spec-generation-rule.md) §1 を正とする（本マップでは再掲しない）。

<!-- devtool:endblock -->
<!-- devtool:block id="rules.codegen-map-rule.s03" -->
## 2. SSOT データソース（項目/エンドポイント/メッセージの出所）

| SSOT | 提供内容 | バックエンド | フロント |
|------|------|:---:|:---:|
| `spec-06-db-physical.yaml` | テーブル・列・型・制約・encrypted | ✅ Entity/Mapper/Flyway | — |
| `spec-07-api-list.yaml` | エンドポイント・operationId・schema・列挙 | ✅ DTO/Controller | ✅ types/api |
| `spec-09-message-list.{yaml,zh,en}` | messageId・3言語文言 | ✅ MessageResolver/例外 | ✅ i18n |
| `spec-03-auth-ui.md` | ロール×データ範囲×項目マトリクス | ✅ データ範囲/マスキング | ✅ 表示制御 |

<!-- devtool:endblock -->
<!-- devtool:block id="rules.codegen-map-rule.s04" -->
## 3. バックエンド（spec-backend-coding-rule.md が主）

| 成果物 | 規約の出所 | テンプレート（spec-wf-codegen/assets/）| データソース |
|------|---------|------|--------|
| 列挙 | §2 + 07 enum↔06 INT | `enum.java.tmpl` | 06/07.yaml |
| Entity | §2(POJO・camelCase↔snake_case) + §5(主キー・外部キー) + §11(単数 PascalCase) | `entity.java.tmpl` | 06.yaml |
| Mapper(インタフェース+XML) | §6(論理削除/**resultType+underscore**/PageHelper/文字列連結禁止) + `@Mapper` | `mapper.interface.tmpl`・`mapper.xml.tmpl` | 06.yaml |
| DTO | §2(schema に整合・Entity の公開禁止。入力は record／マスキング出力は可変クラス) | `dto.input-record.tmpl`・`dto.detail-class.tmpl` | 07.yaml |
| Converter | §2(**MapStruct `@Mapper`**・列挙マッピング) | `converter.java.tmpl` | — |
| ServiceImpl | §2 + §7(トランザクションはここのみ・複数テーブルのアトミック性・副作用はコミット後) + §4(BusinessException をスロー) + §9(マスキングはここで実施・DataMaskUtil) | `service.interface.tmpl`・`service-impl.java.tmpl` | — |
| Controller | §2(REST の受付のみ/Result で包む/業務ロジック禁止) + §8(@PreAuthorize) + §11(パスは複数形 kebab) | `controller.java.tmpl` | 07.yaml |
| Batch(任意) | §7 + 01 §2(Spring Scheduler) | `batch.java.tmpl` | — |
| Flyway DDL | spec-database-rule §4(id/監査列/deleted_at/`trg_set_updated_at_<table>`) + `spec-flyway-migration-rule.md` §2・R-01(命名・適用済みは変更不可) | （コード生成対象外）| 06.yaml |

> テンプレート = コードの骨格（How。参照実装から抽出）、規約の出所 = 規則（What/Why）。**競合時は規約を正とする**。SSOT の境界は `spec-wf-codegen/assets/README.md` を参照。

- **生成順序**（§12）：`Entity → Mapper → DTO → Converter → [採番/ドメインサービス] → ServiceImpl → Controller → Batch`
- **生成後の検証ゲート**（§13）：G-列 / G-API / G-msg / G-命名 / G-アーキテクチャ / G-秘密鍵 / G-改行 / G-javadoc
- ⚠ **出力先（本プロジェクトのマルチモジュール `ddmsaa-backend/`）**：業務ドメインのコードは `ddmsaa-backend/business-service-core/src/main/java/com/example/ddmsaa/service/core/<feature>/` に配置する。XML→`business-service-core/.../resources/mapper/`、DDL→`データベース` の Flyway `V<n>__*.sql`（スクリプトの正本は 02 に帰属）

<!-- devtool:endblock -->
<!-- devtool:block id="rules.codegen-map-rule.s05" -->
## 4. フロントエンド（spec-frontend-coding-rule.md が主）

| 成果物 | 規約の出所 | データソース |
|------|---------|--------|
| types/`<domain>`.ts | §3(Union 強制・enum 禁止・schema に整合) | 07.yaml |
| api/`<domain>`-api.ts | §4(関数名=operationId・axios ラッパーで JWT 注入/Result 展開/ErrorResponse 変換) | 07.yaml |
| components/`<domain>`/* | §2(ロール接尾辞) + §8(ロール別表示/非表示・表示≠セキュリティ) | 03 権限 |
| pages/`<domain>`/*-page.tsx | §2(ロール接尾辞・ルーティング登録) + §6(状態・単方向データフロー) | — |
| routes.tsx | §9(path ↔ 画面 ID SCR-xx のトレーサビリティ) | 画面遷移（詳細設計） |
| i18n 文言 | §5(全文言を i18n 経由とする・ハードコード禁止・{0} プレースホルダ・antd/dayjs の locale を同期) | 09 3言語 |
| クライアント検証 | §7(バックエンド制約に整合・UX 目的のみ・バックエンドが最終権威) | 07/Bean Validation |

- **生成順序**（§10）：`types → api → 子コンポーネント(Form/Table/Modal/Card) → Page → routes.tsx`
- **生成後の検証ゲート**（§11）：G-型(enum なし) / G-API(=operationId) / G-msg(ハードコードなし) / G-命名 / G-権限 / G-改行 / G-tsdoc
- **出力先**：`frontend/src/{types,api,components,pages}/<domain>/…` + `routes.tsx`

<!-- devtool:endblock -->
<!-- devtool:block id="rules.codegen-map-rule.s06" -->
## 5. spec-wf-codegen ゲート（生成前/生成後）

- **生成前・SSOT 整合プリチェックゲート**：① スタブ状態の DB yaml、② messageId のずれ、③ PK 規約違反を遮断する。
- **生成後・検証ゲート**：バックエンド §13 / フロント §11。いずれも機械的に検査可能。**G-改行**（ファイル規約への適合。`開発規約/spec-backend-coding-rule.md` §12 を参照）を含む。
- **生成後・必須成果物**：`詳細設計/<機能名>/spec-<feature>-filelist.md` を書き込み/更新する（成果物一覧 + ゲート結果 + Flyway 追加バージョン + 参照 messageId + 手作業の残タスク）。レビュー・再生成・クリーンアップに供する。

<!-- devtool:endblock -->
<!-- devtool:block id="rules.codegen-map-rule.s07" -->
## 6. 共有契約（3つのスキャフォールドで一致。一か所を変更したら同期が必要）

| 契約 | 値 |
|------|----|
| API basePath | `/api/v1`（07.yaml servers.url。バックエンドの context-path / フロントの axios / nginx リバースプロキシで一致）|
| バックエンドポート | `8080` |
| フロント dev ポート | `3000`（Vite、proxy `/api`→8080）|
| DB 接続 | `localhost:5434`（ホストから docker の PostgreSQL に接続。`docker/.env` に整合）|

---
**要点**：命名/DB 規約が最上位 → フロント・バックエンドの実装規約 → 詳細設計 → yaml データソース。成果物の種類ごとに出所があり、それぞれ生成順序に従い、検証ゲートを通す。本プロジェクトのバックエンドコードはマルチモジュール `ddmsaa-backend/` に配置する（業務ドメイン→`business-service-core`）。
<!-- devtool:endblock -->
