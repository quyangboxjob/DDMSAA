package com.example.ddmsaa.infrastructure.scan;

/**
 * スキャナに到達できない、またはタイムアウトしたなど、ウイルススキャンを実施できなかったことを
 * 表す例外。呼び出し側（BAT-05）は対象を PENDING のまま次回に再試行する。
 */
public class VirusScanUnavailableException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * メッセージを指定して生成する。
     *
     * @param message 詳細メッセージ
     */
    public VirusScanUnavailableException(String message) {
        super(message);
    }

    /**
     * メッセージと原因を指定して生成する。
     *
     * @param message 詳細メッセージ
     * @param cause 原因
     */
    public VirusScanUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
