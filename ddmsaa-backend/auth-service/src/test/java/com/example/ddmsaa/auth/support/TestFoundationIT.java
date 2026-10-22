package com.example.ddmsaa.auth.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.example.ddmsaa.infrastructure.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

/**
 * auth-service の L3 基盤（Testcontainers の PostgreSQL・Redis、機構A の SQL、メール替身）の
 * 動作確認。Magic Link のログインを HTTP から JWT 発行まで通す。
 */
@DisplayName("auth-service の L3 テスト基盤（PostgreSQL + Redis + メール替身）")
class TestFoundationIT extends AbstractIntegrationTest {

    private static final Pattern TOKEN = Pattern.compile("token=(\\S+)");

    @Autowired
    private JwtUtil jwtUtil;

    private void insertUser(String userId, int role, int status, String email) {
        jdbc.update("""
                INSERT INTO users (user_id, user_name, role, status, customer_id)
                VALUES (?, 'テスト利用者', ?, ?, NULL)
                """, userId, role, status);
        jdbc.update("INSERT INTO user_contacts (user_id, email) VALUES (?, ?)", userId, email);
    }

    private MvcResult postJson(String path, String json) throws Exception {
        return mockMvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(json))
                .andReturn();
    }

    @Test
    @DisplayName("機構A の schema と参照マスタが入っている")
    void schemaAndReferenceDataAreLoaded() {
        Integer tables = jdbc.queryForObject("""
                SELECT count(*) FROM information_schema.tables
                WHERE table_schema = 'public' AND table_type = 'BASE TABLE'
                  AND table_name <> 'flyway_schema_history'
                """, Integer.class);

        assertThat(tables).isEqualTo(31);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM user_role_codes", Integer.class))
                .isEqualTo(3);
    }

    @Test
    @DisplayName("login-init → メール記録 → verify で JWT が発行され、トークンは 1 回しか使えない")
    void magicLinkLoginFlow() throws Exception {
        insertUser("E0001", 2, 1, "employee@example.com");

        MvcResult init = postJson("/api/v1/auth/login-init", "{\"userId\":\"E0001\"}");
        ApiResponseAssert.assertThat(init).isSuccess().hasMessageId("I0001");
        assertThat(mailSender.sentCount()).isEqualTo(1);
        assertThat(mailSender.simpleMessages().get(0).getTo())
                .containsExactly("employee@example.com");
        Matcher matcher = TOKEN.matcher(mailSender.lastSimpleText());
        assertThat(matcher.find()).isTrue();
        String token = matcher.group(1);

        MvcResult verify = postJson("/api/v1/auth/magic-link/verify",
                "{\"token\":\"" + token + "\"}");
        ApiResponseAssert verified = ApiResponseAssert.assertThat(verify).isSuccess();
        String accessToken = verified.data().path("accessToken").asText();
        Claims claims = jwtUtil.validateAndGetClaims(accessToken);
        assertThat(claims.getSubject()).isEqualTo("E0001");
        assertThat(claims.get("role", String.class)).isEqualTo("EMPLOYEE");

        MvcResult reuse = postJson("/api/v1/auth/magic-link/verify",
                "{\"token\":\"" + token + "\"}");
        ApiResponseAssert.assertThat(reuse).isError(401, "E0004");
    }

    @Test
    @DisplayName("存在しないユーザー ID でも 200 / I0001 を返し、メールは送らない")
    void unknownUserDoesNotLeakExistence() throws Exception {
        MvcResult init = postJson("/api/v1/auth/login-init", "{\"userId\":\"E9999\"}");

        ApiResponseAssert.assertThat(init).isSuccess().hasMessageId("I0001");
        assertThat(mailSender.sentCount()).isZero();
    }

    @Test
    @DisplayName("業務データと Redis は各テストの前に空になる")
    void stateIsResetBetweenTests() {
        assertThat(jdbc.queryForObject("SELECT count(*) FROM users", Integer.class)).isZero();
        assertThat(redis.keys("*")).isEmpty();
    }
}
