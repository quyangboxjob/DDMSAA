package com.example.ddmsaa.core.shared.enums;

/**
 * 税区分（T07-5。DB tax_category_codes の INT コードに対応）。
 */
public enum TaxCategory {

    /** 標準税率。 */
    STANDARD(1),

    /** 軽減税率。 */
    REDUCED(2),

    /** 非課税。 */
    EXEMPT(3);

    private final int code;

    TaxCategory(int code) {
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
    public static TaxCategory fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TaxCategory v : values()) {
            if (v.code == code) {
                return v;
            }
        }
        throw new IllegalArgumentException("Unknown TaxCategory code: " + code);
    }
}
