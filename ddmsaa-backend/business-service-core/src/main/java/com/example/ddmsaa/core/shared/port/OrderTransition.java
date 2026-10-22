package com.example.ddmsaa.core.shared.port;

import com.example.ddmsaa.core.shared.enums.OrderStatus;

/**
 * {@link OrderStatePort#transition} の戻り値。通知の件名・宛先に必要な注文情報。
 *
 * <p>承認側が {@link NotificationSubject} を作るために使う。</p>
 *
 * @param orderId 注文番号
 * @param customerId 顧客 ID
 * @param salesUserId 担当者（営業担当）のユーザー ID
 * @param fromStatus 遷移前の状態
 * @param toStatus 遷移後の状態
 */
public record OrderTransition(String orderId, String customerId, String salesUserId,
        OrderStatus fromStatus, OrderStatus toStatus) {
}
