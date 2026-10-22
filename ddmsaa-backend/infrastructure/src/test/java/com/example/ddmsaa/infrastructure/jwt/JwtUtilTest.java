package com.example.ddmsaa.infrastructure.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.ddmsaa.common.exception.AuthException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("JwtUtil の発行と検証")
class JwtUtilTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef-test-secret";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = newJwtUtil(SECRET, "ddmsaa", 3600);
    }

    private static JwtUtil newJwtUtil(String secret, String issuer, int expirationSeconds) {
        JwtUtil util = new JwtUtil();
        ReflectionTestUtils.setField(util, "secret", secret);
        ReflectionTestUtils.setField(util, "issuer", issuer);
        ReflectionTestUtils.setField(util, "expirationSeconds", expirationSeconds);
        return util;
    }

    private static SecretKey key(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("発行したトークンを検証でき sub と role を取得できる")
    void issueAndValidate() {
        String token = jwtUtil.issue("A0001", "ADMIN");

        Claims claims = jwtUtil.validateAndGetClaims(token);

        assertThat(claims.getSubject()).isEqualTo("A0001");
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
        assertThat(claims.getIssuer()).isEqualTo("ddmsaa");
        assertThat(jwtUtil.validate(token)).isTrue();
        assertThat(jwtUtil.getSubject(token)).isEqualTo("A0001");
        assertThat(jwtUtil.getRole(token)).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("有効期限は設定秒数になる")
    void expirationFollowsConfiguration() {
        String token = newJwtUtil(SECRET, "ddmsaa", 120).issue("E0001", "EMPLOYEE");

        Claims claims = jwtUtil.validateAndGetClaims(token);

        long seconds = (claims.getExpiration().getTime() - claims.getIssuedAt().getTime()) / 1000;
        assertThat(seconds).isEqualTo(120);
    }

    @Test
    @DisplayName("userId が null または空なら IllegalArgumentException")
    void issueRejectsBlankUserId() {
        assertThatThrownBy(() -> jwtUtil.issue(null, "ADMIN"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> jwtUtil.issue("", "ADMIN"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("秘密鍵が未設定なら IllegalStateException")
    void issueRequiresSecret() {
        JwtUtil noSecret = newJwtUtil("", "ddmsaa", 3600);

        assertThatThrownBy(() -> noSecret.issue("A0001", "ADMIN"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("秘密鍵が短すぎれば IllegalStateException")
    void issueRejectsWeakSecret() {
        JwtUtil weak = newJwtUtil("short", "ddmsaa", 3600);

        assertThatThrownBy(() -> weak.issue("A0001", "ADMIN"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("token が null または空白なら E0040")
    void blankTokenIsE0040() {
        assertThatThrownBy(() -> jwtUtil.validateAndGetClaims(null))
                .isInstanceOfSatisfying(AuthException.class,
                        e -> assertThat(e.getMessageId()).isEqualTo("E0040"));
        assertThatThrownBy(() -> jwtUtil.validate("  "))
                .isInstanceOfSatisfying(AuthException.class,
                        e -> assertThat(e.getMessageId()).isEqualTo("E0040"));
    }

    @Test
    @DisplayName("形式異常のトークンは E0041")
    void malformedTokenIsE0041() {
        assertThatThrownBy(() -> jwtUtil.validateAndGetClaims("not-a-jwt"))
                .isInstanceOfSatisfying(AuthException.class,
                        e -> assertThat(e.getMessageId()).isEqualTo("E0041"));
    }

    @Test
    @DisplayName("別の鍵で署名されたトークンは E0041")
    void wrongSignatureIsE0041() {
        String token = newJwtUtil("another-secret-another-secret-another-secret", "ddmsaa", 3600)
                .issue("A0001", "ADMIN");

        assertThatThrownBy(() -> jwtUtil.validateAndGetClaims(token))
                .isInstanceOfSatisfying(AuthException.class,
                        e -> assertThat(e.getMessageId()).isEqualTo("E0041"));
    }

    @Test
    @DisplayName("issuer が異なるトークンは E0041")
    void wrongIssuerIsE0041() {
        String token = newJwtUtil(SECRET, "other-issuer", 3600).issue("A0001", "ADMIN");

        assertThatThrownBy(() -> jwtUtil.validateAndGetClaims(token))
                .isInstanceOfSatisfying(AuthException.class,
                        e -> assertThat(e.getMessageId()).isEqualTo("E0041"));
    }

    @Test
    @DisplayName("有効期限切れのトークンは E0001")
    void expiredTokenIsE0001() {
        String token = Jwts.builder()
                .issuer("ddmsaa")
                .subject("A0001")
                .expiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(key(SECRET), Jwts.SIG.HS256)
                .compact();

        assertThatThrownBy(() -> jwtUtil.validateAndGetClaims(token))
                .isInstanceOfSatisfying(AuthException.class,
                        e -> assertThat(e.getMessageId()).isEqualTo("E0001"));
    }

    @Test
    @DisplayName("role が null なら role クレームを持たず getRole は null")
    void nullRoleHasNoClaim() {
        String token = jwtUtil.issue("A0001", null);

        assertThat(jwtUtil.getRole(token)).isNull();
    }
}
