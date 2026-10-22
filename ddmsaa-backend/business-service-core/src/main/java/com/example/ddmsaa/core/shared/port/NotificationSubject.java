package com.example.ddmsaa.core.shared.port;

import com.example.ddmsaa.core.shared.enums.OrderStatus;

/**
 * 通知の対象。{@link NotificationPort#enqueue} の引数。
 *
 * <p>注文系イベントは {@code orderId}・{@code customerId}・{@code salesUserId} を必須とする。
 * {@code applicantUserId} は申請者（{@code submitOrder} は操作者、承認・差戻しは
 * {@code approval_requests.created_by}）。{@code cancelOrder} は null を渡してよく、その場合は
 * {@link NotificationPort} の実装が当該注文の最新の承認依頼の {@code created_by} を申請者とする
 * （なければ申請者宛は作らない）。{@code fromStatus} は {@code ORDER_CANCELLED} のとき
 * 取消し前の状態で、APPROVED なら顧客通知先を加える（BR-NTF-003）。
 * {@code EXPORT_COMPLETED} は {@code exportJobId} だけを持つ。</p>
 *
 * @param orderId 注文番号（出力完了通知では null）
 * @param customerId 顧客 ID（出力完了通知では null）
 * @param salesUserId 担当者のユーザー ID（出力完了通知では null）
 * @param applicantUserId 申請者のユーザー ID（null 可）
 * @param fromStatus 取消し前の状態（{@code ORDER_CANCELLED} 以外では null 可）
 * @param exportJobId 出力ジョブ ID（注文系イベントでは null）
 */
public record NotificationSubject(String orderId, String customerId, String salesUserId,
        String applicantUserId, OrderStatus fromStatus, String exportJobId) {

    /**
     * 注文系イベント用の通知対象を作る。
     *
     * @param orderId 注文番号
     * @param customerId 顧客 ID
     * @param salesUserId 担当者のユーザー ID
     * @param applicantUserId 申請者のユーザー ID（null 可）
     * @param fromStatus 取消し前の状態（{@code ORDER_CANCELLED} 以外では null 可）
     * @return 通知対象（{@code exportJobId} は null）
     */
    public static NotificationSubject forOrder(String orderId, String customerId,
            String salesUserId, String applicantUserId, OrderStatus fromStatus) {
        return new NotificationSubject(orderId, customerId, salesUserId, applicantUserId,
                fromStatus, null);
    }

    /**
     * CSV 出力完了通知（{@code EXPORT_COMPLETED}）用の通知対象を作る。
     *
     * @param exportJobId 出力ジョブ ID
     * @return 通知対象（注文系の項目はすべて null）
     */
    public static NotificationSubject forExport(String exportJobId) {
        return new NotificationSubject(null, null, null, null, null, exportJobId);
    }
}
