package com.example.ddmsaa.common.security;

/**
 * 文字列のマスク処理ユーティリティ（状態を持たない）。
 */
public class DataMaskUtil {

    private static final String MASK = "*";

    /**
     * 末尾 last 文字を残し、それ以前を {@code *} に置換する。
     *
     * @param value 対象文字列（null・空白のみはそのまま返す）
     * @param last 末尾に残す文字数（0 以上）
     * @return マスク後の文字列。last が長さ以上ならそのまま
     * @throws IllegalArgumentException last が負の場合
     */
    public static String maskKeepLast(String value, int last) {
        if (value == null || value.isBlank()) {
            return value;
        }
        if (last < 0) {
            throw new IllegalArgumentException("last must not be negative: " + last);
        }
        int length = value.length();
        if (last >= length) {
            return value;
        }
        return MASK.repeat(length - last) + value.substring(length - last);
    }

    /**
     * 全文字を {@code *} に置換する。
     *
     * @param value 対象文字列（null・空白のみはそのまま返す）
     * @return 同じ長さの {@code *} 文字列
     */
    public static String maskAll(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return MASK.repeat(value.length());
    }
}
