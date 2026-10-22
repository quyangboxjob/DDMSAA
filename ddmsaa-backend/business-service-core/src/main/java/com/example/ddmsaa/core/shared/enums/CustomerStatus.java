package com.example.ddmsaa.core.shared.enums;

/**
 * 顧客状態（T07-3。DB customer_status_codes の INT コードに対応）。
 */
public enum CustomerStatus {

    /** 有効。 */
    ACTIVE(1),

    /** 無効。 */
    INACTIVE(2);

    private final int code;

    CustomerStatus(int code) {
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
    public static CustomerStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CustomerStatus v : values()) {
            if (v.code == code) {
                return v;
            }
        }
        throw new IllegalArgumentException("Unknown CustomerStatus code: " + code);
    }
}
