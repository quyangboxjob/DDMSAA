package com.example.ddmsaa.core.shared.enums;

/**
 * 通知状態（T07-12。DB notification_status_codes の INT コードに対応）。
 */
public enum NotificationStatus {

    /** 待機中。 */
    QUEUED(1),

    /** 送信中。 */
    SENDING(2),

    /** 送信済み。 */
    SENT(3),

    /** 失敗。 */
    FAILED(4);

    private final int code;

    NotificationStatus(int code) {
        this.code = code;
    }

    /**
     * DB の INT コード。
     *
     * @return INT コード
     */
    public int getCode() {
        return code;
    }

    /**
     * INT コードから enum を得る。
     *
     * @param code DB の INT コード（null 可）
     * @return 対応する enum。code が null なら null
     * @throws IllegalArgumentException 未知のコードの場合
     */
    public static NotificationStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (NotificationStatus v : values()) {
            if (v.code == code) {
                return v;
            }
        }
        throw new IllegalArgumentException("Unknown NotificationStatus code: " + code);
    }
}
