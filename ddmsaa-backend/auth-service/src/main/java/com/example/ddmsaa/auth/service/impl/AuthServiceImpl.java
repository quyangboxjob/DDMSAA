package com.example.ddmsaa.auth.service.impl;

import com.example.ddmsaa.auth.dto.TokenResponse;
import com.example.ddmsaa.auth.mapper.AuthAuditMapper;
import com.example.ddmsaa.auth.mapper.UserAuthMapper;
import com.example.ddmsaa.auth.mapper.UserAuthView;
import com.example.ddmsaa.auth.service.AuthService;
import com.example.ddmsaa.common.enums.Role;
import com.example.ddmsaa.common.exception.AuthException;
import com.example.ddmsaa.infrastructure.auth.AuthStateStore;
import com.example.ddmsaa.infrastructure.jwt.JwtUtil;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * {@link AuthService} の実装。Magic Link の生成・メール送信・トークン検証・JWT 発行・
 * ロール判定とログイン監査（audit_logs への追記）を行う。
 *
 * <p>失敗回数によるロックは使用しない（BR-AUTH-005）。トークンは API 応答・ログ・監査に出さない。
 * DB アクセスは READ ONLY の照会と audit_logs の単独 INSERT のみのため、トランザクションは
 * 使用しない。</p>
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private static final String E_AUTH_FAILED = "E0004";

    private static final String ACTION_LOGIN_REQUESTED = "LOGIN_REQUESTED";

    private static final String ACTION_LOGIN_SUCCEEDED = "LOGIN_SUCCEEDED";

    private static final String ACTION_LOGIN_FAILED = "LOGIN_FAILED";

    private static final String TARGET_TYPE_USER = "USER";

    private static final int TARGET_ID_MAX_LENGTH = 10;

    private static final int TOKEN_BYTES = 32;

    private static final String TOKEN_TYPE_BEARER = "Bearer";

    private static final String MAIL_SUBJECT = "ログイン用リンク";

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserAuthMapper userAuthMapper;

    private final AuthAuditMapper authAuditMapper;

    private final AuthStateStore authStateStore;

    private final JwtUtil jwtUtil;

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.auth.magic-link.ttl-seconds:3600}")
    private int magicLinkTtlSeconds = 3600;

    @Value("${app.auth.magic-link.base-url:http://localhost:3000/auth/verify}")
    private String magicLinkBaseUrl = "http://localhost:3000/auth/verify";

    @Value("${app.auth.mail-from:no-reply@ddmsaa.local}")
    private String mailFrom = "no-reply@ddmsaa.local";

    /**
     * サービスを生成する。
     *
     * @param userAuthMapper 認証判定用ビューのマッパー
     * @param authAuditMapper ログイン監査のマッパー（INSERT のみ）
     * @param authStateStore Magic Link トークンの一時ストア
     * @param jwtUtil JWT 発行ユーティリティ
     * @param mailSenderProvider メール送信（未構成の場合は送信をスキップする）
     */
    public AuthServiceImpl(UserAuthMapper userAuthMapper, AuthAuditMapper authAuditMapper,
            AuthStateStore authStateStore, JwtUtil jwtUtil,
            ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.userAuthMapper = userAuthMapper;
        this.authAuditMapper = authAuditMapper;
        this.authStateStore = authStateStore;
        this.jwtUtil = jwtUtil;
        this.mailSenderProvider = mailSenderProvider;
    }

    /**
     * {@inheritDoc}
     *
     * <p>ログイン不可（不存在・停用・ロック・ロール不正）の場合はトークン生成もメール送信も
     * 行わず静かに戻る。どの経路でも監査 {@code LOGIN_REQUESTED} を記録する。</p>
     */
    @Override
    public void loginInit(String userId) {
        UserAuthView view = userAuthMapper.findAuthView(userId);
        if (loginable(view)) {
            if (resolveRole(view) != null) {
                issueMagicLink(view);
            } else {
                log.error("Magic Link not issued: unsupported role code, userId={}", userId);
            }
        }
        audit(ACTION_LOGIN_REQUESTED, null, userId);
    }

    /**
     * {@inheritDoc}
     *
     * @throws AuthException 無効・期限切れ・使用済みトークン、ログイン不可、ロール不正（E0004）
     */
    @Override
    public TokenResponse verifyMagicLink(String token) {
        String userId = authStateStore.consumeMagicLinkToken(token);
        if (userId == null) {
            audit(ACTION_LOGIN_FAILED, null, null);
            throw new AuthException(E_AUTH_FAILED);
        }
        UserAuthView view = userAuthMapper.findAuthView(userId);
        if (!loginable(view)) {
            audit(ACTION_LOGIN_FAILED, null, userId);
            throw new AuthException(E_AUTH_FAILED);
        }
        Role role = resolveRole(view);
        if (role == null) {
            log.error("JWT not issued: unsupported role code, userId={}", userId);
            audit(ACTION_LOGIN_FAILED, null, userId);
            throw new AuthException(E_AUTH_FAILED);
        }
        String accessToken = jwtUtil.issue(userId, role.name());
        audit(ACTION_LOGIN_SUCCEEDED, userId, userId);
        return new TokenResponse(accessToken, TOKEN_TYPE_BEARER);
    }

    /**
     * {@inheritDoc}
     *
     * <p>旧トークンの失効は {@code AuthStateStore.saveMagicLinkToken} が逆引きキーで行う。</p>
     */
    @Override
    public void resendMagicLink(String userId) {
        loginInit(userId);
    }

    /**
     * ログイン可否を判定する（BR-AUTH-001）。
     *
     * @param view ユーザービュー（null 可）
     * @return 存在し、停用でなく、ロック開始時刻が未到来（または無し）なら true
     */
    private boolean loginable(UserAuthView view) {
        if (view == null) {
            return false;
        }
        if (view.disabled()) {
            return false;
        }
        OffsetDateTime lockStartedAt = view.lockStartedAt();
        return lockStartedAt == null || lockStartedAt.isAfter(OffsetDateTime.now());
    }

    /**
     * ロールの INT コードを {@link Role} に変換する。1/2/3 以外は既定値に置き換えない。
     *
     * @param view ユーザービュー
     * @return 対応する {@link Role}。コードが null または未知の場合は null
     */
    private Role resolveRole(UserAuthView view) {
        try {
            return Role.fromCode(view.role());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * トークンを生成して保存し、Magic Link メールを送信する。
     *
     * @param view ログイン可と判定されたユーザービュー
     */
    private void issueMagicLink(UserAuthView view) {
        byte[] bytes = new byte[TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        authStateStore.saveMagicLinkToken(token, view.userId(), magicLinkTtlSeconds);
        sendMail(view, token);
    }

    /**
     * Magic Link メールを送信する（best-effort）。未構成・宛先なし・送信失敗は warn ログのみで、
     * トークンはログに出さない。
     *
     * @param view 送信先のユーザービュー
     * @param token Magic Link トークン
     */
    private void sendMail(UserAuthView view, String token) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("Magic Link mail skipped: JavaMailSender is not configured, userId={}",
                    view.userId());
            return;
        }
        if (view.email() == null || view.email().isBlank()) {
            log.warn("Magic Link mail skipped: no email address, userId={}", view.userId());
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(view.email());
            message.setSubject(MAIL_SUBJECT);
            message.setText(buildMailBody(token));
            mailSender.send(message);
        } catch (RuntimeException e) {
            log.warn("Magic Link mail failed: userId={}, cause={}", view.userId(),
                    e.getClass().getSimpleName());
        }
    }

    /**
     * メール本文を組み立てる。
     *
     * @param token Magic Link トークン
     * @return 本文（ログイン用 URL を含む）
     */
    private String buildMailBody(String token) {
        return "以下のリンクからログインしてください（1 回限り・"
                + (magicLinkTtlSeconds / 60) + " 分間有効）。\n\n"
                + magicLinkBaseUrl + "?token=" + token + "\n\n"
                + "心当たりがない場合は、このメールを破棄してください。\n";
    }

    /**
     * ログイン監査を追記する。追記に失敗しても応答は変えず、error ログのみとする。
     *
     * @param action 監査操作
     * @param actorUserId 操作者。未認証は null
     * @param targetId 対象のユーザー ID。無い場合は null（10 文字に切り詰める）
     */
    private void audit(String action, String actorUserId, String targetId) {
        String target = targetId != null && targetId.length() > TARGET_ID_MAX_LENGTH
                ? targetId.substring(0, TARGET_ID_MAX_LENGTH) : targetId;
        try {
            authAuditMapper.insert(actorUserId, action, TARGET_TYPE_USER, target,
                    MDC.get("traceId"));
        } catch (RuntimeException e) {
            log.error("Login audit insert failed: action={}, targetId={}", action, target, e);
        }
    }
}
