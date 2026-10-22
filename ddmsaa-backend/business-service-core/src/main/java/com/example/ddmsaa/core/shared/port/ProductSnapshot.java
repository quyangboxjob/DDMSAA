package com.example.ddmsaa.core.shared.port;

import com.example.ddmsaa.core.shared.enums.ProductStatus;
import com.example.ddmsaa.core.shared.enums.TaxCategory;
import java.math.BigDecimal;

/**
 * 注文明細へ複写する商品の値（BR-PRD-002）。{@link ProductLookupPort#findForOrder} の戻り値の値。
 *
 * @param id 商品 ID
 * @param code 商品コード
 * @param name 商品名
 * @param standardUnitPrice 標準単価
 * @param taxCategory 税区分
 * @param taxRate {@code tax_rates} から求め済みの税率
 * @param status 商品状態
 */
public record ProductSnapshot(String id, String code, String name,
        BigDecimal standardUnitPrice, TaxCategory taxCategory, BigDecimal taxRate,
        ProductStatus status) {
}
