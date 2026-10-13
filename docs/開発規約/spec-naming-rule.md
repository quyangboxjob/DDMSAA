# Naming Spec

> 本文書は React / Spring Boot / PostgreSQL 全スタックアーキテクチャにおける層間命名規約である。
> 本文書はすべての層の規約の命名基盤であり、各層規約の命名部分は本文書を唯一の参照元とする。
> AI が任意の層のコードを生成する際は、本文書と対応する層の開発規約を必ず併せて参照すること。
> すべての規則は強制的な取り決めであり、例外はない。

---

## 目次

- [文書関係表](#文書関係表)
- [1. Case Convention by Layer](#1-case-convention-by-layer)
  - [1.1 共通規則](#11-共通規則)
  - [1.2 各層の Case 規範](#12-各層共通規則)
  - [1.3 Frontend 型定義規則](#13-frontend-型定義規則)
- [2. Resource Naming Across Layers](#2-resource-naming-across-layers)
  - [2.1 変換規則](#21-変換規則)
  - [2.2 Frontend コンポーネント役割接尾辞](#22-frontend-コンポーネント役割接尾辞)
- [3. Fundamental Principles](#3-fundamental-principles)
  - [Principle 1：意味単位の順序一貫性](#principle-1意味単位の順序一貫性)
  - [Principle 2：表現形式の許容差](#principle-2表現形式の許容差)
  - [Principle 3：明示的な変換宣言](#principle-3明示的な変換宣言)
- [4. Abbreviation Whitelist](#4-abbreviation-whitelist)
  - [4.1 全層共通ホワイトリスト](#41-全層共通ホワイトリスト)
  - [4.2 各層専用ホワイトリスト（暫定）](#42-各層専用ホワイトリスト暫定)
- [Appendix：命名チェックリスト](#appendix命名チェックリストai-によるコード生成時の確認事項)

---

## 文書関係表

| 文書名 | ファイル名 | 役割 | 本文書との関係 |
|---|---|---|---|
| 命名規約 | `spec-naming-rule.md`（本文書） | 各層の命名規則と層間変換規則を定義 | 命名に関する唯一の規則元 |
| Frontend開発規約 | `spec-frontend-coding-rule.md` | Frontend 層のアーキテクチャと実装詳細 | 命名部分は本文書を正とする |
| API設計規約 | `spec-07-api-list-ui.md` | API インタフェース設計規範 | 命名部分は本文書を正とする |
| Backend開発規約 | `spec-backend-coding-rule.md` | Backend 層のアーキテクチャと実装詳細 | 命名部分は本文書を正とする |
| DB開発規約 | `spec-database-rule.md` | DB 層のアーキテクチャと実装詳細 | 命名部分は本文書を正とする |
| データ辞書規範 | `spec-data-dictionary-rule.md` | データ型の基礎定義規範 | 命名部分は本文書を正とする |
| データ辞書テンプレート | `spec-data-dictionary-project-temp.md` | データ辞書規範に基づきデータ辞書を作成する際のテンプレート | Principle 3 ホワイトリストの唯一のデータ元 |

---

## 1. Case Convention by Layer

本節は各層のオブジェクトに共通する命名規則を定義するものであり、すべての命名の基礎規則である。

### 1.1 共通規則

<!-- rule: naming.english-only -->
<!-- rule: naming.abbreviation-whitelist -->
- すべての識別子は英語を使用し、中国語・ピンインの使用を禁止する
- ホワイトリストに載っていない略語の使用を禁止する

### 1.2 各層共通規則

<!-- rule: naming.db-identifier-snake-case -->
<!-- rule: naming.backend-class-pascal-case -->
<!-- rule: naming.backend-member-camel-case -->
<!-- rule: naming.api-json-camel-case -->
<!-- rule: naming.api-enum-upper-snake -->
<!-- rule: naming.frontend-file-kebab-case -->
| 層 | 対象 | Case | 例 |
|---|---|---|---|
| **DB** | テーブル名 | snake_case（複数形） | `order_items`, `users` |
| **DB** | 列名 | snake_case | `external_id`, `created_at` |
| **DB** | 制約・インデックス名 | snake_case（プレフィックス付き） | `pk_users`, `idx_orders_status` |
| **Backend** | クラス名・インタフェース名 | PascalCase | `OrderItem`, `OrderService` |
| **Backend** | メソッド名・変数名・フィールド名 | camelCase | `externalId`, `findByExternalId` |
| **Backend** | 定数名 | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| **Backend** | パッケージ名 | 小文字、区切りなし | `com.example.app.order` |
| **API JSON** | リクエスト・レスポンスのフィールド名 | camelCase | `externalId`, `createdAt` |
| **API JSON** | パスパラメータ名 | camelCase | `orderId`, `userId` |
| **API JSON** | クエリパラメータ名 | camelCase | `createdAfter`, `nameLike` |
| **API JSON** | Enum 値 | UPPER_SNAKE_CASE（例外はデータ辞書 spec-13 の T07 枠で登録） | `IN_PROGRESS`, `PAYMENT_FAILED` |
| **Frontend** | コンポーネント名・型名・Interface 名 | PascalCase | `OrderItem`, `OrderCard` |
| **Frontend** | 変数名・関数名・props 名・フィールド名 | camelCase | `externalId`, `createdAt` |
| **Frontend** | ファイル名 | kebab-case | `order-item.tsx`, `order-card.tsx` |
| **Frontend** | 定数名 | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| **Frontend** | Union Type の値 | UPPER_SNAKE_CASE | `"IN_PROGRESS"`, `"PAYMENT_FAILED"` |

### 1.3 Frontend 型定義規則

<!-- rule: naming.frontend-union-type-no-enum -->
Frontend は Union Type を使用し、TypeScript Enum の使用を禁止する。

```typescript
// ✅ 正しい：Union Type
type OrderStatus = "IN_PROGRESS" | "PAYMENT_FAILED" | "COMPLETED"

// ❌ 禁止：TypeScript Enum
enum OrderStatus { IN_PROGRESS = "IN_PROGRESS" }
```

**理由：** Union Type は Backend が返す文字列型との対応が直感的であり、AI によるコード生成時の暗黙的な型変換エラーを回避できる。

---

## 2. Resource Naming Across Layers

本節は、業務エンティティ名の各層における命名変換規則を定義する。
本節は §1 の応用である。業務エンティティ名の意味単位は不変であり、各層の規則と単数/複数形に従って変換するのみである。

### 2.1 変換規則

<!-- rule: naming.db-table-plural -->
<!-- rule: naming.backend-entity-singular -->
<!-- rule: naming.api-path-kebab-plural -->
| 層 | 形式 | 変換規則 | 例（Order Item） |
|---|---|---|---|
| DB | snake_case 複数形 | 複数形 + snake_case | `order_items` |
| Backend Entity | PascalCase 単数形 | 単数形 + PascalCase | `OrderItem` |
| Backend その他のクラス | PascalCase + 接尾辞 | 単数形 + PascalCase + 接尾辞 | `OrderItemService` |
| API パス | kebab-case 複数形 | 複数形 + kebab-case | `/order-items` |
| Frontend コンポーネント | PascalCase 単数形 + 役割接尾辞 | 単数形 + PascalCase + 役割接尾辞 | `OrderItemCard` |
| Frontend ファイル名 | kebab-case 単数形 + 役割接尾辞 | kebab-case（単数の業務名 + 役割接尾辞） | `order-item-card.tsx` |
| Frontend 型・Interface | PascalCase 単数形 | 単数形 + PascalCase | `OrderItem` |

### 2.2 Frontend コンポーネント役割接尾辞

Frontend コンポーネントの役割接尾辞（一覧・用途・使用規則）は `開発規約/spec-frontend-coding-rule.md` §2 を唯一の定義元とする。

---

## 3. Fundamental Principles

本節は、データが層をまたいで受け渡される際の3つの基本原則を定義する。

### Principle 1：意味単位の順序一貫性

**一つのデータが各層間で受け渡される際、その名称を構成する意味単位と順序は一貫していなければならない。**

順序が一致する意味単位は、層をまたぐ同一性の唯一の識別子である。名称が同じでも順序が異なれば、別のデータとみなす。

| 例 | 判定 | 理由 |
|---|---|---|
| `external_id` / `externalId` / `external-id` | ✅ 同一概念 | 意味単位 `external` + `id` の順序が一致しており、適用される規則が異なるのみ |
| `commonUser` vs `userCommon` | ❌ 別のデータ | 意味単位は同じだが、順序が異なる |
| `payment_status` vs `status_payment` | ❌ 別のデータ | 意味単位は同じだが、順序が異なる |

### Principle 2：表現形式の許容差

**各層は自身の技術的制約に応じて適切な命名 case を使用してよく、表現形式の違いは層間のデータの同一性に影響しない。**

意味単位が同じで順序も一致し、case のみが異なる場合は、同一データの正当な層間マッピングとみなし、明示的な宣言は不要である。

### Principle 3：明示的な変換宣言

**名称の意味単位が変化する場合は、【プロジェクトデータ辞書】の Principle 3 ホワイトリストに明示的に登録しなければならず、文脈からの推測に依存してはならない。**

Principle 3 は以下の2種類の場合に発動する。登録規則の詳細は §4 を参照。

**タイプ A：意味境界の絞り込み**

広い語（`id`、`name`、`code`、`type` など）が、ある層の文脈で意味が十分に明確でなく、限定語の追加が必要になる場合、その意味単位が変化するため、【プロジェクトデータ辞書】の Principle 3 ホワイトリストに明示的に登録しなければならない。

規則説明用の例：
```
externalId（Backend）→ id（API JSON / Frontend）
理由：API と Frontend の文脈における id は、Backend の文脈では意味が十分に明確でなく、
限定語 external を追加して意味を明確にする必要がある。
```

**タイプ B：技術的競合による強制置換**

ある層の技術的制約により名称をそのまま使用できない場合は、強制的に置換し、明示的に登録しなければならない。競合語の具体的な定義は各層の開発規約を正とする。

規則説明用の例：
```
order（Backend / Frontend）→ purchases（DB）
理由：order は SQL 予約語であり、DB 層ではテーブル名として直接使用できない。
     SQL 予約語の完全なリストは【DB開発規約】を唯一の参照元とする。
```

---

## 4. Abbreviation Whitelist

本節は略語ホワイトリストを定義する。ホワイトリストに載っていない略語は、いずれの層でも使用を禁止する。
ホワイトリストは正規のホワイトリスト変更手続きによってのみ変更でき、恣意的に変更してはならない。

### 4.1 全層共通ホワイトリスト

| 略語 | 正式名称 | 適用範囲 |
|---|---|---|
| `id` | identifier | 全層（フィールド名、変数名。広い語の絞り込み変換、たとえば `externalId → id` に該当する場合、その変換マッピングは §3 Principle 3 の規定に従い【データ辞書】に登録しなければならない）。注：本プロジェクトの現状では主キーに VARCHAR の業務採番を採用（users=`user_id`）しており、externalId／二重 ID は使用しない（DBQ-08 解決済み）。externalId は汎用的な例示である |
| `url` | URL | 全層（フィールド名、変数名） |
| `ip` | IP address | 全層（フィールド名、変数名） |

### 4.2 各層専用ホワイトリスト（暫定）

> 本節は暫定的な内容である。対応する層の開発規約が正式に公開された時点で、その層専用の略語ホワイトリストを同規約へ移行し、本節の該当内容を削除する。

**Backend 専用：**

| 略語 | 正式名称 | 適用範囲 |
|---|---|---|
| `dto` | Data Transfer Object | クラス名の接尾辞にのみ使用（例：`OrderDto`） |
| `vo` | Value Object | クラス名の接尾辞にのみ使用（例：`MoneyVo`） |

**API 専用：**

| 略語 | 正式名称 | 適用範囲 |
|---|---|---|
| `auth` | authentication | API パスにのみ使用（例：`/auth/login`） |
| `config` | configuration | API パスにのみ使用（例：`/config`） |

---

## Appendix：命名チェックリスト（AI によるコード生成時の確認事項）

AI がコードを生成する際は、以下の順序で項目ごとに確認しなければならない。

1. **§1 の確認**：対象層の正しい case を使用しているか
2. **§2 の確認**：リソース名の単数/複数形と case 変換が規則に合致しているか
3. **§3 の確認（層間マッピング）**：層をまたいで使用する名称について、意味単位の順序が一致しているか。層をまたいで受け渡され、かつ意味単位が変化する項目（広い語の絞り込みや予約語の置換など）は、§3 Principle 3 の規定に厳密に従い、【データ辞書】の Principle 3 変換マッピング表に明示的に登録し、整合させているか
4. **§4 の確認（略語の適合）**：コード中で使用するすべての非正式名称の単語（`id`、`url`、`dto` など）が、§4 の略語ホワイトリストに載っているか。未登録の略語の使用は禁止する
