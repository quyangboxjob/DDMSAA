package com.example.ddmsaa.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * {@code POST /api/v1/auth/magic-link/resend} のリクエストボディ。
 *
 * <p>07 では {@code LoginInitRequest} を共用する（項目は同じ）。</p>
 *
 * @param userId ユーザー ID（必須・最大 10 文字）
 */
public record ResendMagicLinkRequest(
        @NotBlank @Size(max = 10) String userId) {
}
