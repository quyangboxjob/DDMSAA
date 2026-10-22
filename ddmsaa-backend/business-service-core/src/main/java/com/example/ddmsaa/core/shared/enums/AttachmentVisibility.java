package com.example.ddmsaa.core.shared.enums;

/**
 * 添付公開範囲（T07-9。DB attachment_visibility_codes の INT コードに対応）。
 */
public enum AttachmentVisibility {

    /** 社内。 */
    INTERNAL(1),

    /** 顧客公開。 */
    CUSTOMER(2);

    private final int code;

    AttachmentVisibility(int code) {
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
    public static AttachmentVisibility fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AttachmentVisibility v : values()) {
            if (v.code == code) {
                return v;
            }
        }
        throw new IllegalArgumentException("Unknown AttachmentVisibility code: " + code);
    }
}
