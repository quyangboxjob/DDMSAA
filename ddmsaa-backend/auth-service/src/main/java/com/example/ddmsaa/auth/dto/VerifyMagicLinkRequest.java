package com.example.ddmsaa.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * {@code POST /api/v1/auth/magic-link/verify} のリクエストボディ（07 の
 * {@code MagicLinkVerifyRequest}）。
 *
 * @param token メールの Magic Link に含まれるワンタイムトークン（必須・空白不可）
 */
public record VerifyMagicLinkRequest(
        @NotBlank String token) {
}
