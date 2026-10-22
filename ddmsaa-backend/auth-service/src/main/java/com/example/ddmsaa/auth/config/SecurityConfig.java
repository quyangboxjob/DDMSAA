package com.example.ddmsaa.auth.config;

import com.example.ddmsaa.infrastructure.jwt.JwtAuthenticationFilter;
import com.example.ddmsaa.infrastructure.jwt.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * auth-service 用の薄いセキュリティ設定。
 *
 * <p>{@code SecurityFilterChain} は infrastructure の {@code SecurityConfig} に委譲し、
 * 本クラスは {@link JwtAuthenticationFilter} の Bean 登録のみを担う。infrastructure 側の
 * 同名クラスとの Bean 名衝突を避けるため、Bean 名を明示する。</p>
 */
@Configuration("authSecurityConfig")
public class SecurityConfig {

    /**
     * JWT 認証フィルタを Bean として登録する。
     *
     * @param jwtUtil JWT 検証ユーティリティ
     * @return JWT 認証フィルタ
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtUtil jwtUtil) {
        return new JwtAuthenticationFilter(jwtUtil);
    }
}
