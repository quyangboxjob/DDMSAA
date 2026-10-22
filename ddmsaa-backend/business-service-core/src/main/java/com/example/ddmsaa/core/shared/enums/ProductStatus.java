package com.example.ddmsaa.core.shared.enums;

/**
 * 商品状態（T07-4。DB product_status_codes の INT コードに対応）。
 */
public enum ProductStatus {

    /** 有効。 */
    ACTIVE(1),

    /** 無効。 */
    INACTIVE(2);

    private final int code;

    ProductStatus(int code) {
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
    public static ProductStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ProductStatus v : values()) {
            if (v.code == code) {
                return v;
            }
        }
        throw new IllegalArgumentException("Unknown ProductStatus code: " + code);
    }
}
