package com.example.ddmsaa.core.shared.enums;

/**
 * 承認依頼状態（T07-7。DB approval_request_status_codes の INT コードに対応）。
 */
public enum ApprovalRequestStatus {

    /** 承認待ち。 */
    PENDING(1),

    /** 承認。 */
    APPROVED(2),

    /** 差戻し。 */
    RETURNED(3);

    private final int code;

    ApprovalRequestStatus(int code) {
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
    public static ApprovalRequestStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ApprovalRequestStatus v : values()) {
            if (v.code == code) {
                return v;
            }
        }
        throw new IllegalArgumentException("Unknown ApprovalRequestStatus code: " + code);
    }
}
