package com.example.ddmsaa.common.exception;

/**
 * 要求サイズ超過を表す例外（HTTP 413）。
 */
public class PayloadTooLargeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String messageId;

    private final String[] args;

    /**
     * サイズ超過の例外を生成する。
     *
     * @param messageId 超過内容を表すメッセージ ID
     * @param args メッセージの置換引数
     */
    public PayloadTooLargeException(String messageId, String... args) {
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
