package com.example.ddmsaa.core.support;

import com.example.ddmsaa.common.message.YamlMessageCatalog;
import com.example.ddmsaa.core.config.SecurityConfig;
import com.example.ddmsaa.infrastructure.jwt.JwtUtil;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

/**
 * L2（Web スライス）で追加する設定。{@code @WebMvcTest} は {@code @Configuration} や
 * {@code @Component} の Bean を拾わないため、本番と同じセキュリティ設定（JWT フィルタ、
 * {@code @PreAuthorize}、401/403 の ErrorResponse）とメッセージカタログを取り込む。
 * Service・Mapper は含まないので、テスト側で {@code @MockitoBean} にする。
 */
@TestConfiguration(proxyBeanMethods = false)
@Import({com.example.ddmsaa.infrastructure.config.SecurityConfig.class, SecurityConfig.class,
    JwtUtil.class, YamlMessageCatalog.class})
public class WebSliceTestConfig {

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
}
