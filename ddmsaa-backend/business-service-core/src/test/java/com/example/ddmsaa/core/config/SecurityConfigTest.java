package com.example.ddmsaa.core.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ddmsaa.core.controller.PingController;
import com.example.ddmsaa.infrastructure.jwt.JwtAuthenticationFilter;
import com.example.ddmsaa.infrastructure.jwt.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = PingController.class)
@ContextConfiguration(classes = {PingController.class, SecurityConfig.class, JwtUtil.class})
@Import(com.example.ddmsaa.infrastructure.config.SecurityConfig.class)
@TestPropertySource(properties = {
        "app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "app.jwt.issuer=ddmsaa",
        "app.jwt.expiration-seconds=3600"})
@DisplayName("business-service-core の SecurityConfig")
class SecurityConfigTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("JwtAuthenticationFilter が Bean として登録される")
    void jwtFilterBeanIsRegistered() {
        assertThat(jwtAuthenticationFilter).isNotNull();
    }

    @Test
    @DisplayName("トークンなしの /api/v1/ping は 401（E0004）")
    void pingWithoutTokenIsUnauthorized() throws Exception {
        mvc.perform(get("/api/v1/ping"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.messageId").value("E0004"));
    }

    @Test
    @DisplayName("有効なトークン付きの /api/v1/ping は 200")
    void pingWithValidTokenIsOk() throws Exception {
        String token = jwtUtil.issue("E0001", "EMPLOYEE");

        mvc.perform(get("/api/v1/ping").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("pong"));
    }

    @Test
    @DisplayName("不正なトークンの /api/v1/ping は 401")
    void pingWithInvalidTokenIsUnauthorized() throws Exception {
        mvc.perform(get("/api/v1/ping").header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("/actuator/** と /api/v1/auth/** は認証なしでも 401 にならない")
    void permitAllPathsAreNotUnauthorized() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(result -> assertThat(result.getResponse().getStatus())
                        .isNotEqualTo(401));
        mvc.perform(get("/api/v1/auth/anything"))
                .andExpect(result -> assertThat(result.getResponse().getStatus())
                        .isNotEqualTo(401));
    }
}
