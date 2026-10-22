package com.example.ddmsaa.auth.service;

import com.example.ddmsaa.auth.dto.TokenResponse;

/**
 * Magic Link 認証フローを定義するサービスインタフェース。
 */
public interface AuthService {

    /**
     * Magic Link を要求する。ログイン可否に関わらず呼び出し側へは区別できる結果を返さない。
     *
     * @param userId ユーザー ID
     */
    void loginInit(String userId);

    /**
     * Magic Link を検証して JWT を発行する。
     *
     * @param token Magic Link トークン
     * @return アクセストークン応答
     * @throws com.example.ddmsaa.common.exception.AuthException
     *             トークンが無効・期限切れ・使用済み、またはログイン不可の場合（E0004）
     */
    TokenResponse verifyMagicLink(String token);

    /**
     * Magic Link を再送する。旧トークンは失効し、処理は {@link #loginInit(String)} と同じ。
     *
     * @param userId ユーザー ID
     */
    void resendMagicLink(String userId);
}
