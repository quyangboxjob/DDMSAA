package com.example.ddmsaa.core.shared.port;

import java.util.List;

/**
 * 顧客参照ポート。顧客の存在・状態、「担当者として有効か」の判定、顧客の通知先メールは
 * {@code customer} のデータ（{@code customers}・{@code customer_assignments}・
 * {@code customer_contacts}）であり、他 feature が他 feature のテーブルを直接読まないための窓口。
 *
 * <p>所有者（実装）：{@code customer}（{@code CustomerLookupPortImpl}）。
 * 主な呼び出し元：{@code order}（{@link #findForOrder}・{@link #isActiveAssignee}。
 * {@code createOrder}・{@code updateOrder}・{@code submitOrder}）、
 * {@code approval}（{@code NotificationPort} 実装が {@link #findNotificationEmails}）。</p>
 *
 * <p>トランザクション：すべて読取専用。</p>
 */
public interface CustomerLookupPort {

    /**
     * 未削除（{@code deleted_at IS NULL}）の顧客の情報を取得する。
     *
     * <p>データ範囲は判定しない（呼び出し側が {@link DataScope} で判定する）。</p>
     *
     * @param customerId 顧客 ID
     * @return 顧客の情報。存在しなければ null
     */
    CustomerInfo findForOrder(String customerId);

    /**
     * ユーザーが顧客の担当者として有効かを判定する。
     *
     * <p>{@code customer_assignments.is_active = TRUE} かつ {@code deleted_at IS NULL} の割当があり、
     * {@code userId} が {@code users.role = EMPLOYEE}・{@code deleted_at IS NULL} であれば true
     * （停用・ロックの扱いは {@code customer} の判断。TBD-ORD-07）。</p>
     *
     * @param customerId 顧客 ID
     * @param userId ユーザー ID
     * @return 担当者として有効な場合 true
     */
    boolean isActiveAssignee(String customerId, String userId);

    /**
     * 顧客の通知先メールアドレスを取得する。
     *
     * <p>{@code customer_contacts} のうち {@code is_active = TRUE} かつ
     * {@code is_notification_recipient = TRUE}・未削除の連絡先のメールアドレスを重複なしで返す。
     * {@code ORDER_APPROVED}、および {@code fromStatus = APPROVED} の {@code ORDER_CANCELLED} の
     * 顧客通知先に使う。</p>
     *
     * @param customerId 顧客 ID
     * @return メールアドレスのリスト（重複なし。なければ空リスト）
     */
    List<String> findNotificationEmails(String customerId);
}
