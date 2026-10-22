package com.example.ddmsaa.common.response;

/**
 * 項目単位の入力エラー（{@link ErrorResponse#getDetails()} の要素）。
 *
 * @param field 対象項目名（特定できない場合は null）
 * @param reason エラー理由（利用者に表示してよい文言）
 */
public record FieldError(String field, String reason) {
}
