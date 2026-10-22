package com.example.ddmsaa.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * {@code POST /api/v1/auth/login-init} のリクエストボディ。
 *
 * @param userId ユーザー ID（必須・最大 10 文字）
 */
public record LoginInitRequest(
        @NotBlank @Size(max = 10) String userId) {
}
