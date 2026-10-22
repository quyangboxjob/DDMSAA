package com.example.ddmsaa.auth.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ddmsaa.auth.dto.TokenResponse;
import com.example.ddmsaa.auth.service.AuthService;
import com.example.ddmsaa.common.exception.AuthException;
import com.example.ddmsaa.common.exception.GlobalExceptionHandler;
import com.example.ddmsaa.common.message.MessageCatalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController: 認証 3 API の入出力")
class AuthControllerTest {

    /** messageId をそのまま返すテスト用カタログ。 */
    private static final class EchoMessageCatalog implements MessageCatalog {

        @Override
        public String resolve(String messageId, Object... args) {
            return messageId;
        }

        @Override
        public boolean exists(String messageId) {
            return true;
        }
    }

    @Mock
    private AuthService authService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandler(new EchoMessageCatalog()))
                .build();
    }

    @Test
    @DisplayName("login-init: 200 と messageId=I0001 を返し Service に委譲する")
    void loginInit_ok_returnsI0001() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login-init")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"E0001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0"))
                .andExpect(jsonPath("$.messageId").value("I0001"));

        verify(authService).loginInit("E0001");
    }

    @Test
    @DisplayName("login-init: userId 欠落は 400 E0008")
    void loginInit_missingUserId_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login-init")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messageId").value("E0008"));

        verify(authService, never()).loginInit(anyString());
    }

    @Test
    @DisplayName("login-init: userId が 11 文字以上は 400 E0008")
    void loginInit_tooLongUserId_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login-init")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"E0000000001\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messageId").value("E0008"));
    }

    @Test
    @DisplayName("login-init: 不正な JSON は 400 E0008")
    void loginInit_malformedJson_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login-init")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messageId").value("E0008"));
    }

    @Test
    @DisplayName("verify: 200 とアクセストークン（tokenType=Bearer）を返す")
    void verify_ok_returnsToken() throws Exception {
        when(authService.verifyMagicLink("tok")).thenReturn(new TokenResponse("jwt", "Bearer"));

        mockMvc.perform(post("/api/v1/auth/magic-link/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"tok\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("jwt"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("verify: AuthException(E0004) は 401 E0004")
    void verify_authException_returns401() throws Exception {
        when(authService.verifyMagicLink("bad")).thenThrow(new AuthException("E0004"));

        mockMvc.perform(post("/api/v1/auth/magic-link/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"bad\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.messageId").value("E0004"));
    }

    @Test
    @DisplayName("verify: token 空白は 400 E0008")
    void verify_blankToken_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/magic-link/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messageId").value("E0008"));
    }

    @Test
    @DisplayName("resend: 200 と messageId=I0001 を返し Service に委譲する")
    void resend_ok_returnsI0001() throws Exception {
        mockMvc.perform(post("/api/v1/auth/magic-link/resend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"E0001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageId").value("I0001"));

        verify(authService).resendMagicLink("E0001");
    }

    @Test
    @DisplayName("resend: userId 欠落は 400 E0008")
    void resend_missingUserId_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/magic-link/resend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messageId").value("E0008"));
    }

    @Test
    @DisplayName("login-init: Redis 等の障害（システム例外）は 500 E0012")
    void loginInit_systemFailure_returns500() throws Exception {
        doThrow(new IllegalStateException("redis down")).when(authService).loginInit("E0001");

        mockMvc.perform(post("/api/v1/auth/login-init")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"E0001\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.messageId").value("E0012"));
    }
}
