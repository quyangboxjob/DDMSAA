package com.example.ddmsaa.core.shared.enums;

/**
 * 利用者状態（T07-2。DB user_status_codes の INT コードに対応）。
 */
public enum UserStatus {

    /** 有効。 */
    ACTIVE(1),

    /** 停用。 */
    DISABLED(2);

    private final int code;

    UserStatus(int code) {
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
    public static UserStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (UserStatus v : values()) {
            if (v.code == code) {
                return v;
            }
        }
        throw new IllegalArgumentException("Unknown UserStatus code: " + code);
    }
}
