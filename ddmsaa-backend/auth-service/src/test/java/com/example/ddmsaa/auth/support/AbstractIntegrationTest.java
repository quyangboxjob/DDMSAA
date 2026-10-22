package com.example.ddmsaa.auth.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * auth-service の L3（プロセス内結合）テストの基底クラス。クラス名は {@code *IT}（failsafe）。
 *
 * <p>DB は Testcontainers の PostgreSQL（本番と同じ機構A の SQL で構築）、Magic Link の一時状態は
 * Testcontainers の Redis。メール送信は記録する替身。各テストの前に業務データ・Redis・替身を
 * 初期状態へ戻す。ユーザーなどの業務データは含まないので、テストが自分で用意する。</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSupportConfig.class)
public abstract class AbstractIntegrationTest {

    static {
        // システムタイムゾーンは Asia/Tokyo（spec-11）。開発機の JVM 設定に依らず揃える。
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Tokyo"));
    }

    /** HTTP 呼び出し（Spring Security のフィルタを含む）。 */
    @Autowired
    protected MockMvc mockMvc;

    /** 検証・fixture 投入用の JDBC。 */
    @Autowired
    protected JdbcTemplate jdbc;

    /** Redis（Magic Link の一時状態）。 */
    @Autowired
    protected StringRedisTemplate redis;

    /** メール送信の替身。 */
    @Autowired
    protected RecordingMailSender mailSender;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    /**
     * データソースと Redis の接続先をテスト用コンテナへ向ける。
     *
     * @param registry プロパティの登録先
     */
    @DynamicPropertySource
    static void containerProperties(DynamicPropertyRegistry registry) {
        PostgresTestContainer.register(registry);
        RedisTestContainer.register(registry);
    }

    /** 各テストの前に、業務データ・Redis・替身の状態を初期化する。 */
    @BeforeEach
    void resetState() {
        databaseCleaner.reset();
        var factory = redis.getConnectionFactory();
        if (factory != null) {
            try (var connection = factory.getConnection()) {
                connection.serverCommands().flushAll();
            }
        }
        mailSender.clear();
    }
}
