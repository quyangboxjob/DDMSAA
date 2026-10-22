package com.example.ddmsaa.core.shared.port;

import com.example.ddmsaa.core.shared.enums.OrderStatus;

/**
 * 注文状態ポート。{@code orders} の状態更新・状態履歴・注文系の監査は {@code order} のデータと規則
 * （BR-ORD-011）であり、承認側が {@code OrderMapper} を触らないための窓口。
 *
 * <p>所有者（実装）：{@code order}（{@code OrderStatePortImpl}）。
 * 主な呼び出し元：{@code approval}（{@code approveApprovalRequest}・{@code returnApprovalRequest}）。</p>
 *
 * <p>トランザクション：書込を伴い、呼び出し側のトランザクションに参加する
 * （実装は {@code @Transactional(propagation = MANDATORY)}）。ロック順序は先に
 * {@code approval_requests}、後に {@code orders}。</p>
 */
public interface OrderStatePort {

    /**
     * 注文の状態を遷移させる。
     *
     * <p>(1) {@code orders} を {@code SELECT ... FOR UPDATE}（データ範囲は適用しない）、
     * (2) 現在の状態が {@code expectedFrom} か確認、(3) 状態を更新、
     * (4) {@code order_status_histories} へ INSERT、(5) {@code AuditLogPort.record}
     * （{@code APPROVED} は {@code ORDER_APPROVE}、{@code RETURNED} は {@code ORDER_RETURN}）、
     * (6) {@link OrderTransition} を返す。受け付ける組合せは
     * {@code PENDING_APPROVAL → APPROVED} と {@code PENDING_APPROVAL → RETURNED} のみ。</p>
     *
     * @param orderId 注文番号
     * @param expectedFrom 遷移前に期待する状態
     * @param to 遷移後の状態
     * @param reason 遷移理由（差戻し理由など。null 可）
     * @return 通知の件名・宛先に必要な注文情報
     * @throws com.example.ddmsaa.common.exception.EntityNotFoundException 注文が存在しない
     *         （{@code E0005}）場合
     * @throws com.example.ddmsaa.common.exception.BusinessException 現在の状態が
     *         {@code expectedFrom} でない（{@code E0006}）場合
     * @throws IllegalArgumentException 受け付けない遷移の組合せ（呼び出しの誤り。500 {@code E0012}）
     */
    OrderTransition transition(String orderId, OrderStatus expectedFrom, OrderStatus to,
            String reason);
}
