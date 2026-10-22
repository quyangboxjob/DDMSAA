package com.example.ddmsaa.core.shared.enums;

/**
 * 注文状態（T07-6。DB order_status_codes の INT コードに対応）。
 */
public enum OrderStatus {

    /** 下書き。 */
    DRAFT(1),

    /** 承認待ち。 */
    PENDING_APPROVAL(2),

    /** 差戻し。 */
    RETURNED(3),

    /** 承認済み。 */
    APPROVED(4),

    /** 取消済み。 */
    CANCELLED(5);

    private final int code;

    OrderStatus(int code) {
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
    public static OrderStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OrderStatus v : values()) {
            if (v.code == code) {
                return v;
            }
        }
        throw new IllegalArgumentException("Unknown OrderStatus code: " + code);
    }
}
