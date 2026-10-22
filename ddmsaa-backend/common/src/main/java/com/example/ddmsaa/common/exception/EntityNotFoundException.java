package com.example.ddmsaa.common.exception;

/**
 * 対象リソースが存在しないことを表す例外（HTTP 404）。
 *
 * <p>既定の messageId は E0005。
 */
public class EntityNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** デフォルトの messageId（対象が見つかりません）。 */
    private static final String DEFAULT_MESSAGE_ID = "E0005";

    private final String messageId;

    private final String[] args;

    /** 既定の messageId（E0005）で「対象なし」例外を生成する。 */
    public EntityNotFoundException() {
        this(DEFAULT_MESSAGE_ID);
    }

    /**
     * 指定 messageId の「対象なし」例外を生成する。
     *
     * @param messageId メッセージ ID
     * @param args メッセージの置換引数
     */
    public EntityNotFoundException(String messageId, String... args) {
        super(messageId);
        this.messageId = messageId;
        this.args = args;
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
     * @return 置換引数（無ければ null または空）
     */
    public String[] getArgs() {
        return args;
    }
}
