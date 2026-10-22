package com.example.ddmsaa.core.shared.port;

import java.util.Collection;
import java.util.Map;

/**
 * 商品参照ポート。商品・税区分・税率（{@code tax_rates}）の取得は {@code product} のデータであり、
 * 注文明細への複写値（BR-PRD-002）の元を一箇所で提供する。
 *
 * <p>所有者（実装）：{@code product}（{@code ProductLookupPortImpl}）。
 * 主な呼び出し元：{@code order}（{@code createOrder}・{@code updateOrder}・{@code submitOrder}）。</p>
 *
 * <p>トランザクション：読取専用。</p>
 */
public interface ProductLookupPort {

    /**
     * 注文用の商品スナップショットを取得する。
     *
     * <p>存在しない・論理削除済みの ID は戻り値の map に含めない。無効商品
     * （{@code status = INACTIVE}）は含める（有効性の判定は呼び出し側。BR-PRD-003）。</p>
     *
     * @param productIds 商品 ID の集合
     * @return 商品 ID をキーとするスナップショットの map
     */
    Map<String, ProductSnapshot> findForOrder(Collection<String> productIds);
}
