package com.example.ddmsaa.core.shared.port;

import com.example.ddmsaa.core.shared.enums.OrderStatus;

/**
 * 注文の可視情報（読取専用）。{@link OrderAccessPort} の戻り値。Entity は渡さない。
 *
 * @param id 注文番号
 * @param customerId 顧客 ID
 * @param salesUserId 担当者のユーザー ID
 * @param status 注文の状態
 */
public record OrderAccess(String id, String customerId, String salesUserId,
        OrderStatus status) {
}
