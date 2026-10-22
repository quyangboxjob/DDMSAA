package com.example.ddmsaa.core.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.example.ddmsaa.common.enums.Role;
import java.io.ByteArrayInputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MvcResult;

/** L3 基盤（Testcontainers の PostgreSQL・機構A の SQL・DB クリーナー・替身）の動作確認。 */
@DisplayName("L3 テスト基盤（@SpringBootTest + Testcontainers）")
class TestFoundationIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("機構A の schema（31 テーブル）と参照マスタが入っている")
    void schemaAndReferenceDataAreLoaded() {
        Integer tables = jdbc.queryForObject("""
                SELECT count(*) FROM information_schema.tables
                WHERE table_schema = 'public' AND table_type = 'BASE TABLE'
                  AND table_name <> 'flyway_schema_history'
                """, Integer.class);
        Integer codeTables = jdbc.queryForObject("""
                SELECT count(*) FROM information_schema.tables
                WHERE table_schema = 'public' AND table_name LIKE '%\\_codes'
                """, Integer.class);
        Integer roles = jdbc.queryForObject("SELECT count(*) FROM user_role_codes", Integer.class);
        Integer taxRates = jdbc.queryForObject("SELECT count(*) FROM tax_rates", Integer.class);

        assertThat(tables).isEqualTo(31);
        assertThat(codeTables).isEqualTo(13);
        assertThat(roles).isEqualTo(3);
        assertThat(taxRates).isEqualTo(3);
    }

    @Test
    @DisplayName("DB のタイムゾーンは Asia/Tokyo")
    void databaseTimeZoneIsTokyo() {
        assertThat(jdbc.queryForObject("SHOW timezone", String.class)).isEqualTo("Asia/Tokyo");
    }

    @Test
    @DisplayName("本物の JWT フィルタを通して保護された API を呼べる")
    void protectedApiWorksWithOfflineJwt() throws Exception {
        MvcResult ok = mockMvc.perform(get("/api/v1/ping")
                .header(HttpHeaders.AUTHORIZATION, jwt.bearer("A0001", Role.ADMIN))).andReturn();
        MvcResult anonymous = mockMvc.perform(get("/api/v1/ping")).andReturn();

        ApiResponseAssert.assertThat(ok).isSuccess().dataIs("pong");
        ApiResponseAssert.assertThat(anonymous).isError(401, "E0004");
    }

    @Test
    @DisplayName("業務データは各テストの前に空になり、採番は開始値へ戻る")
    void businessDataIsResetBetweenTests() {
        jdbc.update("""
                INSERT INTO customers (customer_code, customer_name, status)
                VALUES ('RESET-1', 'reset check', 1)
                """);
        assertThat(jdbc.queryForObject("SELECT id FROM customers", String.class))
                .isEqualTo("CS00000001");

        resetStateAgain();

        assertThat(jdbc.queryForObject("SELECT count(*) FROM customers", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT public.next_business_id('CS', 8)", String.class))
                .isEqualTo("CS00000001");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM user_role_codes", Integer.class))
                .isEqualTo(3);
    }

    @Test
    @DisplayName("ファイル保存・ウイルススキャン・メールは替身に差し替わっている")
    void sideEffectsAreStubbed() {
        storage.upload("orders/x/a.txt", new ByteArrayInputStream("hello".getBytes()), 5,
                "text/plain");
        virusScan.willReturn(com.example.ddmsaa.infrastructure.scan.ScanResult.REJECTED);

        assertThat(storage.contains("orders/x/a.txt")).isTrue();
        assertThat(virusScan.scan(new ByteArrayInputStream(new byte[1]), 1))
                .isEqualTo(com.example.ddmsaa.infrastructure.scan.ScanResult.REJECTED);
        assertThat(mailSender.sentCount()).isZero();
        assertThat(storage.keys()).hasSize(1);
    }

    private void resetStateAgain() {
        new DatabaseCleaner(jdbc).reset();
    }
}
