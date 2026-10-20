<!-- devtool:file-region id="document-header" -->
# 〇〇〇〇 単体テスト仕様書（バックエンド）

> バックエンド（Spring Boot）の単体テスト仕様書テンプレート。**対象クラス（群）ごとに本ファイルをコピーして記入し**、`単体テスト/バックエンド/spec-<対象 kebab-case>-unittest-sd.md` として保存する。
> テスト階層・ID 規則・ディレクトリ・命名・テストデータの原則は [`開発規約/spec-test-rule.md`](../../開発規約/spec-test-rule.md) を正とする。本ファイルは SSOT を参照するのみで、項目・列挙値・メッセージ・期待値を再定義しない。
> **用例の唯一の源泉は詳細設計 §5.1 UT 用例一覧**（`詳細設計/<機能名>/spec-<feature>-detail-sd.md`）。テスト工程で用例を追加・発明しない。不足は詳細設計へ差し戻す（`spec-test-rule.md` §5）。

<!-- devtool:end-file-region -->
<!-- devtool:block id="unittest.backend-unittest-temp.s01" -->
## 文書情報

| 項目 | 内容 |
|------|------|
| システム名称 | 〇〇システム |
| 文書名称 | 〇〇〇〇 単体テスト仕様書（バックエンド） |
| 文書バージョン | V0.1 |
| ステータス | 草案 |
| 作成者 | TBD |
| 作成日 | YYYY-MM-DD |
| レビュー担当 | TBD |

## 改訂履歴

| バージョン | 改訂日 | 改訂者 | 改訂内容 |
|------|--------|--------|---------|
| V0.1 | YYYY-MM-DD | 〇〇 | 初版 |

---

<!-- devtool:endblock -->
<!-- devtool:block id="unittest.backend-unittest-temp.s02" -->
## 0. 対象と根拠

| 項目 | 内容 |
|------|------|
| 対象機能 | F-〇〇-0N |
| 対象 slug（`<FEATURE>`） | 〇〇（UT-ID の `UT-<FEATURE>-NNN` に用いる機能 slug の英大文字・ハイフン区切り表記） |
| 所属サービス | business-service-core / auth-service / common / infrastructure（いずれか） |
| 対象クラス | §1 を参照 |
| 用例の源泉（必須） | `詳細設計/<機能名>/spec-<feature>-detail-sd.md` §5.1 UT 用例一覧 |
| 上流 SSOT | `spec-07-api-list.yaml`（operationId・入出力）／`spec-09-message-list.yaml`（messageId）／`spec-06-db-physical.yaml`（テーブル・カラム）／`spec-03-auth-ui.md`（ロール・マスキング） |

### 0.1 テスト生成時のロード一覧（AI コンテキスト組み立て）

```
- 本ファイル（当該対象の単体テスト仕様書）
- 詳細設計/<機能名>/spec-<feature>-detail-sd.md   ← §5.1 UT 用例一覧（唯一の用例入力）
- 基本設計/共通/spec-07-api-list.yaml              ← 対象 operationId 部分のみ
- 基本設計/共通/spec-09-message-list.yaml          ← 対象 messageId のみ
- 基本設計/共通/spec-03-auth-ui.md                 ← ロール・マスキングマトリクス
- 開発規約/spec-test-rule.md / spec-naming-rule.md
- 開発規約/@springboot/spec-backend-coding-rule.md
- 基本設計/基盤/@springboot/基盤テンプレート/spec-backend-foundation-temp.md（例外→HTTP・Result）
```

---

<!-- devtool:endblock -->
<!-- devtool:block id="unittest.backend-unittest-temp.s03" -->
## 1. テスト対象クラス

> 被テストクラスを列挙する。機能に紐付かない共通基盤クラス（`common`／`infrastructure`）は UT-ID を付けず、`@DisplayName` に日本語の一言説明を付ける（`spec-test-rule.md` §6）。

| 被テストクラス | パッケージ | 種別 | 推奨層 | テストクラス名 |
|------|------|------|:---:|------|
| `〇〇ServiceImpl` | `com.〇〇.service.〇〇.service.impl` | 業務ロジック | L1 | `〇〇ServiceImplTest` |
| `〇〇Controller` | `com.〇〇.service.〇〇.controller` | Web スライス | L2 | `〇〇ControllerTest` |
| `〇〇Converter`（MapStruct） | `com.〇〇.service.〇〇.convert` | 写像 | L1 | `〇〇ConverterTest` |
| `〇〇`（端到端） | 同上 | プロセス内結合 | L3 | `〇〇IT` |

> ↑ 記入例。正式記入時は対象に合わせて置換・増減する。

---

<!-- devtool:endblock -->
<!-- devtool:block id="unittest.backend-unittest-temp.s04" -->
## 2. テスト層と方針（L1／L2／L3）

> 詳細設計 §5.1 の「推奨層」列に対応する（`spec-test-rule.md` §2.1）。L1／L2 は常に作成する。L3 は共有テスト基盤が整っている場合にのみ作成し、未整備なら L1／L2 のみ作成した旨を §7 に明記する。

