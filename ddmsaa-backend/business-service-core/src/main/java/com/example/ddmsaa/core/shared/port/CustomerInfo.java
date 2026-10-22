package com.example.ddmsaa.core.shared.port;

import com.example.ddmsaa.core.shared.enums.CustomerStatus;

/**
 * 注文が参照する顧客の情報。{@link CustomerLookupPort#findForOrder} の戻り値。
 *
 * @param id 顧客 ID
 * @param name 顧客名
 * @param status 顧客状態
 */
public record CustomerInfo(String id, String name, CustomerStatus status) {
}
