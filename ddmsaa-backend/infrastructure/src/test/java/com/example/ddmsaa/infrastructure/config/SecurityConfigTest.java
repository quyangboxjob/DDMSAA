package com.example.ddmsaa.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ddmsaa.infrastructure.jwt.JwtAuthenticationFilter;
import com.example.ddmsaa.infrastructure.jwt.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;

@DisplayName("SecurityConfig の CORS 設定")
class SecurityConfigTest {

    @Test
    @DisplayName("許可オリジンはカンマ区切りで読み込み、全パスに適用する")
    void corsOriginsAreSplit() {
        SecurityConfig config = new SecurityConfig(new JwtAuthenticationFilter(new JwtUtil()));
        ReflectionTestUtils.setField(config, "allowedOrigins",
                "http://localhost:3000, https://app.example.com ,");

        CorsConfiguration cors = config.corsConfigurationSource()
                .getCorsConfiguration(new MockHttpServletRequest("GET", "/api/v1/orders"));

        assertThat(cors).isNotNull();
        assertThat(cors.getAllowedOrigins())
                .containsExactly("http://localhost:3000", "https://app.example.com");
        assertThat(cors.getAllowedMethods()).contains("GET", "POST", "PUT", "DELETE");
        assertThat(cors.getAllowCredentials()).isFalse();
    }
}
