package com.example.ddmsaa.auth.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ddmsaa.infrastructure.jwt.JwtAuthenticationFilter;
import com.example.ddmsaa.infrastructure.jwt.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;

@DisplayName("auth-service SecurityConfig: JwtAuthenticationFilter の Bean 登録")
class SecurityConfigTest {

    @Test
    @DisplayName("JwtAuthenticationFilter を生成する")
    void jwtAuthenticationFilter_returnsFilter() {
        JwtAuthenticationFilter filter = new SecurityConfig()
                .jwtAuthenticationFilter(new JwtUtil());

        assertThat(filter).isNotNull();
    }

    @Test
    @DisplayName("infrastructure の SecurityConfig と Bean 名が衝突しない")
    void beanName_differsFromInfrastructureSecurityConfig() {
        String ours = SecurityConfig.class.getAnnotation(Configuration.class).value();

        assertThat(ours).isNotEmpty().isNotEqualTo("securityConfig");
    }
}
