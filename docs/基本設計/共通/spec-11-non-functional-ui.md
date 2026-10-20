<!-- devtool:file-region id="document-header" -->
# 11 非機能要件

> システムの「出来の良さ」に関する要求。分類は IPA 非機能要求グレード（六大項目）を参考とする。
> 各項目には**具体的に検証可能な指標**を示し、「できるだけ速く」のような受入れ不能な記述を避ける。

（文書ヘッダは `spec-00-doc-standard-ui.md` を参照）

---

<!-- devtool:end-file-region -->
<!-- devtool:block id="basic.common.11-non-functional-ui.s01" -->
## 1. 可用性・事業継続性

| 項目 | 要求 | 備考 |
|------|------|------|
| 稼働時間 | 平日 8:00–22:00 / 24h | |
| 目標稼働率 | 99.5% | |
| 計画停止 | 月1回・深夜 | |
| バックアップ | 毎日フル + 増分、〇日間保持 | |
| 災害復旧 RPO/RTO | RPO 〇h / RTO 〇h | |

<!-- devtool:endblock -->
<!-- devtool:block id="basic.common.11-non-functional-ui.s02" -->
## 2. 性能・拡張性

| 項目 | 要求 | 備考 |
|------|------|------|
| 画面応答 | 95% のリクエストが 2秒未満 | |
| 同時接続ユーザ数 | 同時オンライン 〇 人 | |
| データ規模 | ユーザ 〇 万 / 申請 〇 万件 | |
| バッチ処理時間枠 | 〇分以内に完了 | `spec-15-batch-design-ui.md` に対応 |
| 拡張方式 | スケールアップ/スケールアウト | |

<!-- devtool:endblock -->
<!-- devtool:block id="basic.common.11-non-functional-ui.s03" -->
## 3. 運用・監視

| 項目 | 要求 | 備考 |
|------|------|------|
| ログ | 操作ログ・エラーログ・アクセスログ、〇か月保持 | |
| 監視 | リソース・死活・エラー率のアラート | 具体化は `spec-18-operation-design-ui.md` |
| マスタ保守 | 管理画面から実施し、コード改修を不要とする | |

<!-- devtool:endblock -->
<!-- devtool:block id="basic.common.11-non-functional-ui.s04" -->
## 4. 移行

| 項目 | 要求 | 備考 |
|------|------|------|
| 現行データ移行 | あり/なし。範囲・方式 | |
| 移行ツール | 〇〇 | 具体化は `spec-17-data-migration-ui.md` |

<!-- devtool:endblock -->
<!-- devtool:block id="basic.common.11-non-functional-ui.s05" -->
## 5. セキュリティ

| 項目 | 要求 | 備考 |
|------|------|------|
| 認証 | Magic Link（メール・パスワードレス）/ 〇〇 | |
| 認可 | ロール + 行単位（本人のみ）制御 | spec-03-auth-ui.md に対応 |
| 機密情報 | 個人番号・口座を暗号化して保存 | |
| 通信 | 全区間 HTTPS | |
| 監査 | 重要操作の証跡を残す（誰が・いつ・何を行ったか） | |
| コンプライアンス | 個人情報保護法 など | |

<!-- devtool:endblock -->
<!-- devtool:block id="basic.common.11-non-functional-ui.s06" -->
## 6. システム環境・エコシステム

| 項目 | 要求 | 備考 |
|------|------|------|
| フロントエンド | React 〇 / 対応ブラウザの範囲 | |
| バックエンド | Spring Boot 〇 / JDK 〇 | |
| DB | PostgreSQL（バージョンは spec-01 §2.3 に従う） | |
| 配置 | 〇〇（コンテナ/VM/クラウド） | |
| 文字コード・タイムゾーン | UTF-8 / システムタイムゾーン（`spec-01-system-architecture-ui.md`） | |
| 多言語対応 | あり/なし | |

### 記入要領

- 各項目はできるだけ**定量化**（数値・パーセンテージ・時間）し、テスト段階での受入れを容易にする。
- セキュリティ項目は `spec-03-auth-ui.md` および機密項目（`spec-06-db-physical-ui.md`）と相互に突き合わせ、整合を確認する。
- 未確定のものは「TBD」と記入して確認事項に計上し、空欄にしない。
<!-- devtool:endblock -->
