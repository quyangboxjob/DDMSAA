package com.example.ddmsaa.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

/**
 * WebFlux 用の Spring Security 設定。
 *
 * <p>認証・認可は {@code JwtAuthGlobalFilter} に完全委譲するため、本クラスは CSRF 無効化・
 * セッションレス・全パス許可のフィルタチェーンのみを定義する。
 * ブロッキング依存（パスワードエンコーダ、UserDetailsService、JDBC 等）は含めない。
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    /**
     * リアクティブ {@link SecurityWebFilterChain} を構成する。
     *
     * @param http リアクティブ HTTP セキュリティビルダー
     * @return CSRF 無効・セッションレス・全パス許可のフィルタチェーン
     */
    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
                .build();
    }
}
