package com.example.ddmsaa.core.support;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * L3 のテスト間でデータを初期化する。対象テーブルは現在の schema から導出する（表数や表名を
 * 固定しない）。参照マスタ（{@code *_codes}・{@code tax_rates}）と Flyway 履歴は残し、
 * 採番シーケンスは開始値へ戻す。
 */
public class DatabaseCleaner {

    private final JdbcTemplate jdbc;

    /**
     * クリーナーを生成する。
     *
     * @param jdbc 接続先のテンプレート
     */
    public DatabaseCleaner(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** 業務テーブルを空にし、採番シーケンスを開始値へ戻す。 */
    public void reset() {
        List<String> tables = jdbc.queryForList("""
                SELECT table_name FROM information_schema.tables
                WHERE table_schema = 'public' AND table_type = 'BASE TABLE'
                  AND table_name NOT LIKE '%\\_codes'
                  AND table_name NOT IN ('tax_rates', 'flyway_schema_history')
                ORDER BY table_name
                """, String.class);
        if (!tables.isEmpty()) {
            jdbc.execute("TRUNCATE TABLE " + String.join(", ", tables.stream()
                    .map(name -> "public." + name).toList()) + " CASCADE");
        }
        List<String> sequences = jdbc.queryForList(
                "SELECT sequencename FROM pg_sequences WHERE schemaname = 'public'", String.class);
        for (String sequence : sequences) {
            jdbc.execute("ALTER SEQUENCE public." + sequence + " RESTART");
        }
    }
}
