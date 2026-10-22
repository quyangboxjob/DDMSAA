package com.example.ddmsaa.auth.controller;

import com.example.ddmsaa.auth.dto.LoginInitRequest;
import com.example.ddmsaa.auth.dto.ResendMagicLinkRequest;
import com.example.ddmsaa.auth.dto.TokenResponse;
import com.example.ddmsaa.auth.dto.VerifyMagicLinkRequest;
import com.example.ddmsaa.auth.service.AuthService;
import com.example.ddmsaa.common.response.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Magic Link 認証の REST コントローラ。3 エンドポイントを公開し {@link AuthService} に委譲する。
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final String MESSAGE_MAIL_SENT = "I0001";

    private final AuthService authService;

    /**
     * コントローラを生成する。
     *
     * @param authService 認証サービス
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * ログインを開始する（Magic Link 要求）。可否に関わらず常に I0001 を返す。
     *
     * @param request リクエストボディ
     * @return 成功結果（messageId = I0001）
     */
    @PostMapping("/login-init")
    public Result<Void> loginInit(@Valid @RequestBody LoginInitRequest request) {
        authService.loginInit(request.userId());
        return Result.ok(null, MESSAGE_MAIL_SENT);
    }

    /**
     * Magic Link を検証して JWT を発行する。
     *
     * @param request リクエストボディ
     * @return アクセストークン応答
     */
    @PostMapping("/magic-link/verify")
    public Result<TokenResponse> verifyMagicLink(
            @Valid @RequestBody VerifyMagicLinkRequest request) {
        return Result.ok(authService.verifyMagicLink(request.token()));
    }

    /**
     * Magic Link を再送する。可否に関わらず常に I0001 を返す。
     *
     * @param request リクエストボディ
     * @return 成功結果（messageId = I0001）
     */
    @PostMapping("/magic-link/resend")
    public Result<Void> resendMagicLink(@Valid @RequestBody ResendMagicLinkRequest request) {
        authService.resendMagicLink(request.userId());
        return Result.ok(null, MESSAGE_MAIL_SENT);
    }
}
