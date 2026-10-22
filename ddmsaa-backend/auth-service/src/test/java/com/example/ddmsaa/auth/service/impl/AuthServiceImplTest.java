package com.example.ddmsaa.auth.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.ddmsaa.auth.dto.TokenResponse;
import com.example.ddmsaa.auth.mapper.AuthAuditMapper;
import com.example.ddmsaa.auth.mapper.UserAuthMapper;
import com.example.ddmsaa.auth.mapper.UserAuthView;
import com.example.ddmsaa.common.exception.AuthException;
import com.example.ddmsaa.infrastructure.auth.AuthStateStore;
import com.example.ddmsaa.infrastructure.jwt.JwtUtil;
import java.time.OffsetDateTime;
import java.util.Base64;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AuthServiceImpl: Magic Link 認証フロー")
class AuthServiceImplTest {

    private static final String USER_ID = "E0001";

    private static final String EMAIL = "e0001@example.com";

    private static final String TOKEN = "dummy-token-value";

    @Mock
    private UserAuthMapper userAuthMapper;

    @Mock
    private AuthAuditMapper authAuditMapper;

    @Mock
    private AuthStateStore authStateStore;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private ObjectProvider<JavaMailSender> mailSenderProvider;

    @Mock
    private JavaMailSender mailSender;

