package com.example.ddmsaa.core.support;

import com.example.ddmsaa.common.enums.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.core.env.Environment;

/**
 * テスト用のオフライン JWT 署名ヘルパ。auth-service を呼ばずに、本番の JwtUtil が受理する
 * トークン（subject = ユーザー ID、claim {@code role}、HS256、issuer）を作る。
 * 鍵・issuer は {@code application-test.yml} の {@code app.jwt.*} と同じ値を使う。
 * ユーザー ID とロールは呼び出し側が指定し、固定の人物やロール番号を持たない。
 */
public final class TestJwt {

    private static final long DEFAULT_TTL_SECONDS = 3600;

    private final String secret;

    private final String issuer;

    /**
     * ヘルパを生成する。
     *
     * @param secret JWT の署名鍵（{@code app.jwt.secret}）
     * @param issuer issuer（{@code app.jwt.issuer}）
     */
    public TestJwt(String secret, String issuer) {
        this.secret = secret;
        this.issuer = issuer;
    }

    /**
     * Spring の設定から鍵と issuer を読んでヘルパを生成する。
     *
     * @param environment 設定
     * @return ヘルパ
     */
    public static TestJwt from(Environment environment) {
        return new TestJwt(environment.getRequiredProperty("app.jwt.secret"),
                environment.getProperty("app.jwt.issuer", "ddmsaa"));
    }

    /**
     * 有効なアクセストークンを発行する。
     *
     * @param userId ユーザー ID（subject）
     * @param role ロール
     * @return JWT
     */
    public String token(String userId, Role role) {
        return build(userId, role.name(), Instant.now().plusSeconds(DEFAULT_TTL_SECONDS), secret);
    }

    /**
     * Authorization ヘッダの値（{@code Bearer <JWT>}）を返す。
     *
     * @param userId ユーザー ID（subject）
     * @param role ロール
     * @return ヘッダ値
     */
    public String bearer(String userId, Role role) {
        return "Bearer " + token(userId, role);
    }

    /**
     * 有効期限が切れたトークンを発行する（401 / E0001 の検証用）。
     *
     * @param userId ユーザー ID（subject）
     * @param role ロール
     * @return 期限切れの JWT
     */
    public String expiredToken(String userId, Role role) {
        return build(userId, role.name(), Instant.now().minusSeconds(60), secret);
    }

    /**
     * 別の鍵で署名したトークンを発行する（401 / E0041 の検証用）。
     *
     * @param userId ユーザー ID（subject）
     * @param role ロール
     * @return 署名が一致しない JWT
     */
    public String tokenWithWrongSignature(String userId, Role role) {
        return build(userId, role.name(), Instant.now().plusSeconds(DEFAULT_TTL_SECONDS),
                "another-test-only-secret-that-is-long-enough-for-hs256");
    }

    /**
     * role クレームのないトークンを発行する。
     *
     * @param userId ユーザー ID（subject）
     * @return role のない JWT
     */
    public String tokenWithoutRole(String userId) {
        return build(userId, null, Instant.now().plusSeconds(DEFAULT_TTL_SECONDS), secret);
    }

    private String build(String userId, String role, Instant expiration, String signingSecret) {
        SecretKey key = Keys.hmacShaKeyFor(signingSecret.getBytes(StandardCharsets.UTF_8));
        var builder = Jwts.builder()
                .issuer(issuer)
                .subject(userId)
                .issuedAt(Date.from(Instant.now().minusSeconds(120)))
                .expiration(Date.from(expiration));
        if (role != null) {
            builder.claim("role", role);
        }
        return builder.signWith(key, Jwts.SIG.HS256).compact();
    }
}
