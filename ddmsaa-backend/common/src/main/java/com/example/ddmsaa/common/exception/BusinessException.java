package com.example.ddmsaa.common.exception;

/**
 * 業務ルール違反を表す例外（HTTP 409）。
 *
 * <p>messageId は呼び出し側が必ず指定する。
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String messageId;

    private final String[] args;

    /**
     * 業務ルール違反の例外を生成する。
     *
     * @param messageId 違反内容を表すメッセージ ID
     * @param args メッセージの置換引数
     */
    public BusinessException(String messageId, String... args) {
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
