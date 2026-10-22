package com.example.ddmsaa.gateway.filter;

import com.example.ddmsaa.gateway.error.ErrorResponseWriter;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * permit-all パス以外で Bearer JWT（HS256）を検証する GlobalFilter。
 *
 * <p>検証に成功した場合は {@code X-User-Id}（JWT の sub）と {@code X-User-Role}（role クレーム）を
 * 下流リクエストへ付与する。失敗時は 401 を統一 JSON で返す（欠落 E0040 / 期限切れ E0001 /
 * その他不正 E0041）。DB は参照しない。
 */
@Component
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {

    /** 認証をスキップするパスプレフィックス。 */
    private static final String AUTH_PATH_PREFIX = "/api/v1/auth/";

    /** 認証をスキップするヘルスチェックパス。 */
    private static final String HEALTH_PATH = "/actuator/health";

    /** Authorization ヘッダの Bearer スキーム接頭辞。 */
    private static final String BEARER_PREFIX = "Bearer ";

    /** 下流へ付与するユーザー ID ヘッダ。 */
    private static final String HEADER_USER_ID = "X-User-Id";

    /** 下流へ付与するロールヘッダ。 */
    private static final String HEADER_USER_ROLE = "X-User-Role";

    /** ロールを格納する JWT クレーム名。 */
    private static final String CLAIM_ROLE = "role";

    /** traceId の Reactor Context / MDC キー。 */
    private static final String TRACE_ID_KEY = "traceId";

    /** userId の Reactor Context / MDC キー。 */
    private static final String USER_ID_KEY = "userId";

    /** JWT 署名検証鍵。 */
    private final SecretKey key;

    /** 要求する issuer。 */
    private final String issuer;

    /** 401 応答の書き出し先。 */
    private final ErrorResponseWriter errorResponseWriter;

    /**
     * 検証鍵と issuer を準備する。
     *
     * @param secret JWT 署名秘密鍵（{@code app.jwt.secret}、UTF-8 で 32 バイト以上）
     * @param issuer 要求する issuer（{@code app.jwt.issuer}）
     * @param errorResponseWriter エラー応答ライター
     * @throws IllegalStateException secret が未設定または HS256 に不足する長さの場合
     */
    public JwtAuthGlobalFilter(@Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.issuer:ddmsaa}") String issuer,
            ErrorResponseWriter errorResponseWriter) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("app.jwt.secret is not configured");
        }
        try {
            this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        } catch (WeakKeyException e) {
            throw new IllegalStateException("app.jwt.secret is too short for HS256", e);
        }
        this.issuer = issuer;
        this.errorResponseWriter = errorResponseWriter;
    }

    /**
     * JWT を検証し、成功時のみ下流へ進める。
     *
     * <p>permit-all パスは検証せず、クライアントが付与した {@code X-User-Id} /
     * {@code X-User-Role} を除去して通過させる（なりすまし防止）。
     *
     * @param exchange 現在の exchange
     * @param chain 後続のフィルタチェーン
     * @return 後続処理または 401 応答の完了を表す {@link Mono}
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if (isPermitAll(request.getPath().value())) {
            ServerHttpRequest stripped = request.mutate().headers(headers -> {
                headers.remove(HEADER_USER_ID);
                headers.remove(HEADER_USER_ROLE);
            }).build();
            return chain.filter(exchange.mutate().request(stripped).build());
        }

        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || authorization.isBlank()) {
            return reject(exchange, "E0040");
        }
        if (!authorization.startsWith(BEARER_PREFIX)) {
            return reject(exchange, "E0041");
        }
        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            return reject(exchange, "E0040");
        }

        Claims claims;
        try {
            claims = Jwts.parser().verifyWith(key).requireIssuer(issuer).build()
                    .parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException e) {
            return reject(exchange, "E0001");
        } catch (JwtException | IllegalArgumentException e) {
            return reject(exchange, "E0041");
        }

        String userId = claims.getSubject();
        String role = claims.get(CLAIM_ROLE, String.class);
        if (userId == null || userId.isBlank() || role == null || role.isBlank()) {
            return reject(exchange, "E0041");
        }

        ServerHttpRequest authenticated = request.mutate().headers(headers -> {
            headers.set(HEADER_USER_ID, userId);
            headers.set(HEADER_USER_ROLE, role);
        }).build();
        MDC.put(USER_ID_KEY, userId);
        return chain.filter(exchange.mutate().request(authenticated).build())
                .contextWrite(ctx -> ctx.put(USER_ID_KEY, userId))
                .doFinally(signal -> MDC.remove(USER_ID_KEY));
    }

    /**
     * フィルタ順序（{@code TraceIdGlobalFilter} の次）を返す。
     *
     * @return {@link Ordered#HIGHEST_PRECEDENCE} + 1
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

    /**
     * 検証をスキップするパスか判定する。
     *
     * @param path リクエストパス
     * @return {@code /api/v1/auth/} 配下または {@code /actuator/health} なら true
     */
    private boolean isPermitAll(String path) {
        return path.startsWith(AUTH_PATH_PREFIX) || path.equals(HEALTH_PATH);
    }

    /**
     * 401 を統一 JSON で返す。traceId は Reactor Context、無ければ MDC から取得する。
     *
     * @param exchange 現在の exchange
     * @param messageId メッセージ ID
     * @return 応答書き込み完了を表す {@link Mono}
     */
    private Mono<Void> reject(ServerWebExchange exchange, String messageId) {
        return Mono.deferContextual(ctx -> errorResponseWriter.write(exchange,
                HttpStatus.UNAUTHORIZED, messageId,
                ctx.getOrDefault(TRACE_ID_KEY, MDC.get(TRACE_ID_KEY))));
    }
}
