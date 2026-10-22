package com.example.ddmsaa.auth.dto;

/**
 * JWT 発行後にクライアントへ返すアクセストークン応答（リフレッシュトークンなし）。
 *
 * @param accessToken JWT アクセストークン
 * @param tokenType トークン種別（常に {@code Bearer}）
 */
public record TokenResponse(String accessToken, String tokenType) {
}
