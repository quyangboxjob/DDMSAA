package com.example.ddmsaa.infrastructure.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("JwtAuthenticationFilter の Bearer 検証")
class JwtAuthenticationFilterTest {

    private JwtUtil jwtUtil;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "0123456789abcdef0123456789abcdef-test");
        ReflectionTestUtils.setField(jwtUtil, "issuer", "ddmsaa");
        ReflectionTestUtils.setField(jwtUtil, "expirationSeconds", 3600);
        filter = new JwtAuthenticationFilter(jwtUtil);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        UserContext.clear();
        MDC.clear();
    }

    private static MockHttpServletRequest request(String path, String authorization) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
        request.setRequestURI(path);
        if (authorization != null) {
            request.addHeader("Authorization", authorization);
        }
        return request;
    }

    @Test
    @DisplayName("有効なトークンなら UserContext と SecurityContext を設定してチェーンを継続し、後で消去する")
    void validTokenSetsContextAndClears() throws Exception {
        String token = jwtUtil.issue("E0001", "EMPLOYEE");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> userId = new AtomicReference<>();
        AtomicReference<String> role = new AtomicReference<>();
        AtomicReference<Authentication> auth = new AtomicReference<>();
        AtomicReference<String> mdcUser = new AtomicReference<>();

        filter.doFilter(request("/api/v1/orders", "Bearer " + token), response,
                (req, res) -> {
                    userId.set(UserContext.getUserId());
                    role.set(UserContext.getRole());
                    auth.set(SecurityContextHolder.getContext().getAuthentication());
                    mdcUser.set(MDC.get("userId"));
                });

        assertThat(userId.get()).isEqualTo("E0001");
        assertThat(role.get()).isEqualTo("EMPLOYEE");
        assertThat(mdcUser.get()).isEqualTo("E0001");
        assertThat(auth.get().getName()).isEqualTo("E0001");
        assertThat(auth.get().getAuthorities()).extracting("authority")
                .containsExactly("ROLE_EMPLOYEE");
        assertThat(UserContext.getUserId()).isNull();
        assertThat(MDC.get("userId")).isNull();
    }

    @Test
    @DisplayName("トークンが不正なら 401 を返し、チェーンを継続しない")
    void invalidTokenReturns401() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request("/api/v1/orders", "Bearer invalid"), response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(response.getContentAsString()).contains("\"messageId\":\"E0041\"")
                .contains("\"path\":\"/api/v1/orders\"");
        assertThat(chain.getRequest()).isNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(UserContext.getUserId()).isNull();
    }

    @Test
    @DisplayName("Authorization ヘッダが無ければ認証を設定せずチェーンを継続する")
    void noHeaderContinuesWithoutAuthentication() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request("/api/v1/orders", null), response, chain);

        assertThat(chain.getRequest()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Bearer 以外のスキームなら認証を設定せずチェーンを継続する")
    void nonBearerContinuesWithoutAuthentication() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request("/api/v1/orders", "Basic abc"), response, chain);

        assertThat(chain.getRequest()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("後続処理が例外を投げても UserContext を消去する")
    void clearsUserContextOnException() {
        String token = jwtUtil.issue("A0001", "ADMIN");

        try {
            filter.doFilter(request("/api/v1/orders", "Bearer " + token),
                    new MockHttpServletResponse(), (req, res) -> {
                        throw new IllegalStateException("boom");
                    });
        } catch (Exception expected) {
            assertThat(expected).isInstanceOf(IllegalStateException.class);
        }

        assertThat(UserContext.getUserId()).isNull();
    }

    @Test
    @DisplayName("permitAll パスは検証対象外で、不正トークンでも 401 にしない")
    void permitAllPathsAreSkipped() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request("/api/v1/auth/login-init", "Bearer invalid"), response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(chain.getRequest()).isNotNull();
        assertThat(filter.shouldNotFilter(request("/actuator/health", null))).isTrue();
        assertThat(filter.shouldNotFilter(request("/api/v1/orders", null))).isFalse();
    }
}
