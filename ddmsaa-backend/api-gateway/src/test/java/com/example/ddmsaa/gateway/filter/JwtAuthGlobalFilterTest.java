package com.example.ddmsaa.gateway.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.ddmsaa.gateway.error.ErrorResponseWriter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@DisplayName("JwtAuthGlobalFilter: Bearer JWT の検証と下流ヘッダ付与")
class JwtAuthGlobalFilterTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef";
    private static final String ISSUER = "ddmsaa";

    private final JwtAuthGlobalFilter filter =
            new JwtAuthGlobalFilter(SECRET, ISSUER, new ErrorResponseWriter());
    private final ObjectMapper mapper = new ObjectMapper();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    private static SecretKey key(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private static String token(String secret, String issuer, String subject, String role,
            long expiresInMillis) {
        var builder = Jwts.builder()
                .issuer(issuer)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis() - 10_000))
                .expiration(new Date(System.currentTimeMillis() + expiresInMillis));
        if (role != null) {
            builder.claim("role", role);
        }
        return builder.signWith(key(secret)).compact();
    }

    private static String validToken() {
        return token(SECRET, ISSUER, "U0001", "ADMIN", 60_000);
    }

    private static MockServerWebExchange exchange(String path, String authorization) {
        MockServerHttpRequest.BaseBuilder<?> builder = MockServerHttpRequest.get(path);
        if (authorization != null) {
            builder.header("Authorization", authorization);
        }
        return MockServerWebExchange.from(builder.build());
    }

    /** チェーン到達と到達時の exchange を記録する。 */
    private static final class RecordingChain implements GatewayFilterChain {
        private final AtomicReference<ServerWebExchange> reached = new AtomicReference<>();

        @Override
        public Mono<Void> filter(ServerWebExchange exchange) {
            reached.set(exchange);
            return Mono.empty();
        }
    }

    private JsonNode body(MockServerWebExchange exchange) throws Exception {
        return mapper.readTree(exchange.getResponse().getBodyAsString().block());
    }

    private void assertRejected(MockServerWebExchange exchange, RecordingChain chain,
            String messageId) throws Exception {
        assertThat(chain.reached.get()).isNull();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(body(exchange).get("messageId").asText()).isEqualTo(messageId);
    }

    @Test
    @DisplayName("有効なトークンは下流へ通り X-User-Id / X-User-Role が付与される")
    void filter_validToken_addsUserHeaders() {
        MockServerWebExchange ex = exchange("/api/v1/users", "Bearer " + validToken());
        RecordingChain chain = new RecordingChain();

        filter.filter(ex, chain).block();

        assertThat(chain.reached.get()).isNotNull();
        var headers = chain.reached.get().getRequest().getHeaders();
        assertThat(headers.getFirst("X-User-Id")).isEqualTo("U0001");
        assertThat(headers.getFirst("X-User-Role")).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("クライアントが付与した X-User-Id / X-User-Role は JWT の値で上書きされる")
    void filter_validToken_overridesSpoofedHeaders() {
        MockServerWebExchange ex = MockServerWebExchange.from(MockServerHttpRequest
                .get("/api/v1/users")
                .header("Authorization", "Bearer " + validToken())
                .header("X-User-Id", "EVIL")
                .header("X-User-Role", "CUSTOMER").build());
        RecordingChain chain = new RecordingChain();

        filter.filter(ex, chain).block();

        var headers = chain.reached.get().getRequest().getHeaders();
        assertThat(headers.get("X-User-Id")).containsExactly("U0001");
        assertThat(headers.get("X-User-Role")).containsExactly("ADMIN");
    }

    @Test
    @DisplayName("検証成功中は MDC に userId が設定され完了後に除去される")
    void filter_validToken_setsAndClearsUserIdMdc() {
        MockServerWebExchange ex = exchange("/api/v1/users", "Bearer " + validToken());
        AtomicReference<String> mdcInChain = new AtomicReference<>();
        GatewayFilterChain chain = e -> {
            mdcInChain.set(MDC.get("userId"));
            return Mono.empty();
        };

        filter.filter(ex, chain).block();

        assertThat(mdcInChain.get()).isEqualTo("U0001");
        assertThat(MDC.get("userId")).isNull();
    }

    @ParameterizedTest(name = "{0} は検証をスキップして通過する")
    @ValueSource(strings = {"/api/v1/auth/login-init", "/api/v1/auth/magic-link/verify",
            "/actuator/health"})
    @DisplayName("permit-all パスは Authorization 無しでも下流へ通る")
    void filter_permitAllPath_passesWithoutToken(String path) {
        MockServerWebExchange ex = exchange(path, null);
        RecordingChain chain = new RecordingChain();

        filter.filter(ex, chain).block();

        assertThat(chain.reached.get()).isNotNull();
        assertThat(ex.getResponse().getStatusCode()).isNull();
    }

    @Test
    @DisplayName("permit-all パスではクライアント付与の X-User-Id / X-User-Role を除去する")
    void filter_permitAllPath_stripsSpoofedHeaders() {
        MockServerWebExchange ex = MockServerWebExchange.from(MockServerHttpRequest
                .get("/api/v1/auth/login-init")
                .header("X-User-Id", "EVIL")
                .header("X-User-Role", "ADMIN").build());
        RecordingChain chain = new RecordingChain();

        filter.filter(ex, chain).block();

        var headers = chain.reached.get().getRequest().getHeaders();
        assertThat(headers.containsKey("X-User-Id")).isFalse();
        assertThat(headers.containsKey("X-User-Role")).isFalse();
    }

    @Test
    @DisplayName("Authorization ヘッダ欠落は 401 / E0040")
    void filter_missingAuthorization_returnsE0040() throws Exception {
        MockServerWebExchange ex = exchange("/api/v1/users", null);
        RecordingChain chain = new RecordingChain();

        filter.filter(ex, chain).block();

        assertRejected(ex, chain, "E0040");
    }

    @Test
    @DisplayName("Bearer の後ろが空のトークンは 401 / E0040")
    void filter_emptyBearerToken_returnsE0040() throws Exception {
        MockServerWebExchange ex = exchange("/api/v1/users", "Bearer ");
        RecordingChain chain = new RecordingChain();

        filter.filter(ex, chain).block();

        assertRejected(ex, chain, "E0040");
    }

    @Test
    @DisplayName("Bearer 以外のスキームは 401 / E0041")
    void filter_nonBearerScheme_returnsE0041() throws Exception {
        MockServerWebExchange ex = exchange("/api/v1/users", "Basic dXNlcjpwYXNz");
        RecordingChain chain = new RecordingChain();

        filter.filter(ex, chain).block();

        assertRejected(ex, chain, "E0041");
    }

    @Test
    @DisplayName("期限切れトークンは 401 / E0001")
    void filter_expiredToken_returnsE0001() throws Exception {
        String expired = token(SECRET, ISSUER, "U0001", "ADMIN", -5_000);
        MockServerWebExchange ex = exchange("/api/v1/users", "Bearer " + expired);
        RecordingChain chain = new RecordingChain();

        filter.filter(ex, chain).block();

        assertRejected(ex, chain, "E0001");
    }

    @Test
    @DisplayName("署名が不正なトークンは 401 / E0041")
    void filter_badSignature_returnsE0041() throws Exception {
        String forged = token("ffffffffffffffffffffffffffffffff", ISSUER, "U0001", "ADMIN",
                60_000);
        MockServerWebExchange ex = exchange("/api/v1/users", "Bearer " + forged);
        RecordingChain chain = new RecordingChain();

        filter.filter(ex, chain).block();

        assertRejected(ex, chain, "E0041");
    }

    @Test
    @DisplayName("issuer が一致しないトークンは 401 / E0041")
    void filter_wrongIssuer_returnsE0041() throws Exception {
        String other = token(SECRET, "other-issuer", "U0001", "ADMIN", 60_000);
        MockServerWebExchange ex = exchange("/api/v1/users", "Bearer " + other);
        RecordingChain chain = new RecordingChain();

        filter.filter(ex, chain).block();

        assertRejected(ex, chain, "E0041");
    }

    @Test
    @DisplayName("形式が不正なトークンは 401 / E0041")
    void filter_malformedToken_returnsE0041() throws Exception {
        MockServerWebExchange ex = exchange("/api/v1/users", "Bearer not-a-jwt");
        RecordingChain chain = new RecordingChain();

        filter.filter(ex, chain).block();

        assertRejected(ex, chain, "E0041");
    }

    @Test
    @DisplayName("署名なし（alg=none）のトークンは 401 / E0041")
    void filter_unsignedToken_returnsE0041() throws Exception {
        String unsigned = Jwts.builder().issuer(ISSUER).subject("U0001").claim("role", "ADMIN")
                .expiration(new Date(System.currentTimeMillis() + 60_000)).compact();
        MockServerWebExchange ex = exchange("/api/v1/users", "Bearer " + unsigned);
        RecordingChain chain = new RecordingChain();

        filter.filter(ex, chain).block();

        assertRejected(ex, chain, "E0041");
    }

    @Test
    @DisplayName("role クレームが無いトークンは 401 / E0041")
    void filter_missingRoleClaim_returnsE0041() throws Exception {
        String noRole = token(SECRET, ISSUER, "U0001", null, 60_000);
        MockServerWebExchange ex = exchange("/api/v1/users", "Bearer " + noRole);
        RecordingChain chain = new RecordingChain();

        filter.filter(ex, chain).block();

        assertRejected(ex, chain, "E0041");
    }

    @Test
    @DisplayName("401 応答の traceId は Reactor Context の値が入る")
    void filter_rejection_usesTraceIdFromContext() throws Exception {
        MockServerWebExchange ex = exchange("/api/v1/users", null);

        filter.filter(ex, new RecordingChain())
                .contextWrite(ctx -> ctx.put("traceId", "trace-xyz")).block();

        assertThat(body(ex).get("traceId").asText()).isEqualTo("trace-xyz");
        assertThat(body(ex).get("path").asText()).isEqualTo("/api/v1/users");
    }

    @Test
    @DisplayName("フィルタ順序は TraceIdGlobalFilter の次（HIGHEST_PRECEDENCE + 1）")
    void getOrder_isHighestPrecedencePlusOne() {
        assertThat(filter.getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE + 1);
    }

    @Test
    @DisplayName("secret が空の場合は起動時に IllegalStateException")
    void constructor_blankSecret_throws() {
        assertThatThrownBy(() -> new JwtAuthGlobalFilter(" ", ISSUER, new ErrorResponseWriter()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("secret が HS256 に不足する長さの場合は起動時に IllegalStateException")
    void constructor_shortSecret_throws() {
        assertThatThrownBy(
                () -> new JwtAuthGlobalFilter("short", ISSUER, new ErrorResponseWriter()))
                .isInstanceOf(IllegalStateException.class);
    }
}
