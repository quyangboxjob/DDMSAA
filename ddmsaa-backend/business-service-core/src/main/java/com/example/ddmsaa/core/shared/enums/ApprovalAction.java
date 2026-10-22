package com.example.ddmsaa.core.shared.enums;

/**
 * 承認操作（T07-8。DB approval_action_codes の INT コードに対応）。
 */
public enum ApprovalAction {

    /** 承認。 */
    APPROVE(1),

    /** 差戻し。 */
    RETURN(2);

    private final int code;

    ApprovalAction(int code) {
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
    public static ApprovalAction fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ApprovalAction v : values()) {
            if (v.code == code) {
                return v;
            }
        }
        throw new IllegalArgumentException("Unknown ApprovalAction code: " + code);
    }
}
