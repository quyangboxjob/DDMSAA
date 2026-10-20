---
name: spec-sql-lint
description: 建立、执行或维护 DDMSAA 数据库初始化 SQL 和 Flyway 迁移的静态检查。
---

# DDMSAA SQL 静态检查

> 开始前必须阅读 [DDMSAA-PROJECT-CONTEXT.md](../DDMSAA-PROJECT-CONTEXT.md)。

## 范围

- 检查当前 Docker 初始化 SQL 与 `ddmsaa-backend/infrastructure/src/main/resources/db/migration/` 下的 Flyway SQL。
- SQL 规则以现有开发规约和 `.sqlfluff` 配置为准；配置和文字规约出现冲突时报告。

## 规则

- 静态检查不替代 schema 一致性、迁移顺序或运行时验证。
- 已应用的版本迁移不可为了格式而修改；应按迁移规约创建新的版本文件或获得明确批准。
- 缺少 Python/sqlfluff 时报告环境依赖；未经用户确认不安装依赖或更改全局环境。

## 输出

- 报告被检查的文件、命令、结果、豁免和无法验证的范围。
