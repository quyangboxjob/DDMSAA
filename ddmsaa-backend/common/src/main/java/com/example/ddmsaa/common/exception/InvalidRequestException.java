package com.example.ddmsaa.common.exception;

import com.example.ddmsaa.common.response.FieldError;
import java.util.List;

/**
 * 入力不正を表す例外（HTTP 400）。
 *
 * <p>項目単位のエラーを details として保持する。既定の messageId は E0008。
 */
public class InvalidRequestException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 既定の messageId（入力内容に誤りがあります）。 */
    private static final String DEFAULT_MESSAGE_ID = "E0008";

    private final String messageId;

    private final transient List<FieldError> details;

    /**
     * 単一項目の入力不正（E0008）を生成する。
     *
     * @param field 対象項目名
     * @param reason エラー理由
     */
    public InvalidRequestException(String field, String reason) {
        this(DEFAULT_MESSAGE_ID, field, reason);
    }

    /**
     * messageId を指定して単一項目の入力不正を生成する。
     *
     * @param messageId メッセージ ID
     * @param field 対象項目名
     * @param reason エラー理由
     */
    public InvalidRequestException(String messageId, String field, String reason) {
        super(messageId);
        this.messageId = messageId;
        this.details = List.of(new FieldError(field, reason));
    }

    /**
     * 複数項目の入力不正（E0008）を生成する。
     *
     * @param details 項目エラー一覧（null 可。null なら空リスト）
     */
    public InvalidRequestException(List<FieldError> details) {
        super(DEFAULT_MESSAGE_ID);
        this.messageId = DEFAULT_MESSAGE_ID;
        this.details = details == null ? List.of() : List.copyOf(details);
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
     * 項目エラー一覧を返す。
     *
     * @return 項目エラー一覧（不変）
     */
    public List<FieldError> getDetails() {
        return details;
    }
}
