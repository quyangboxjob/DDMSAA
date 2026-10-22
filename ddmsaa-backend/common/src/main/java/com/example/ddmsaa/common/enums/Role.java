package com.example.ddmsaa.common.enums;

/**
 * ロール（T07-1。07 Role の文字列値 ↔ DB INT コード）。
 */
public enum Role {

    /** 管理者。 */
    ADMIN(1),
    /** 従業員。 */
    EMPLOYEE(2),
    /** 顧客。 */
    CUSTOMER(3);

    private final int code;

    Role(int code) {
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
    public static Role fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (Role v : values()) {
            if (v.code == code) {
                return v;
            }
        }
        throw new IllegalArgumentException("Unknown Role code: " + code);
    }
}
