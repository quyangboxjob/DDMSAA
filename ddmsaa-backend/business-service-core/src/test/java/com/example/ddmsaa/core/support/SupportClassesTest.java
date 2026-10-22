package com.example.ddmsaa.core.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.ddmsaa.common.enums.Role;
import com.example.ddmsaa.common.exception.AuthException;
import com.example.ddmsaa.infrastructure.jwt.JwtUtil;
import com.example.ddmsaa.infrastructure.scan.ScanResult;
import com.example.ddmsaa.infrastructure.scan.VirusScanUnavailableException;
import io.jsonwebtoken.Claims;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.util.ReflectionTestUtils;

/** テスト基盤クラス（JWT ヘルパ・各替身）自体の単体テスト。 */
@DisplayName("テスト基盤クラス")
class SupportClassesTest {

    private static final String SECRET = "test-only-secret-for-ddmsaa-must-be-32-bytes-or-more";

    private JwtUtil realJwtUtil() {
        JwtUtil util = new JwtUtil();
        ReflectionTestUtils.setField(util, "secret", SECRET);
        ReflectionTestUtils.setField(util, "issuer", "ddmsaa");
        ReflectionTestUtils.setField(util, "expirationSeconds", 3600);
        return util;
    }

    @Test
    @DisplayName("TestJwt のトークンは本番の JwtUtil が受理し、subject と role が一致する")
    void testJwtIsAcceptedByRealJwtUtil() {
        TestJwt testJwt = new TestJwt(SECRET, "ddmsaa");

        Claims claims = realJwtUtil().validateAndGetClaims(testJwt.token("E0001", Role.EMPLOYEE));

        assertThat(claims.getSubject()).isEqualTo("E0001");
        assertThat(claims.get("role", String.class)).isEqualTo("EMPLOYEE");
    }

    @Test
    @DisplayName("TestJwt の期限切れは E0001、別鍵は E0041 で本番の JwtUtil に拒否される")
    void testJwtNegativeTokensAreRejected() {
        TestJwt testJwt = new TestJwt(SECRET, "ddmsaa");
        JwtUtil util = realJwtUtil();

        assertThatThrownBy(
                () -> util.validateAndGetClaims(testJwt.expiredToken("A0001", Role.ADMIN)))
                .isInstanceOf(AuthException.class).hasMessageContaining("E0001");
        assertThatThrownBy(() -> util.validateAndGetClaims(
                testJwt.tokenWithWrongSignature("A0001", Role.ADMIN)))
                .isInstanceOf(AuthException.class).hasMessageContaining("E0041");
    }

    @Test
    @DisplayName("TestJwt の role なしトークンは role が null になる")
    void testJwtWithoutRole() {
        Claims claims = realJwtUtil().validateAndGetClaims(new TestJwt(SECRET, "ddmsaa")
                .tokenWithoutRole("C0001"));

        assertThat(claims.get("role", String.class)).isNull();
    }

    @Test
    @DisplayName("TestJwt.bearer は Bearer 接頭辞付きの値を返す")
    void bearerHasPrefix() {
        assertThat(new TestJwt(SECRET, "ddmsaa").bearer("A0001", Role.ADMIN))
                .startsWith("Bearer ey");
    }

    @Test
    @DisplayName("InMemoryFileStorageService は保存・取得・削除・クリアができる")
    void inMemoryStorageRoundTrip() throws Exception {
        InMemoryFileStorageService storage = new InMemoryFileStorageService();
        byte[] data = "内容".getBytes(StandardCharsets.UTF_8);

        storage.upload("a/b.txt", new ByteArrayInputStream(data), data.length, "text/plain");

        assertThat(storage.contains("a/b.txt")).isTrue();
        assertThat(storage.contentType("a/b.txt")).isEqualTo("text/plain");
        assertThat(storage.download("a/b.txt").readAllBytes()).isEqualTo(data);
        assertThat(storage.presignedUrl("a/b.txt", 300)).contains("a/b.txt");
        storage.delete("a/b.txt");
        assertThat(storage.contains("a/b.txt")).isFalse();
        assertThatThrownBy(() -> storage.download("a/b.txt"))
                .isInstanceOf(NoSuchElementException.class);
        storage.upload("c", new ByteArrayInputStream(data), data.length, "x");
        storage.clear();
        assertThat(storage.keys()).isEmpty();
    }

    @Test
    @DisplayName("StubVirusScanService は既定 SAFE で、結果・停止を切り替えられ reset で戻る")
    void stubVirusScanService() {
        StubVirusScanService scan = new StubVirusScanService();
        ByteArrayInputStream content = new ByteArrayInputStream(new byte[] {1});

        assertThat(scan.scan(content, 1)).isEqualTo(ScanResult.SAFE);
        scan.willReturn(ScanResult.REJECTED);
        assertThat(scan.scan(new ByteArrayInputStream(new byte[] {1}), 1))
                .isEqualTo(ScanResult.REJECTED);
        scan.willBeUnavailable();
        assertThatThrownBy(() -> scan.scan(new ByteArrayInputStream(new byte[] {1}), 1))
                .isInstanceOf(VirusScanUnavailableException.class);
        assertThat(scan.scanCount()).isEqualTo(3);
        scan.reset();
        assertThat(scan.scanCount()).isZero();
        assertThat(scan.scan(new ByteArrayInputStream(new byte[] {1}), 1))
                .isEqualTo(ScanResult.SAFE);
    }

    @Test
    @DisplayName("RecordingMailSender は送信せずに記録し、次の 1 回だけ失敗させられる")
    void recordingMailSender() {
        RecordingMailSender sender = new RecordingMailSender();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("user@example.com");
        message.setSubject("件名");

        sender.send(message);
        sender.failNext(new MailSendException("smtp down"));
        assertThatThrownBy(() -> sender.send(message)).isInstanceOf(MailSendException.class);
        sender.send(message);

        assertThat(sender.sentCount()).isEqualTo(2);
        assertThat(sender.simpleMessages().get(0).getSubject()).isEqualTo("件名");
        sender.clear();
        assertThat(sender.sentCount()).isZero();
    }

    @Test
    @DisplayName("リポジトリルートを機構A の SQL の位置から見つけられる")
    void locatesSetupSql() {
        assertThat(PostgresTestContainer.locateRepositoryRoot()
                .resolve("docs/基本設計/データベース/setup/10_schema.sql")).exists();
    }
}
