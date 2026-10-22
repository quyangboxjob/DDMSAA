package com.example.ddmsaa.common.exception;

/**
 * 認証失敗を表す例外（HTTP 401）。
 *
 * <p>messageId が null の場合、GlobalExceptionHandler は既定の E0004 を用いる。
 */
public class AuthException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String messageId;

    private final String[] args;

    /**
     * 認証失敗の例外を生成する。
     *
     * @param messageId メッセージ ID（null 可。null なら E0004 として扱われる）
     * @param args メッセージの置換引数
     */
    public AuthException(String messageId, String... args) {
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