| 層 | 方式 | 検証対象 | 外部依存の扱い | 接尾辞 |
|----|------|---------|---------|:---:|
| L1 | JUnit 5 + Mockito（Spring 非起動） | ServiceImpl の純ロジック（採番・状態遷移・列挙変換・マスキング・Converter 写像）、共通基盤クラス | Mapper／外部コンポーネントをモック | `Test` |
| L2 | `@WebMvcTest` + MockMvc + 模擬ユーザー | Controller のルーティング、Bean Validation（400）、`@PreAuthorize`（403）、`Result`／`ErrorResponse` 包絡構造 | Service をモック、テスト用セキュリティ設定 | `Test` |
| L3 | `@SpringBootTest` + Testcontainers（PostgreSQL） | HTTP → Controller → Service → MyBatis → 実 DB の全経路、暗号化ラウンドトリップ、マスク出力 | 共有テスト基盤（基底クラス・固定 dev 鍵・オフライン JWT 署名・副作用スタブ） | `IT` |

---

<!-- devtool:endblock -->
<!-- devtool:block id="unittest.backend-unittest-temp.s05" -->
## 3. テスト項目表

> **各行 ＝ 1 つの UT-ID ＝ 1 つのテストメソッド**（`spec-test-rule.md` §5）。UT-ID は詳細設計 §5.1 の集合をそのまま転記する（ここで追加・発明しない）。期待結果は SSOT の参照のみ（messageId ← `spec-09-message-list.yaml`、HTTP 状態 ← 例外型ごとの固定マッピング、マスク ← `spec-03-auth-ui.md`）。
> `@DisplayName` は **UT-ID を先頭に置く**こと（必須。JUnit XML からの逆引きの前提）。

| UT-ID | 推奨層 | 被テスト（operationId / メソッド） | 種別 | 前提条件 | 入力要点 | モック設定 | 期待結果（messageId / HTTP / 状態遷移 / マスク） | `@DisplayName` / テストメソッド名 | 実装状況 |
|-------|:---:|------|:---:|------|------|------|------|------|:---:|
| UT-〇〇-001 | L1 | `<operationId>` / `〇〇ServiceImpl.〇〇()` | 正常 | 〇〇が存在する | 〇〇 | `〇〇Mapper.select…` が 〇〇 を返す | 成功 `I00NN`／状態 〇→〇 | `UT-〇〇-001 正常_〇〇で成功` / `shouldSucceedWhen〇〇` | 未 |
| UT-〇〇-002 | L2 | `<operationId>` | 検証 | — | 必須項目欠落 | Service はモック | 400 / `E00NN` | `UT-〇〇-002 検証_必須欠落で400` / `shouldReturn400When〇〇Missing` | 未 |
| UT-〇〇-003 | L2 | `<operationId>` | 権限 | 権限不足のロール | 〇〇 | 模擬ユーザー（ロール指定） | 403 / `E0002` | `UT-〇〇-003 権限_〇〇拒否で403` / `shouldReturn403WhenRole〇〇` | 未 |
| UT-〇〇-004 | L1 | `〇〇ServiceImpl.〇〇()` | 異常 | 禁止遷移の前提状態 | 〇〇 | — | `BusinessException` → 409 / `E0006` | `UT-〇〇-004 異常_禁止遷移で409` / `shouldThrowWhenInvalidTransition` | 未 |
| UT-〇〇-005 | L3 | `<operationId>` | 境界 | 実 DB・暗号化カラム有 | my_number 等 | 副作用スタブ | 暗号化ラウンドトリップ成立／出力は `****` でマスク | `UT-〇〇-005 境界_暗号化往復とマスク` / `shouldRoundtripAndMask` | 未 |

> ↑ 記入例。正式記入時は削除し、詳細設計 §5.1 の UT-ID を漏れなく展開する。
> **共通基盤クラス（機能非紐付け）**：UT-ID 列を「—」とし、`@DisplayName` に日本語一言説明（例：「空入力のとき例外を投げる」）を記す。

### 3.1 列定義

| 列 | 内容 |
|------|------|
| UT-ID | `UT-<FEATURE>-NNN`。詳細設計 §5.1 の採番をそのまま使用（機能非紐付けの共通基盤は「—」） |
| 推奨層 | L1／L2／L3。詳細設計 §5.1 の指示に従い、テスト工程での微調整を許容 |
| 種別 | 正常／異常／権限／検証／境界 のいずれか |
| モック設定 | L1／L2 でモックする対象（Mapper・Service・外部コンポーネント）と戻り値の要点 |
| 期待結果 | SSOT の参照のみ（messageId／HTTP／状態遷移／マスク項目）。実装の挙動を正解にしない |
| 実装状況 | 未／作成済／更新／スキップ（理由）。既存の人手テストは盲目的に上書きしない |

---

