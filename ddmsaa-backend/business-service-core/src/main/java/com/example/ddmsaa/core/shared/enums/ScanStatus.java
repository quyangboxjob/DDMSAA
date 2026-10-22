package com.example.ddmsaa.core.shared.enums;

/**
 * スキャン状態（T07-10。DB scan_status_codes の INT コードに対応）。
 */
public enum ScanStatus {

    /** 確認中。 */
    PENDING(1),

    /** 安全。 */
    SAFE(2),

    /** 拒否。 */
    REJECTED(3);

    private final int code;

    ScanStatus(int code) {
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
    public static ScanStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ScanStatus v : values()) {
            if (v.code == code) {
                return v;
            }
        }
        throw new IllegalArgumentException("Unknown ScanStatus code: " + code);
    }
}