    private AuthServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AuthServiceImpl(userAuthMapper, authAuditMapper, authStateStore, jwtUtil,
                mailSenderProvider);
        ReflectionTestUtils.setField(service, "magicLinkTtlSeconds", 3600);
        ReflectionTestUtils.setField(service, "magicLinkBaseUrl",
                "http://localhost:3000/auth/verify");
        ReflectionTestUtils.setField(service, "mailFrom", "no-reply@ddmsaa.local");
        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);
        MDC.put("traceId", "trace-1");
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    private static UserAuthView view(Integer role, boolean disabled, OffsetDateTime lockStartedAt,
            String email) {
        return new UserAuthView(USER_ID, role, disabled, lockStartedAt, email);
    }

    private static UserAuthView activeView() {
        return view(2, false, null, EMAIL);
    }

    // ---------------------------------------------------------------- loginInit

    @Test
    @DisplayName("loginInit: 有効なユーザーはトークン保存・メール送信・LOGIN_REQUESTED 記録を行う")
    void loginInit_activeUser_savesTokenSendsMailAndAudits() {
        when(userAuthMapper.findAuthView(USER_ID)).thenReturn(activeView());

        service.loginInit(USER_ID);

        ArgumentCaptor<String> token = ArgumentCaptor.forClass(String.class);
        verify(authStateStore).saveMagicLinkToken(token.capture(), eq(USER_ID), eq(3600));
        ArgumentCaptor<SimpleMailMessage> mail = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(mail.capture());
        assertThat(mail.getValue().getTo()).containsExactly(EMAIL);
        assertThat(mail.getValue().getFrom()).isEqualTo("no-reply@ddmsaa.local");
        assertThat(mail.getValue().getText())
                .contains("http://localhost:3000/auth/verify?token=" + token.getValue());
        verify(authAuditMapper).insert(isNull(), eq("LOGIN_REQUESTED"), eq("USER"),
                eq(USER_ID), eq("trace-1"));
    }

    @Test
    @DisplayName("loginInit: トークンは 32 byte の URL セーフ Base64（パディングなし）")
    void loginInit_token_isUrlSafeBase64Of32Bytes() {
        when(userAuthMapper.findAuthView(USER_ID)).thenReturn(activeView());

        service.loginInit(USER_ID);

        ArgumentCaptor<String> token = ArgumentCaptor.forClass(String.class);
        verify(authStateStore).saveMagicLinkToken(token.capture(), eq(USER_ID), anyInt());
        assertThat(token.getValue()).doesNotContain("=", "+", "/");
        assertThat(Base64.getUrlDecoder().decode(token.getValue())).hasSize(32);
    }

    @Test
    @DisplayName("loginInit: 毎回異なるトークンを生成する")
    void loginInit_token_differsEachTime() {
        when(userAuthMapper.findAuthView(USER_ID)).thenReturn(activeView());

        service.loginInit(USER_ID);
        service.loginInit(USER_ID);

        ArgumentCaptor<String> token = ArgumentCaptor.forClass(String.class);
        verify(authStateStore, org.mockito.Mockito.times(2))
                .saveMagicLinkToken(token.capture(), eq(USER_ID), anyInt());
        assertThat(token.getAllValues().get(0)).isNotEqualTo(token.getAllValues().get(1));
    }

    @Test
    @DisplayName("loginInit: 存在しないユーザーはトークン・メールなしで LOGIN_REQUESTED（actor なし）を記録する")
    void loginInit_unknownUser_onlyAudits() {
        when(userAuthMapper.findAuthView("X9999")).thenReturn(null);

        service.loginInit("X9999");

        verifyNoInteractions(authStateStore, mailSender);
        verify(authAuditMapper).insert(isNull(), eq("LOGIN_REQUESTED"), eq("USER"),
                eq("X9999"), eq("trace-1"));
    }

    @Test
    @DisplayName("loginInit: 停用中のユーザーはトークン・メールなし")
    void loginInit_disabledUser_noTokenNoMail() {
        when(userAuthMapper.findAuthView(USER_ID)).thenReturn(view(2, true, null, EMAIL));

        service.loginInit(USER_ID);

        verifyNoInteractions(authStateStore, mailSender);
        verify(authAuditMapper).insert(isNull(), eq("LOGIN_REQUESTED"), eq("USER"),
                eq(USER_ID), anyString());
    }

    @Test
    @DisplayName("loginInit: ロック開始時刻が過去（ロック中）ならトークン・メールなし")
    void loginInit_lockedUser_noTokenNoMail() {
        when(userAuthMapper.findAuthView(USER_ID)).thenReturn(
                view(2, false, OffsetDateTime.now().minusMinutes(1), EMAIL));

        service.loginInit(USER_ID);

        verifyNoInteractions(authStateStore, mailSender);
    }

    @Test
    @DisplayName("loginInit: ロック開始時刻が未来ならまだロックされていない（可）")
    void loginInit_lockStartsInFuture_isAllowed() {
        when(userAuthMapper.findAuthView(USER_ID)).thenReturn(
                view(2, false, OffsetDateTime.now().plusMinutes(10), EMAIL));

        service.loginInit(USER_ID);

        verify(authStateStore).saveMagicLinkToken(anyString(), eq(USER_ID), anyInt());
    }

    @Test
    @DisplayName("loginInit: ロール不正（1/2/3 以外・null）はトークンを発行しない")
    void loginInit_invalidRole_noToken() {
        when(userAuthMapper.findAuthView(USER_ID)).thenReturn(view(9, false, null, EMAIL));
        service.loginInit(USER_ID);
        when(userAuthMapper.findAuthView(USER_ID)).thenReturn(view(null, false, null, EMAIL));
        service.loginInit(USER_ID);

        verifyNoInteractions(authStateStore, mailSender);
        verify(authAuditMapper, org.mockito.Mockito.times(2)).insert(isNull(),
                eq("LOGIN_REQUESTED"), eq("USER"), eq(USER_ID), anyString());
    }

    @Test
    @DisplayName("loginInit: JavaMailSender 未構成でもトークンは保存され例外にならない")
    void loginInit_mailSenderMissing_skipsMail() {
        when(mailSenderProvider.getIfAvailable()).thenReturn(null);
        when(userAuthMapper.findAuthView(USER_ID)).thenReturn(activeView());

        service.loginInit(USER_ID);

        verify(authStateStore).saveMagicLinkToken(anyString(), eq(USER_ID), anyInt());
        verify(authAuditMapper).insert(isNull(), eq("LOGIN_REQUESTED"), eq("USER"), eq(USER_ID),
                anyString());
    }

    @Test
    @DisplayName("loginInit: 連絡先メールが無い場合は送信をスキップする")
    void loginInit_noEmail_skipsMail() {
        when(userAuthMapper.findAuthView(USER_ID)).thenReturn(view(2, false, null, null));

        service.loginInit(USER_ID);

        verify(authStateStore).saveMagicLinkToken(anyString(), eq(USER_ID), anyInt());
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("loginInit: メール送信失敗は握りつぶし、監査は記録される")
    void loginInit_mailFailure_isSwallowed() {
        when(userAuthMapper.findAuthView(USER_ID)).thenReturn(activeView());
        doThrow(new MailSendException("smtp down")).when(mailSender)
                .send(any(SimpleMailMessage.class));

        service.loginInit(USER_ID);

        verify(authAuditMapper).insert(isNull(), eq("LOGIN_REQUESTED"), eq("USER"), eq(USER_ID),
                anyString());
    }

    @Test
    @DisplayName("loginInit: Redis 障害（saveMagicLinkToken の例外）は呼び出し元に伝播する")
    void loginInit_redisFailure_propagates() {
        when(userAuthMapper.findAuthView(USER_ID)).thenReturn(activeView());
        doThrow(new IllegalStateException("redis down")).when(authStateStore)
                .saveMagicLinkToken(anyString(), anyString(), anyInt());

        assertThatThrownBy(() -> service.loginInit(USER_ID))
                .isInstanceOf(IllegalStateException.class);
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("loginInit: 監査 INSERT の失敗は応答に影響しない")
    void loginInit_auditFailure_isSwallowed() {
        when(userAuthMapper.findAuthView(USER_ID)).thenReturn(activeView());
        doThrow(new IllegalStateException("db down")).when(authAuditMapper)
                .insert(any(), anyString(), anyString(), any(), any());

        service.loginInit(USER_ID);

        verify(authStateStore).saveMagicLinkToken(anyString(), eq(USER_ID), anyInt());
    }

    @Test
    @DisplayName("loginInit: 監査の target_id は 10 文字に切り詰める")
    void loginInit_auditTargetId_truncatedTo10() {
        when(userAuthMapper.findAuthView("ABCDEFGHIJKLMN")).thenReturn(null);

        service.loginInit("ABCDEFGHIJKLMN");

        verify(authAuditMapper).insert(isNull(), eq("LOGIN_REQUESTED"), eq("USER"),
                eq("ABCDEFGHIJ"), anyString());
    }

    // ---------------------------------------------------------- verifyMagicLink

    @Test
    @DisplayName("verifyMagicLink: 正常時は JWT を発行し LOGIN_SUCCEEDED（actor=userId）を記録する")
    void verifyMagicLink_valid_issuesJwt() {
        when(authStateStore.consumeMagicLinkToken(TOKEN)).thenReturn(USER_ID);
        when(userAuthMapper.findAuthView(USER_ID)).thenReturn(view(2, false, null, EMAIL));
        when(jwtUtil.issue(USER_ID, "EMPLOYEE")).thenReturn("jwt-value");

        TokenResponse response = service.verifyMagicLink(TOKEN);

        assertThat(response.accessToken()).isEqualTo("jwt-value");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        verify(authAuditMapper).insert(eq(USER_ID), eq("LOGIN_SUCCEEDED"), eq("USER"),
                eq(USER_ID), eq("trace-1"));
    }

    @Test
    @DisplayName("verifyMagicLink: ロール 1/2/3 を ADMIN/EMPLOYEE/CUSTOMER に変換して発行する")
    void verifyMagicLink_roleMapping() {
        when(authStateStore.consumeMagicLinkToken(TOKEN)).thenReturn(USER_ID);
        when(jwtUtil.issue(anyString(), anyString())).thenReturn("jwt");

        when(userAuthMapper.findAuthView(USER_ID)).thenReturn(view(1, false, null, EMAIL));
        service.verifyMagicLink(TOKEN);
        when(userAuthMapper.findAuthView(USER_ID)).thenReturn(view(3, false, null, EMAIL));
        service.verifyMagicLink(TOKEN);

        verify(jwtUtil).issue(USER_ID, "ADMIN");
        verify(jwtUtil).issue(USER_ID, "CUSTOMER");
    }

    @Test
    @DisplayName("verifyMagicLink: トークン無効・期限切れ・使用済みは E0004 と LOGIN_FAILED（対象なし）")
    void verifyMagicLink_invalidToken_throwsE0004() {
        when(authStateStore.consumeMagicLinkToken(TOKEN)).thenReturn(null);

        assertThatThrownBy(() -> service.verifyMagicLink(TOKEN))
                .isInstanceOfSatisfying(AuthException.class,
                        e -> assertThat(e.getMessageId()).isEqualTo("E0004"));

        verify(authAuditMapper).insert(isNull(), eq("LOGIN_FAILED"), eq("USER"), isNull(),
                anyString());
        verifyNoInteractions(userAuthMapper, jwtUtil);
    }

    @Test
    @DisplayName("verifyMagicLink: 消費後に停用されたユーザーは E0004 と LOGIN_FAILED（target=userId）")
    void verifyMagicLink_disabledAfterIssue_throwsE0004() {
        when(authStateStore.consumeMagicLinkToken(TOKEN)).thenReturn(USER_ID);
        when(userAuthMapper.findAuthView(USER_ID)).thenReturn(view(2, true, null, EMAIL));

        assertThatThrownBy(() -> service.verifyMagicLink(TOKEN)).isInstanceOf(AuthException.class);

        verify(authAuditMapper).insert(isNull(), eq("LOGIN_FAILED"), eq("USER"), eq(USER_ID),
                anyString());
        verifyNoInteractions(jwtUtil);
    }

    @Test
    @DisplayName("verifyMagicLink: ロック中のユーザーは E0004")
    void verifyMagicLink_locked_throwsE0004() {
        when(authStateStore.consumeMagicLinkToken(TOKEN)).thenReturn(USER_ID);
        when(userAuthMapper.findAuthView(USER_ID)).thenReturn(
                view(2, false, OffsetDateTime.now().minusSeconds(1), EMAIL));

        assertThatThrownBy(() -> service.verifyMagicLink(TOKEN)).isInstanceOf(AuthException.class);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    @DisplayName("verifyMagicLink: 論理削除済み（ビューなし）のユーザーは E0004")
    void verifyMagicLink_userMissing_throwsE0004() {
        when(authStateStore.consumeMagicLinkToken(TOKEN)).thenReturn(USER_ID);
        when(userAuthMapper.findAuthView(USER_ID)).thenReturn(null);

        assertThatThrownBy(() -> service.verifyMagicLink(TOKEN)).isInstanceOf(AuthException.class);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    @DisplayName("verifyMagicLink: ロールが 1/2/3 以外なら JWT を発行せず E0004")
    void verifyMagicLink_invalidRole_throwsE0004() {
        when(authStateStore.consumeMagicLinkToken(TOKEN)).thenReturn(USER_ID);
        when(userAuthMapper.findAuthView(USER_ID)).thenReturn(view(7, false, null, EMAIL));

        assertThatThrownBy(() -> service.verifyMagicLink(TOKEN))
                .isInstanceOfSatisfying(AuthException.class,
                        e -> assertThat(e.getMessageId()).isEqualTo("E0004"));

        verifyNoInteractions(jwtUtil);
        verify(authAuditMapper).insert(isNull(), eq("LOGIN_FAILED"), eq("USER"), eq(USER_ID),
                anyString());
    }

    @Test
    @DisplayName("verifyMagicLink: 失敗回数ロックの API は呼ばない")
    void verifyMagicLink_neverUsesFailureLockApi() {
        when(authStateStore.consumeMagicLinkToken(TOKEN)).thenReturn(null);

        assertThatThrownBy(() -> service.verifyMagicLink(TOKEN)).isInstanceOf(AuthException.class);

        verify(authStateStore, never()).incrementLoginFailure(anyString());
        verify(authStateStore, never()).isLocked(anyString());
        verify(authStateStore, never()).clearLoginFailure(anyString());
    }

    @Test
    @DisplayName("verifyMagicLink: 監査 INSERT が失敗しても JWT は発行される")
    void verifyMagicLink_auditFailure_stillIssuesJwt() {
        when(authStateStore.consumeMagicLinkToken(TOKEN)).thenReturn(USER_ID);
        when(userAuthMapper.findAuthView(USER_ID)).thenReturn(activeView());
        when(jwtUtil.issue(USER_ID, "EMPLOYEE")).thenReturn("jwt-value");
        doThrow(new IllegalStateException("db down")).when(authAuditMapper)
                .insert(any(), anyString(), anyString(), any(), any());

        assertThat(service.verifyMagicLink(TOKEN).accessToken()).isEqualTo("jwt-value");
    }

    // ------------------------------------------------------------ resendMagicLink

    @Test
    @DisplayName("resendMagicLink: loginInit と同じ処理（新トークン保存・メール・LOGIN_REQUESTED）")
    void resendMagicLink_behavesLikeLoginInit() {
        when(userAuthMapper.findAuthView(USER_ID)).thenReturn(activeView());

        service.resendMagicLink(USER_ID);

        verify(authStateStore).saveMagicLinkToken(anyString(), eq(USER_ID), eq(3600));
        verify(mailSender).send(any(SimpleMailMessage.class));
        verify(authAuditMapper).insert(isNull(), eq("LOGIN_REQUESTED"), eq("USER"), eq(USER_ID),
                anyString());
    }

    @Test
    @DisplayName("resendMagicLink: ログイン不可のユーザーでも例外にならずメールは送らない")
    void resendMagicLink_unknownUser_silent() {
        when(userAuthMapper.findAuthView("X9999")).thenReturn(null);

        service.resendMagicLink("X9999");

        verifyNoInteractions(authStateStore, mailSender);
    }

    @Test
    @DisplayName("トークンはメール以外（監査・ログ引数）に渡さない")
    void token_isNeverPassedToAudit() {
        when(userAuthMapper.findAuthView(USER_ID)).thenReturn(activeView());
        JavaMailSender unused = mock(JavaMailSender.class);

        service.loginInit(USER_ID);

        ArgumentCaptor<String> token = ArgumentCaptor.forClass(String.class);
        verify(authStateStore).saveMagicLinkToken(token.capture(), eq(USER_ID), anyInt());
        ArgumentCaptor<String> args = ArgumentCaptor.forClass(String.class);
        verify(authAuditMapper).insert(any(), args.capture(), args.capture(), args.capture(),
                args.capture());
        assertThat(args.getAllValues()).doesNotContain(token.getValue());
        assertThat(unused).isNotNull();
    }
}