<!-- devtool:endblock -->
<!-- devtool:block id="unittest.backend-unittest-temp.s06" -->
## 4. テストコードの配置と命名

| 項目 | 規則 |
|------|------|
| 配置 | 被テストクラスと同一モジュールの `src/test/java`、パッケージも本体と同一 |
| クラス名 | `<対象クラス名>Test`（L1／L2）／`<対象クラス名>IT`（L3） |
| 共有テスト基盤 | 各サービスモジュールの `src/test/java/…/support/`、`src/test/resources/application-test.yml` |
| `@DisplayName` | `UT-<FEATURE>-NNN <条件→期待>`（ID を先頭に必須）。メソッド名は「条件_期待」が分かる英語 camelCase |
| アサーション | 各テストメソッドは有効なアサーションを最低 1 つ持つ |

---

<!-- devtool:endblock -->
<!-- devtool:block id="unittest.backend-unittest-temp.s07" -->
## 5. テストデータ・モック方針

> 詳細は `spec-test-rule.md` §8。本節は方針のみを記し、業務データ値は列挙しない。

| 項目 | 方針 |
|------|------|
| schema／種子 | データベース基本設計配下の初期化 SQL を一次供給元とする。テスト専用に複製・再定義しない。不足分のみ冪等なフィクスチャで補う |
| 暗号化カラム | 実行サービスと同一鍵で暗号化した実暗号文を用いる（プレースホルダ文字列へのアサーション禁止） |
| 鍵・秘密情報 | 固定暗号鍵・JWT 署名設定は `src/test`（テスト用プロファイル）にのみ置く。`src/main` にハードコードしない |
| 認証トークン | L3 は本番と同一署名設定でオフライン署名した JWT を用いる。L2 は模擬ユーザー（ロール指定）で `@PreAuthorize` を検証 |
| 副作用の隔離 | メール・サービス間呼び出し・ファイルストレージ・外部ストアは、L1／L2 でモック、L3 でスタブ Bean。実リクエストを送らない |

---

<!-- devtool:endblock -->
<!-- devtool:block id="unittest.backend-unittest-temp.s08" -->
## 6. 実行とレポート

| 項目 | 内容 |
|------|------|
| ランナー束縛 | `*Test` → surefire（`test`）、`*IT` → failsafe（`integration-test`／`verify`）。対象モジュールに failsafe 未設定だと `*IT` が漏れる |
| 全層実行 | `mvn verify` |
| JUnit XML | surefire／failsafe の `statelessTestsetReporter`（`JUnit5Xml30StatelessReporter`）で `usePhrasedTestCaseMethodName=true` を設定し、`<testcase name>` を `@DisplayName` にする |
| レポートのデータ源 | `target/surefire-reports/`／`target/failsafe-reports/` の JUnit XML（コンソール出力ではない） |
| 結果レポート | `単体テスト/バックエンド/report/spec-ut-be-testreport-<date>.md`（直近 1 回分を上書き。失敗・スキップも事実のまま記載） |
| 実行不可時 | 結果を作らず「実行待ち（NOT RUN）」と明記し、起動方法を示す |

---

<!-- devtool:endblock -->
<!-- devtool:block id="unittest.backend-unittest-temp.s09" -->
## 7. カバレッジ確認（満たさない場合は本仕様書未完了）

> 詳細設計 §5.1 のカバレッジ規約（§6.1 D11）と一致させる。

| # | 確認項目 | 判定 |
|---|------|:---:|
| 1 | 各 operationId につき正常系を 1 件以上 | ☐ |
| 2 | §1.2（詳細設計）に列挙した例外分岐（messageId → 例外）ごとに 1 件 | ☐ |
| 3 | `@PreAuthorize` の各ロールの**拒否**ごとに 1 件 | ☐ |
| 4 | 主要な Bean Validation 境界（必須欠落・不正列挙値 → 400）を 1 件以上 | ☐ |
| 5 | 暗号化カラムのラウンドトリップ、マスキングマトリクスの**ロールごと**に各 1 件 | ☐ |
| 6 | 状態遷移の**禁止遷移**ごとに 1 件の異常用例 | ☐ |
| 7 | UT-ID に欠落・重複がなく、詳細設計 §5.1 と一致 | ☐ |

### 7.1 品質ルール（`spec-test-rule.md` §9）

- 空のアサーション・恒真アサーション・コメントアウトされたテスト・理由のない `@Disabled`・ゲート通過だけの空実装は禁止。
- 契約（SSOT）が正本。乖離はテストで取り繕わず、RED のまま上流へ報告するか課題として記録する。実装不具合を本依頼でコード修正しない。
- セキュリティ上の欠陥を現状の正しい挙動として固定しない（あるべき安全な挙動を期待値にし、未対応は備考に明記）。
- 既存の人手テストは盲目的に上書きしない（新規／更新／スキップを区別）。
- 改行・文字コードは `開発規約/@springboot/spec-backend-coding-rule.md` §12 に従う。
<!-- devtool:endblock -->
