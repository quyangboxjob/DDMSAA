package com.example.ddmsaa.infrastructure.jwt;

import com.example.ddmsaa.common.exception.AuthException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT の発行・検証・クレーム取得を行うユーティリティ（HS256、jjwt）。
 *
 * <p>署名鍵は {@code app.jwt.secret}（環境変数 {@code JWT_SECRET}）の UTF-8 バイト列から得る。
 * 状態を持たないため複数スレッドから同時に呼び出せる。</p>
 */
@Component
public class JwtUtil {

    private static final String CLAIM_ROLE = "role";

    @Value("${app.jwt.secret:}")
    private String secret;

    @Value("${app.jwt.issuer:ddmsaa}")
    private String issuer;

    @Value("${app.jwt.expiration-seconds:3600}")
    private int expirationSeconds;

    /**
     * 設定値は {@code @Value} で注入される。
     */
    public JwtUtil() {
    }

    /**
     * アクセストークンを発行する。
     *
     * @param userId ユーザー ID（subject）
     * @param role ロール（role クレーム。null の場合はクレームを付与しない）
     * @return 署名済み JWT
     * @throws IllegalArgumentException userId が null または空の場合
     * @throws IllegalStateException app.jwt.secret が未設定または鍵として不適切な場合
     */
    public String issue(String userId, String role) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("userId must not be null or empty");
        }
        SecretKey key = signingKey();
        Instant now = Instant.now();
        var builder = Jwts.builder()
                .issuer(issuer)
                .subject(userId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationSeconds)));
        if (role != null) {
            builder.claim(CLAIM_ROLE, role);
        }
        return builder.signWith(key, Jwts.SIG.HS256).compact();
    }

    /**
     * トークンを検証し、1 回の暗号検証で Claims を取得する。
     *
     * @param token JWT 文字列
     * @return 検証済みの Claims（sub / role を取得可能）
     * @throws AuthException token が null/空白の場合は E0040、署名不正・形式異常は E0041、
     *         有効期限切れは E0001
     */
    public Claims validateAndGetClaims(String token) {
        if (token == null || token.isBlank()) {
            throw new AuthException("E0040");
        }
        try {
            return Jwts.parser()
                    .verifyWith(signingKey())
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new AuthException("E0001");
        } catch (JwtException | IllegalArgumentException e) {
            throw new AuthException("E0041");
        }
    }

    /**
     * トークンを検証する。{@link #validateAndGetClaims(String)} の薄いラッパー。
     *
     * @param token JWT 文字列
     * @return 検証に成功した場合 true
     * @throws AuthException 検証に失敗した場合（messageId は validateAndGetClaims と同じ）
     */
    public boolean validate(String token) {
        validateAndGetClaims(token);
        return true;
    }

    /**
     * 検証済みトークンから subject（ユーザー ID）を取得する。
     *
     * @param token JWT 文字列
     * @return ユーザー ID
     * @throws AuthException 検証に失敗した場合
     */
    public String getSubject(String token) {
        return validateAndGetClaims(token).getSubject();
    }

    /**
     * 検証済みトークンから role クレームを取得する。
     *
     * @param token JWT 文字列
     * @return ロール。クレームが無い場合は null
     * @throws AuthException 検証に失敗した場合
     */
    public String getRole(String token) {
        return validateAndGetClaims(token).get(CLAIM_ROLE, String.class);
    }

    private SecretKey signingKey() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("app.jwt.secret is not configured");
        }
        try {
            return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        } catch (WeakKeyException e) {
            throw new IllegalStateException("app.jwt.secret is too short for HS256", e);
        }
    }
}
