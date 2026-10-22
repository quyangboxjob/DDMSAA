package com.example.ddmsaa.common.response;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * エラーレスポンスの共通ボディ（spec-07 の ErrorResponse）。
 *
 * <p>クライアントは messageId と messageArgs から多言語文言を解決する。message は
 * MessageCatalog の日本語正本によるフォールバック文言（解決できない場合は messageId）。
 * 内部例外のメッセージやスタックトレースは含めない。
 */
public class ErrorResponse {

    /** メッセージ ID（例: E0008）。 */
    private String messageId;

    /** メッセージの置換引数。 */
    private String[] messageArgs;

    /** 日本語正本によるフォールバック文言。 */
    private String message;

    /** 応答生成時刻。 */
    private OffsetDateTime timestamp;

    /** リクエストパス。 */
    private String path;

    /** リクエスト追跡 ID。 */
    private String traceId;

    /** 項目単位のエラー詳細。 */
    private List<FieldError> details;

    /** JSON デシリアライズ用の既定コンストラクタ。 */
    public ErrorResponse() {
    }

    /**
     * メッセージ ID を返す。
     *
     * @return メッセージ ID
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * メッセージの置換引数を返す。
     *
     * @return 置換引数（無ければ null）
     */
    public String[] getMessageArgs() {
        return messageArgs;
    }

    /**
     * フォールバック文言を返す。
     *
     * @return フォールバック文言（未設定なら null）
     */
    public String getMessage() {
        return message;
    }

    /**
     * 応答生成時刻を返す。
     *
     * @return 応答生成時刻
     */
    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * リクエストパスを返す。
     *
     * @return リクエストパス
     */
    public String getPath() {
        return path;
    }

    /**
     * リクエスト追跡 ID を返す。
     *
     * @return 追跡 ID（無ければ null）
     */
    public String getTraceId() {
        return traceId;
    }

    /**
     * 項目単位のエラー詳細を返す。
     *
     * @return エラー詳細（無ければ null）
     */
    public List<FieldError> getDetails() {
        return details;
    }

    /**
     * メッセージ ID を設定する。
     *
     * @param messageId メッセージ ID
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     * メッセージの置換引数を設定する。
     *
     * @param messageArgs 置換引数
     */
    public void setMessageArgs(String[] messageArgs) {
        this.messageArgs = messageArgs;
    }

    /**
     * フォールバック文言を設定する。
     *
     * @param message フォールバック文言
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 応答生成時刻を設定する。
     *
     * @param timestamp 応答生成時刻
     */
    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * リクエストパスを設定する。
     *
     * @param path リクエストパス
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * リクエスト追跡 ID を設定する。
     *
     * @param traceId 追跡 ID
     */
    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    /**
     * 項目単位のエラー詳細を設定する。
     *
     * @param details エラー詳細
     */
    public void setDetails(List<FieldError> details) {
        this.details = details;
    }

    /**
     * 現在時刻の timestamp 付きでエラーレスポンスを生成する。
     *
     * @param messageId メッセージ ID
     * @param path リクエストパス
     * @param traceId リクエスト追跡 ID（null 可）
     * @return 生成したエラーレスポンス（message・details・messageArgs は未設定）
     */
    public static ErrorResponse of(String messageId, String path, String traceId) {
        ErrorResponse response = new ErrorResponse();
        response.messageId = messageId;
        response.path = path;
        response.traceId = traceId;
        response.timestamp = OffsetDateTime.now();
        return response;
    }
}
