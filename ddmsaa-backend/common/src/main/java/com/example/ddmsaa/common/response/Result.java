package com.example.ddmsaa.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;
import org.slf4j.MDC;

/**
 * 成功レスポンスの共通エンベロープ（spec-07 の Result）。
 *
 * <p>null のフィールドは JSON から省略される。エラー時は {@link ErrorResponse} を返す。
 *
 * @param code 結果コード（文字列。成功は {@code "0"}）
 * @param message サーバ側フォールバックメッセージ
 * @param messageId メッセージ ID（クライアントが多言語文言を引く鍵。無ければ null）
 * @param messageArgs メッセージの置換引数（無ければ null）
 * @param traceId リクエスト追跡 ID（MDC から取得。無ければ null）
 * @param timestamp 応答生成時刻
 * @param data 応答データ（無ければ null）
 * @param <T> 応答データの型
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Result<T>(String code, String message, String messageId, String[] messageArgs,
        String traceId, OffsetDateTime timestamp, T data) {

    /** 成功時の結果コード。 */
    private static final String CODE_OK = "0";

    /** 成功時の既定メッセージ（サーバ側フォールバック）。 */
    private static final String MESSAGE_OK = "success";

    /**
     * 成功レスポンスを生成する。
     *
     * @param data 応答データ（null 可）
     * @param <T> 応答データの型
     * @return code が {@code "0"} の成功レスポンス
     */
    public static <T> Result<T> ok(T data) {
        return new Result<>(CODE_OK, MESSAGE_OK, null, null, MDC.get("traceId"),
                OffsetDateTime.now(), data);
    }

    /**
     * メッセージ ID 付きの成功レスポンスを生成する。
     *
     * @param data 応答データ（null 可）
     * @param messageId メッセージ ID
     * @param args メッセージの置換引数（null または空なら messageArgs は null）
     * @param <T> 応答データの型
     * @return code が {@code "0"} の成功レスポンス
     */
    public static <T> Result<T> ok(T data, String messageId, String... args) {
        String[] messageArgs = args == null || args.length == 0 ? null : args;
        return new Result<>(CODE_OK, MESSAGE_OK, messageId, messageArgs, MDC.get("traceId"),
                OffsetDateTime.now(), data);
    }
}
