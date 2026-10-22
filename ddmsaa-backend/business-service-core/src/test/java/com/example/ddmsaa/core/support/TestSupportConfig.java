package com.example.ddmsaa.core.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * L3 で使う副作用の差し替え。ファイル保存・ウイルススキャン・メール送信をテストから制御できる
 * 替身にし、JWT ヘルパと DB クリーナーも提供する。{@link AbstractIntegrationTest} が取り込む。
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestSupportConfig {

    /**
     * インメモリのファイル保存（本番の Local / S3 より優先する）。
     *
     * @return 替身
     */
    @Bean
    @Primary
    public InMemoryFileStorageService inMemoryFileStorageService() {
        return new InMemoryFileStorageService();
    }

    /**
     * 結果を切り替えられるウイルススキャン（本番の Noop / clamd より優先する）。
     *
     * @return 替身
     */
    @Bean
    @Primary
    public StubVirusScanService stubVirusScanService() {
        return new StubVirusScanService();
    }

    /**
     * 送信せずに記録するメール送信（自動構成の JavaMailSender より優先する）。
     *
     * @return 替身
     */
    @Bean
    @Primary
    public RecordingMailSender recordingMailSender() {
        return new RecordingMailSender();
    }

    /**
     * オフライン JWT 署名ヘルパ。
     *
     * @param environment 設定（{@code app.jwt.*}）
     * @return ヘルパ
     */
    @Bean
    public TestJwt testJwt(Environment environment) {
        return TestJwt.from(environment);
    }

    /**
     * テスト間のデータ初期化。
     *
     * @param jdbcTemplate テンプレート
     * @return クリーナー
     */
    @Bean
    public DatabaseCleaner databaseCleaner(JdbcTemplate jdbcTemplate) {
        return new DatabaseCleaner(jdbcTemplate);
    }
}
