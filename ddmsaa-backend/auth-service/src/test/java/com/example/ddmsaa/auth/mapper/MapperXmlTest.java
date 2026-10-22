package com.example.ddmsaa.auth.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.Locale;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Mapper XML の構文・SQL 契約の静的検証（実 DB は使わない。実行時の SQL 検証は結合テストで行う）。
 */
@DisplayName("Mapper XML: UserAuthMapper / AuthAuditMapper の静的検証")
class MapperXmlTest {

    private Configuration configuration;

    @BeforeEach
    void setUp() throws Exception {
        configuration = new Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        parse("mapper/UserAuthMapper.xml", "UserAuthMapper");
        parse("mapper/AuthAuditMapper.xml", "AuthAuditMapper");
    }

    private void parse(String path, String name) throws Exception {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(path)) {
            assertThat(in).as(path).isNotNull();
            new XMLMapperBuilder(in, configuration, path, configuration.getSqlFragments())
                    .parse();
        }
    }

    private String sql(String statementId) {
        MappedStatement statement = configuration.getMappedStatement(statementId);
        BoundSql boundSql = statement.getBoundSql(java.util.Map.of("userId", "E0001"));
        return boundSql.getSql().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
    }

    @Test
    @DisplayName("findAuthView: users / user_contacts / system_locks を "
            + "LEFT JOIN し users の論理削除を除外する")
    void findAuthView_sqlContract() {
        String sql = sql("com.example.ddmsaa.auth.mapper.UserAuthMapper.findAuthView");

        assertThat(sql).startsWith("SELECT");
        assertThat(sql).contains("FROM USERS E");
        assertThat(sql).contains("LEFT JOIN USER_CONTACTS EC ON E.USER_ID = EC.USER_ID");
        assertThat(sql).contains("LEFT JOIN SYSTEM_LOCKS SL ON E.USER_ID = SL.USER_ID");
        assertThat(sql).contains("E.USER_ID = ?");
        assertThat(sql).contains("E.DELETED_AT IS NULL");
        assertThat(sql).contains("SL.LOCK_STARTED_AT");
        assertThat(sql).doesNotContain("INSERT", "UPDATE ", "DELETE FROM");
    }

    @Test
    @DisplayName("findAuthView: SELECT であり resultMap が UserAuthView の 5 引数コンストラクタに対応する")
    void findAuthView_resultMap() {
        MappedStatement statement = configuration
                .getMappedStatement("com.example.ddmsaa.auth.mapper.UserAuthMapper.findAuthView");

        assertThat(statement.getSqlCommandType()).isEqualTo(SqlCommandType.SELECT);
        assertThat(statement.getResultMaps()).hasSize(1);
        assertThat(statement.getResultMaps().get(0).getType()).isEqualTo(UserAuthView.class);
        assertThat(statement.getResultMaps().get(0).getConstructorResultMappings()).hasSize(5);
    }

    @Test
    @DisplayName("AuthAuditMapper.insert: audit_logs への INSERT のみ（トークン列なし）")
    void insert_sqlContract() {
        String statementId = "com.example.ddmsaa.auth.mapper.AuthAuditMapper.insert";
        MappedStatement statement = configuration.getMappedStatement(statementId);
        BoundSql boundSql = statement.getBoundSql(java.util.Map.of());
        String sql = boundSql.getSql().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);

        assertThat(statement.getSqlCommandType()).isEqualTo(SqlCommandType.INSERT);
        assertThat(sql).startsWith("INSERT INTO AUDIT_LOGS");
        assertThat(sql).contains("ACTOR_USER_ID", "AUDIT_ACTION", "TARGET_TYPE", "TARGET_ID",
                "TRACE_ID");
        assertThat(sql).doesNotContain("TOKEN", "AUDIT_DETAIL");
        assertThat(boundSql.getParameterMappings()).hasSize(5);
    }

    @Test
    @DisplayName("AuthAuditMapper は INSERT 以外のステートメントを持たない")
    void auditMapper_hasOnlyInsert() {
        long count = configuration.getMappedStatementNames().stream()
                .filter(n -> n.startsWith("com.example.ddmsaa.auth.mapper.AuthAuditMapper."))
                .filter(n -> n.lastIndexOf('.') == "com.example.ddmsaa.auth.mapper.AuthAuditMapper"
                        .length())
                .count();
        assertThat(count).isEqualTo(1);
    }
}
