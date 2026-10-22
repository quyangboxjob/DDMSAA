package com.example.ddmsaa.core.shared.enums;

/**
 * 通知イベント（T07-11。DB notification_event_codes の INT コードに対応）。
 */
public enum NotificationEvent {

    /** 承認依頼。 */
    ORDER_SUBMITTED(1),

    /** 承認。 */
    ORDER_APPROVED(2),

    /** 差戻し。 */
    ORDER_RETURNED(3),

    /** 取消し。 */
    ORDER_CANCELLED(4),

    /** CSV 出力完了。 */
    EXPORT_COMPLETED(5);

    private final int code;

    NotificationEvent(int code) {
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
    public static NotificationEvent fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (NotificationEvent v : values()) {
            if (v.code == code) {
                return v;
            }
        }
        throw new IllegalArgumentException("Unknown NotificationEvent code: " + code);
    }
}
