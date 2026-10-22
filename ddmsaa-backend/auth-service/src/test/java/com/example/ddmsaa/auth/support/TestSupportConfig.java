package com.example.ddmsaa.auth.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * L3 で使う副作用の差し替え。メール送信を記録する替身にし、DB クリーナーを提供する。
 * {@link AbstractIntegrationTest} が取り込む。
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestSupportConfig {

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
