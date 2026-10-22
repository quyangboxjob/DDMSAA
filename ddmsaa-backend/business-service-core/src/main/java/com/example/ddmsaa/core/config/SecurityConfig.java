package com.example.ddmsaa.core.config;

import com.example.ddmsaa.infrastructure.jwt.JwtAuthenticationFilter;
import com.example.ddmsaa.infrastructure.jwt.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * business-service-core 用の薄いセキュリティ設定。
 *
 * <p>{@link JwtAuthenticationFilter} の {@code @Bean} 登録だけを担う。
 * {@code SecurityFilterChain} は infrastructure の {@code SecurityConfig} に委譲する。
 * infrastructure 側と単純名が同じため、Bean 名の衝突を避けて明示的に命名する。</p>
 */
@Configuration("coreSecurityConfig")
public class SecurityConfig {

    /**
     * JWT 認証フィルタを Bean 登録する。
     *
     * @param jwtUtil トークン検証に使う JwtUtil
     * @return JWT 認証フィルタ
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtUtil jwtUtil) {
        return new JwtAuthenticationFilter(jwtUtil);
    }
}
