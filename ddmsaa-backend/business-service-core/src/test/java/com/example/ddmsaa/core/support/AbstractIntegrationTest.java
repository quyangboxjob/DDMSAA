package com.example.ddmsaa.core.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * L3（プロセス内結合）テストの基底クラス。クラス名は {@code *IT}（failsafe で実行）にする。
 *
 * <p>{@code @SpringBootTest} で全経路（HTTP → Controller → Service → MyBatis → 実 DB）を通す。
 * DB は Testcontainers の PostgreSQL（本番と同じ機構A の SQL で構築）。各テストの前に業務データを
 * 空にして、副作用の替身（保存・スキャン・メール）を初期状態へ戻す。参照マスタの 13 コード表と
 * 税率は残る。管理者や承認ルールなどの業務データは含まないので、テストが自分で用意する。</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSupportConfig.class)
public abstract class AbstractIntegrationTest {

    static {
        // システムタイムゾーンは Asia/Tokyo（spec-11）。開発機の JVM 設定に依らず、DB セッションと
        // 日時の検証を日本時間で揃える（JDBC は接続時に JVM の既定タイムゾーンを送る）。
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Tokyo"));
    }

    /** HTTP 呼び出し（Spring Security のフィルタを含む）。 */
    @Autowired
    protected MockMvc mockMvc;

    /** 検証・fixture 投入用の JDBC。 */
    @Autowired
    protected JdbcTemplate jdbc;

    /** オフライン JWT 署名ヘルパ。 */
    @Autowired
    protected TestJwt jwt;

    /** ファイル保存の替身。 */
    @Autowired
    protected InMemoryFileStorageService storage;

    /** ウイルススキャンの替身。 */
    @Autowired
    protected StubVirusScanService virusScan;

    /** メール送信の替身。 */
    @Autowired
    protected RecordingMailSender mailSender;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    /**
     * データソースをテスト用コンテナへ向ける。
     *
     * @param registry プロパティの登録先
     */
    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        PostgresTestContainer.register(registry);
    }

    /** 各テストの前に、業務データと替身の状態を初期化する。 */
    @BeforeEach
    void resetState() {
        databaseCleaner.reset();
        storage.clear();
        virusScan.reset();
        mailSender.clear();
    }
}
