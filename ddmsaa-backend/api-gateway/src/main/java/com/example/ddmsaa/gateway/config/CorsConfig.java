package com.example.ddmsaa.gateway.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * CORS 設定。{@link CorsWebFilter} を Bean として定義する。
 *
 * <p>{@code CorsWebFilter} は Gateway の GlobalFilter より前に評価されるため、OPTIONS プリフライトは
 * {@code JwtAuthGlobalFilter} を通過せずに応答される。
 */
@Configuration
public class CorsConfig {

    /** プリフライト結果のキャッシュ秒数。 */
    private static final long MAX_AGE_SECONDS = 3600L;

    /** 許可オリジンパターン（カンマ区切りを分割・トリム済み）。 */
    private final List<String> allowedOrigins;

    /**
     * 許可オリジンを受け取る。
     *
     * @param allowedOrigins 許可オリジンのカンマ区切り文字列（{@code app.cors.allowed-origins}）
     */
    public CorsConfig(@Value("${app.cors.allowed-origins}") String allowedOrigins) {
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
    }

    /**
     * 全パスに適用する CORS フィルタを生成する。
     *
     * @return 許可オリジン・メソッド・ヘッダ・露出ヘッダ・資格情報・キャッシュ期間を設定した
     *         {@link CorsWebFilter}
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(allowedOrigins);
        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("X-Trace-Id"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(MAX_AGE_SECONDS);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return new CorsWebFilter(source);
    }
}
